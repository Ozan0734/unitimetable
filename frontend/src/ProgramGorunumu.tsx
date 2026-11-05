import { useState } from 'react'
import Izgara from './Izgara'
import type { Kutu } from './Izgara'
import { useYukle } from './useYukle'
import { aralikMetni, GUN_ADLARI } from './zaman'
import type {
  AkademikProgram,
  Derslik,
  Dil,
  Ders,
  DersSinif,
  OgretimElemani,
  ProgramProjesi,
  Seviye,
  Sinif,
  Sube,
  Yerlesim,
  ZamanIzgarasi,
} from './tipler'

type Gorunum = 'SINIF' | 'HOCA' | 'DERSLIK'

interface Grup {
  etiket: string | null
  ogeler: { id: number; ad: string }[]
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
}

export default function ProgramGorunumu({ proje, dersler, yerlesimler }: Props) {
  const izgara = useYukle<ZamanIzgarasi>('/api/zaman-izgarasi')
  const subeler = useYukle<Sube[]>('/api/subeler')
  const hocalar = useYukle<OgretimElemani[]>(`/api/ogretim-elemanlari?bolumId=${proje.bolumId}`)
  const derslikler = useYukle<Derslik[]>(`/api/derslikler?kurumId=${proje.kurumId}`)
  const programlar = useYukle<AkademikProgram[]>(`/api/programlar?bolumId=${proje.bolumId}`)
  const siniflar = useYukle<Sinif[]>('/api/siniflar')
  const dersSiniflari = useYukle<DersSinif[]>('/api/ders-sinif')
  const [gorunum, setGorunum] = useState<Gorunum>('SINIF')
  const [secim, setSecim] = useState<number | null>(null)

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
  const kullanilan = new Set(yerlesimler.flatMap((y) => y.derslikIdler))

  const gruplar = secenekGruplari(gorunum, pr, sn, hc, dl, kullanilan)
  const tumIdler = gruplar.flatMap((g) => g.ogeler.map((o) => o.id))
  const etkinSecim = secim !== null && tumIdler.includes(secim) ? secim : (tumIdler[0] ?? null)

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
      const derslikAdlari = y.derslikIdler.map((id) => derslikMap.get(id)?.ad ?? `Derslik ${id}`).join(', ')
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
      kutular.push({
        id: y.id,
        gun: y.gun,
        baslangic: y.baslangicSlot,
        bitis: y.bitisSlot,
        baslik: sube.subeNo > 1 ? `${ders.kod} · Şube ${sube.subeNo}` : ders.kod,
        altSatirlar,
        renk,
        kilitli: y.kilitli,
        ipucu: [
          `${ders.kod} ${ders.ad}`,
          `Şube ${sube.subeNo}`,
          hocaMetni,
          derslikAdlari,
          `${GUN_ADLARI[y.gun - 1]} ${aralikMetni(ig, y.baslangicSlot, y.bitisSlot)}`,
          y.kilitli ? 'Kilitli' : '',
        ]
          .filter((s) => s !== '')
          .join('\n'),
      })
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
      {etkinSecim === null ? (
        <p className="soluk">{BOS_MESAJLARI[gorunum]}</p>
      ) : (
        <Izgara izgara={ig} kutular={kutular} />
      )}
    </>
  )
}