import { useMemo, useRef, useState } from 'react'
import { api, ApiHatasi, hataMetni } from './api'
import { anahtar, baslangicSlotu, enIyiYerler } from './birakma'
import type { Hucre } from './birakma'
import Izgara from './Izgara'
import type { Hayalet, Kutu } from './Izgara'
import YerlesmeyenListesi from './YerlesmeyenListesi'
import { useYukle } from './useYukle'
import { aralikMetni, GUN_ADLARI } from './zaman'
import type {
  AkademikProgram,
  CakismaGovdesi,
  Derslik,
  Dil,
  Ders,
  DersSinif,
  Ihlal,
  OgretimElemani,
  ProgramProjesi,
  Seviye,
  Sinif,
  Sube,
  UygunYer,
  Yerlesim,
  YerlesmeyenBlok,
  ZamanIzgarasi,
} from './tipler'

type Gorunum = 'SINIF' | 'HOCA' | 'DERSLIK'

interface Grup {
  etiket: string | null
  ogeler: { id: number; ad: string }[]
}

interface Bildirim {
  tur: 'basari' | 'hata' | 'uyari'
  baslik: string
  satirlar: string[]
}

interface Surukleme {
  blokId: number
  sure: number
  ofset: number
  derslikler: number[]
  ad: string
}

const GORUNUM_ADLARI: Record<Gorunum, string> = {
  SINIF: 'Sınıf',
  HOCA: 'Öğretim elemanı',
  DERSLIK: 'Derslik',
}
const BOS_MESAJLARI: Record<Gorunum, string> = {
  SINIF: 'Bu bölümde henüz program ve sınıf tanımlı değil.',
  HOCA: 'Bu bölümde henüz öğretim elemanı yok.',
  DERSLIK: 'Henüz derslik tanımlı değil.',
}
const SEVIYE_SIRASI: Record<Seviye, number> = { LISANS: 0, YUKSEK_LISANS: 1, DOKTORA: 2 }
const DIL_SIRASI: Record<Dil, number> = { EN: 0, TR: 1 }
const PALET = [
  '#2b5fa6',
  '#c2410c',
  '#15803d',
  '#7e22ce',
  '#b45309',
  '#0e7490',
  '#be185d',
  '#4d7c0f',
  '#6d28d9',
  '#b91c1c',
]

function ihlalMetinleri(ihlaller: Ihlal[], turler: Ihlal['tur'][]): string[] {
  return ihlaller.filter((i) => turler.includes(i.tur)).map((i) => i.mesaj)
}

function secenekGruplari(
  gorunum: Gorunum,
  programlar: AkademikProgram[],
  siniflar: Sinif[],
  hocalar: OgretimElemani[],
  derslikler: Derslik[],
  kullanilanDerslikler: Set<number>,
): Grup[] {
  if (gorunum === 'SINIF') {
    return [...programlar]
      .sort(
        (a, b) =>
          SEVIYE_SIRASI[a.seviye] - SEVIYE_SIRASI[b.seviye] ||
          DIL_SIRASI[a.dil] - DIL_SIRASI[b.dil] ||
          a.ad.localeCompare(b.ad, 'tr'),
      )
      .map((program) => ({
        etiket: program.ad,
        ogeler: siniflar
          .filter((s) => s.programId === program.id)
          .sort((a, b) => a.duzey - b.duzey)
          .map((s) => ({ id: s.id, ad: s.ad })),
      }))
      .filter((grup) => grup.ogeler.length > 0)
  }
  if (gorunum === 'HOCA') {
    const ogeler = [...hocalar]
      .sort((a, b) => a.adSoyad.localeCompare(b.adSoyad, 'tr'))
      .map((h) => ({ id: h.id, ad: h.unvan ? `${h.adSoyad} (${h.unvan})` : h.adSoyad }))
    return ogeler.length > 0 ? [{ etiket: null, ogeler }] : []
  }
  const sirali = [...derslikler].sort((a, b) => a.ad.localeCompare(b.ad, 'tr'))
  const oge = (d: Derslik) => ({ id: d.id, ad: `${d.ad} (${d.kapasite} kişi)` })
  return [
    { etiket: 'Bu projede kullanılan', ogeler: sirali.filter((d) => kullanilanDerslikler.has(d.id)).map(oge) },
    { etiket: 'Diğer derslikler', ogeler: sirali.filter((d) => !kullanilanDerslikler.has(d.id)).map(oge) },
  ].filter((grup) => grup.ogeler.length > 0)
}

interface Props {
  proje: ProgramProjesi
  dersler: Ders[]
  yerlesimler: Yerlesim[]
  yerlesmeyenler: YerlesmeyenBlok[]
  onDegisti: () => void
}

export default function ProgramGorunumu({ proje, dersler, yerlesimler, yerlesmeyenler, onDegisti }: Props) {
  const izgara = useYukle<ZamanIzgarasi>('/api/zaman-izgarasi')
  const subeler = useYukle<Sube[]>('/api/subeler')
  const hocalar = useYukle<OgretimElemani[]>(`/api/ogretim-elemanlari?bolumId=${proje.bolumId}`)
  const derslikler = useYukle<Derslik[]>(`/api/derslikler?kurumId=${proje.kurumId}`)
  const programlar = useYukle<AkademikProgram[]>(`/api/programlar?bolumId=${proje.bolumId}`)
  const siniflar = useYukle<Sinif[]>('/api/siniflar')
  const dersSiniflari = useYukle<DersSinif[]>('/api/ders-sinif')
  const [gorunum, setGorunum] = useState<Gorunum>('SINIF')
  const [secim, setSecim] = useState<number | null>(null)
  const [surukleme, setSurukleme] = useState<Surukleme | null>(null)
  const [hucreler, setHucreler] = useState<Map<string, Hucre> | null>(null)
  const [ustunde, setUstunde] = useState<{ gun: number; slot: number } | null>(null)
  const [bildirim, setBildirim] = useState<Bildirim | null>(null)
  const [mesgul, setMesgul] = useState(false)
  const jeton = useRef(0)

  const hucreRenkleri = useMemo(() => {
    if (!hucreler) {
      return null
    }
    return new Map([...hucreler].map(([k, h]) => [k, h.durum] as const))
  }, [hucreler])

  const hata = [izgara, subeler, hocalar, derslikler, programlar, siniflar, dersSiniflari]
    .map((y) => y.hata)
    .find((h) => h !== undefined)
  if (hata) {
    return (
      <p className="hata" role="alert">
        {hata}
      </p>
    )
  }
  const { veri: ig } = izgara
  const { veri: sb } = subeler
  const { veri: hc } = hocalar
  const { veri: dl } = derslikler
  const { veri: pr } = programlar
  const { veri: sn } = siniflar
  const { veri: ds } = dersSiniflari
  if (!ig || !sb || !hc || !dl || !pr || !sn || !ds) {
    return <p className="soluk">Yükleniyor…</p>
  }

  const dersMap = new Map(dersler.map((d) => [d.id, d]))
  const subeMap = new Map(sb.map((s) => [s.id, s]))
  const hocaMap = new Map(hc.map((h) => [h.id, h]))
  const derslikMap = new Map(dl.map((d) => [d.id, d]))
  const derslikAdi = (id: number) => derslikMap.get(id)?.ad ?? `Derslik ${id}`
  const kullanilan = new Set(yerlesimler.flatMap((y) => y.derslikIdler))

  const gruplar = secenekGruplari(gorunum, pr, sn, hc, dl, kullanilan)
  const tumIdler = gruplar.flatMap((g) => g.ogeler.map((o) => o.id))
  const etkinSecim = secim !== null && tumIdler.includes(secim) ? secim : (tumIdler[0] ?? null)

  const temizle = () => {
    jeton.current += 1
    setSurukleme(null)
    setHucreler(null)
    setUstunde(null)
  }

  const baslat = (yeni: Surukleme) => {
    if (mesgul) {
      return
    }
    jeton.current += 1
    const bu = jeton.current
    window.setTimeout(() => {
      if (jeton.current !== bu) {
        return
      }
      setBildirim(null)
      setSurukleme(yeni)
      setHucreler(null)
      setUstunde(null)
      const sorgu = yeni.derslikler.length > 0 ? `?derslikIdler=${yeni.derslikler.join(',')}` : ''
      api<UygunYer[]>(`/api/bloklar/${yeni.blokId}/uygun-yerler${sorgu}`)
        .then((yerler) => {
          if (jeton.current === bu) {
            setHucreler(enIyiYerler(yerler, yeni.derslikler))
          }
        })
        .catch((e: unknown) => {
          if (jeton.current === bu) {
            setBildirim({ tur: 'hata', baslik: 'Uygun yerler alınamadı', satirlar: [hataMetni(e)] })
            temizle()
          }
        })
    }, 0)
  }

  const yerlestir = async (
    s: Surukleme,
    gun: number,
    baslangic: number,
    derslikId: number,
    onayli: boolean,
  ): Promise<void> => {
    setMesgul(true)
    try {
      await api<Yerlesim>(`/api/bloklar/${s.blokId}/yerlesim`, {
        method: 'PUT',
        body: JSON.stringify({
          gun,
          baslangicSlot: baslangic,
          derslikIdler: [derslikId],
          uyarilariOnayla: onayli,
        }),
      })
      setBildirim({
        tur: 'basari',
        baslik: `${s.ad} yerleştirildi`,
        satirlar: [
          `${GUN_ADLARI[gun - 1]} ${aralikMetni(ig, baslangic, baslangic + s.sure - 1)}, ${derslikAdi(derslikId)}`,
          ...(onayli ? ['Uyarılar onaylandı.'] : []),
        ],
      })
      onDegisti()
    } catch (e) {
      if (e instanceof ApiHatasi && e.durum === 409) {
        const govde = e.govde as CakismaGovdesi | null
        if (govde && Array.isArray(govde.ihlaller)) {
          if (govde.onayGerekli && !onayli) {
            const metin = `Bu yerleştirme için uyarılar var:\n\n${ihlalMetinleri(govde.ihlaller, ['UYARI'])
              .map((m) => `• ${m}`)
              .join('\n')}\n\nYine de yerleştirilsin mi?`
            if (window.confirm(metin)) {
              await yerlestir(s, gun, baslangic, derslikId, true)
            }
            return
          }
          setBildirim({
            tur: 'hata',
            baslik: govde.mesaj,
            satirlar: ihlalMetinleri(govde.ihlaller, ['ENGEL']),
          })
          return
        }
      }
      setBildirim({ tur: 'hata', baslik: 'Yerleştirme yapılamadı', satirlar: [hataMetni(e)] })
    } finally {
      setMesgul(false)
    }
  }

  const birak = async (h: { gun: number; slot: number }) => {
    if (!surukleme || mesgul) {
      return
    }
    const s = surukleme
    if (!hucreler) {
      setBildirim({
        tur: 'uyari',
        baslik: 'Uygun yerler hesaplanıyor',
        satirlar: ['Birkaç saniye sonra tekrar deneyin.'],
      })
      return
    }
    const baslangic = baslangicSlotu(h.slot, s.ofset, s.sure, ig.gunlukSlotSayisi)
    const hucre = baslangic === null ? undefined : hucreler.get(anahtar(h.gun, baslangic))
    temizle()
    if (baslangic === null || !hucre) {
      setBildirim({
        tur: 'hata',
        baslik: 'Blok buraya yerleştirilemez',
        satirlar: ['Blok günün dışına taşıyor ya da bu saat için denenecek derslik yok.'],
      })
      return
    }
    if (hucre.durum === 'ENGELLI') {
      setBildirim({
        tur: 'hata',
        baslik: 'Blok buraya yerleştirilemez',
        satirlar: ihlalMetinleri(hucre.ihlaller, ['ENGEL']),
      })
      return
    }
    if (hucre.durum === 'UYARILI') {
      const metin = `Bu yerleştirme için uyarılar var:\n\n${ihlalMetinleri(hucre.ihlaller, ['UYARI'])
        .map((m) => `• ${m}`)
        .join('\n')}\n\nYine de yerleştirilsin mi?`
      if (!window.confirm(metin)) {
        return
      }
      await yerlestir(s, h.gun, baslangic, hucre.derslikId, true)
      return
    }
    await yerlestir(s, h.gun, baslangic, hucre.derslikId, false)
  }

  const kutular: Kutu[] = []
  if (etkinSecim !== null) {
    const sinifDersleri = new Set(ds.filter((x) => x.sinifId === etkinSecim).map((x) => x.dersId))
    for (const y of yerlesimler) {
      const ders = dersMap.get(y.dersId)
      const sube = subeMap.get(y.subeId)
      if (!ders || !sube) {
        continue
      }
      if (gorunum === 'SINIF' && !sinifDersleri.has(y.dersId)) {
        continue
      }
      if (gorunum === 'HOCA' && sube.ogretimElemaniId !== etkinSecim) {
        continue
      }
      if (gorunum === 'DERSLIK' && !y.derslikIdler.includes(etkinSecim)) {
        continue
      }
      if (y.gun > ig.gunSayisi || y.bitisSlot > ig.gunlukSlotSayisi) {
        continue
      }
      const hoca = sube.ogretimElemaniId !== null ? hocaMap.get(sube.ogretimElemaniId) : undefined
      const derslikAdlari = y.derslikIdler.map(derslikAdi).join(', ')
      const hocaMetni = hoca ? hoca.adSoyad : 'Hoca atanmamış'
      const altSatirlar = [ders.ad]
      if (gorunum !== 'HOCA') {
        altSatirlar.push(hocaMetni)
      }
      if (gorunum !== 'DERSLIK') {
        altSatirlar.push(derslikAdlari)
      }
      const renk =
        sube.ogretimElemaniId === null
          ? null
          : (hoca?.renk ?? PALET[sube.ogretimElemaniId % PALET.length])
      const hareketEdebilir = !y.kilitli && y.derslikIdler.length === 1
      kutular.push({
        id: y.id,
        blokId: y.blokId,
        sure: y.sure,
        derslikIdler: y.derslikIdler,
        gun: y.gun,
        baslangic: y.baslangicSlot,
        bitis: y.bitisSlot,
        baslik: sube.subeNo > 1 ? `${ders.kod} · Şube ${sube.subeNo}` : ders.kod,
        altSatirlar,
        renk,
        kilitli: y.kilitli,
        hareketEdebilir,
        ipucu: [
          `${ders.kod} ${ders.ad}`,
          `Şube ${sube.subeNo}`,
          hocaMetni,
          derslikAdlari,
          `${GUN_ADLARI[y.gun - 1]} ${aralikMetni(ig, y.baslangicSlot, y.bitisSlot)}`,
          y.kilitli ? 'Kilitli: taşımak için önce kilidi kaldırın' : '',
          !y.kilitli && y.derslikIdler.length > 1 ? 'Çoklu derslikli blok şimdilik sürüklenemez' : '',
        ]
          .filter((m) => m !== '')
          .join('\n'),
      })
    }
  }

  let hayalet: Hayalet | null = null
  let durumMetni = ''
  if (surukleme) {
    durumMetni = `${surukleme.ad} taşınıyor`
    if (!hucreler) {
      durumMetni = 'Uygun yerler hesaplanıyor…'
    } else if (ustunde) {
      const bas = baslangicSlotu(ustunde.slot, surukleme.ofset, surukleme.sure, ig.gunlukSlotSayisi)
      const hucre = bas === null ? undefined : hucreler.get(anahtar(ustunde.gun, bas))
      if (bas === null) {
        durumMetni = 'Blok buraya sığmıyor'
      } else if (!hucre) {
        durumMetni = 'Bu saatte denenecek derslik bulunamadı'
      } else {
        hayalet = { gun: ustunde.gun, baslangic: bas, sure: surukleme.sure, durum: hucre.durum }
        const mesajlar = ihlalMetinleri(hucre.ihlaller, ['ENGEL', 'UYARI'])
        durumMetni = mesajlar.length === 0 ? `Uygun: ${derslikAdi(hucre.derslikId)}` : mesajlar.join(' · ')
      }
    }
  }

  return (
    <>
      <div className="gorunum-secici">
        <div className="sekmeler" role="tablist">
          {(Object.keys(GORUNUM_ADLARI) as Gorunum[]).map((g) => (
            <button
              key={g}
              type="button"
              role="tab"
              aria-selected={gorunum === g}
              className={gorunum === g ? 'sekme secili' : 'sekme'}
              onClick={() => {
                setGorunum(g)
                setSecim(null)
              }}
            >
              {GORUNUM_ADLARI[g]}
            </button>
          ))}
        </div>
        {etkinSecim !== null && (
          <select
            aria-label={GORUNUM_ADLARI[gorunum]}
            value={etkinSecim}
            onChange={(olay) => setSecim(Number(olay.target.value))}
          >
            {gruplar.map((grup, i) =>
              grup.etiket === null ? (
                grup.ogeler.map((o) => (
                  <option key={o.id} value={o.id}>
                    {o.ad}
                  </option>
                ))
              ) : (
                <optgroup key={i} label={grup.etiket}>
                  {grup.ogeler.map((o) => (
                    <option key={o.id} value={o.id}>
                      {o.ad}
                    </option>
                  ))}
                </optgroup>
              ),
            )}
          </select>
        )}
        <span className="soluk">{etkinSecim !== null ? `${kutular.length} blok gösteriliyor` : ''}</span>
      </div>

      {bildirim && (
        <div className={`bildirim ${bildirim.tur}`} role={bildirim.tur === 'hata' ? 'alert' : 'status'}>
          <strong>{bildirim.baslik}</strong>
          {bildirim.satirlar.map((satir, i) => (
            <div key={i}>{satir}</div>
          ))}
          <button type="button" className="kapat" aria-label="Kapat" onClick={() => setBildirim(null)}>
            ×
          </button>
        </div>
      )}

      {surukleme && (
        <div className="surukle-durum">
          <span className="aciklama">
            <span className="nokta hucre-uygun" /> Uygun <span className="nokta hucre-uyarili" /> Uyarılı{' '}
            <span className="nokta hucre-engelli" /> Engelli
          </span>
          <span>{durumMetni}</span>
        </div>
      )}

      {etkinSecim === null ? (
        <p className="soluk">{BOS_MESAJLARI[gorunum]}</p>
      ) : (
        <Izgara
          izgara={ig}
          kutular={kutular}
          suruklenen={surukleme !== null}
          hucreRenkleri={hucreRenkleri}
          hayalet={hayalet}
          kutuSurukleBasla={(kutu, ofset) =>
            baslat({
              blokId: kutu.blokId,
              sure: kutu.sure,
              ofset,
              derslikler: kutu.derslikIdler,
              ad: kutu.baslik,
            })
          }
          surukleBitti={temizle}
          ustunde={(h) =>
            setUstunde((onceki) =>
              onceki === h || (onceki && h && onceki.gun === h.gun && onceki.slot === h.slot) ? onceki : h,
            )
          }
          birak={(h) => void birak(h)}
        />
      )}

      <h2>Yerleşmeyen bloklar</h2>
      <YerlesmeyenListesi
        bloklar={yerlesmeyenler}
        surukleBasla={(b) =>
          baslat({
            blokId: b.blokId,
            sure: b.sure,
            ofset: 0,
            derslikler: [],
            ad: `${b.dersKodu} (Blok ${b.siraNo})`,
          })
        }
        surukleBitti={temizle}
      />
    </>
  )
}