import { useState } from 'react'
import { api, hataMetni } from './api'
import { dersleriSuz, hocasizDersler, yerlesikBlokSayisi } from './dersFiltre'
import { blokMetni, ifadeKontrol, ifadeyiSadelestir } from './saatIfadesi'
import { useYukle } from './useYukle'
import type { Ders, DersTuru, OgretimElemani, ProgramProjesi, Sube, Yerlesim } from './tipler'

const TUR_ADLARI: Record<DersTuru, string> = {
  NORMAL: 'Normal',
  YERLESIMSIZ: 'Yerleşimsiz',
  SEMINER: 'Seminer',
}

function hocaMetni(h: OgretimElemani): string {
  return h.unvan ? `${h.unvan} ${h.adSoyad}` : h.adSoyad
}

interface SubeSatiriProps {
  sube: Sube
  hocalar: OgretimElemani[]
  blokSayisi: number
  yerlesik: number
  onDegisti: () => void
}

function SubeSatiri({ sube, hocalar, blokSayisi, yerlesik, onDegisti }: SubeSatiriProps) {
  const [hocaId, setHocaId] = useState(sube.ogretimElemaniId === null ? '' : String(sube.ogretimElemaniId))
  const [ogrenci, setOgrenci] = useState(sube.tahminiOgrenci === null ? '' : String(sube.tahminiOgrenci))
  const [sinifIzni, setSinifIzni] = useState(sube.sinifCakismaIzni)
  const [derslikIzni, setDerslikIzni] = useState(sube.derslikCakismaIzni)
  const [cokluIzni, setCokluIzni] = useState(sube.cokluDerslikIzni)
  const [hata, setHata] = useState<string | null>(null)
  const [bekliyor, setBekliyor] = useState(false)

  const kayitliHoca = sube.ogretimElemaniId === null ? '' : String(sube.ogretimElemaniId)
  const kayitliOgrenci = sube.tahminiOgrenci === null ? '' : String(sube.tahminiOgrenci)
  const degisti =
    hocaId !== kayitliHoca ||
    ogrenci.trim() !== kayitliOgrenci ||
    sinifIzni !== sube.sinifCakismaIzni ||
    derslikIzni !== sube.derslikCakismaIzni ||
    cokluIzni !== sube.cokluDerslikIzni

  async function kaydet() {
    const ogrenciSayisi = ogrenci.trim() === '' ? null : Number(ogrenci)
    if (ogrenciSayisi !== null && (!Number.isInteger(ogrenciSayisi) || ogrenciSayisi < 1 || ogrenciSayisi > 2000)) {
      setHata('Tahmini öğrenci sayısı 1 ile 2000 arasında tam sayı olmalıdır')
      return
    }
    setBekliyor(true)
    setHata(null)
    try {
      await api<Sube>(`/api/subeler/${sube.id}`, {
        method: 'PUT',
        body: JSON.stringify({
          ogretimElemaniId: hocaId === '' ? null : Number(hocaId),
          tahminiOgrenci: ogrenciSayisi,
          sinifCakismaIzni: sinifIzni,
          derslikCakismaIzni: derslikIzni,
          cokluDerslikIzni: cokluIzni,
        }),
      })
      onDegisti()
    } catch (e) {
      setHata(hataMetni(e))
    } finally {
      setBekliyor(false)
    }
  }

  async function sil() {
    const metin =
      `Şube ${sube.subeNo} silinecek.` +
      (yerlesik > 0 ? ` ${yerlesik} yerleşik blok programdan kalkacak.` : '') +
      ' Emin misiniz?'
    if (!window.confirm(metin)) {
      return
    }
    setBekliyor(true)
    setHata(null)
    try {
      await api<void>(`/api/subeler/${sube.id}`, { method: 'DELETE' })
      onDegisti()
    } catch (e) {
      setHata(hataMetni(e))
      setBekliyor(false)
    }
  }

  return (
    <div className="sube-satiri">
      <div className="sube-baslik">
        <strong>Şube {sube.subeNo}</strong>
        <span className="soluk">
          {blokSayisi} blok, {yerlesik} yerleşik
        </span>
      </div>
      <div className="sube-alanlari">
        <label>
          Öğretim elemanı
          <select value={hocaId} onChange={(olay) => setHocaId(olay.target.value)}>
            <option value="">Atanmamış</option>
            {hocalar.map((h) => (
              <option key={h.id} value={h.id}>
                {hocaMetni(h)}
              </option>
            ))}
          </select>
        </label>
        <label>
          Tahmini öğrenci
          <input
            type="number"
            min={1}
            max={2000}
            value={ogrenci}
            onChange={(olay) => setOgrenci(olay.target.value)}
            placeholder="Boş"
          />
        </label>
      </div>
      <div className="sube-izinleri">
        <label className="onay-kutusu">
          <input type="checkbox" checked={sinifIzni} onChange={(olay) => setSinifIzni(olay.target.checked)} />
          Sınıf çakışmasına izin ver
        </label>
        <label className="onay-kutusu">
          <input type="checkbox" checked={derslikIzni} onChange={(olay) => setDerslikIzni(olay.target.checked)} />
          Derslik çakışmasına izin ver
        </label>
        <label className="onay-kutusu">
          <input type="checkbox" checked={cokluIzni} onChange={(olay) => setCokluIzni(olay.target.checked)} />
          Birden fazla derslik kullanabilir
        </label>
      </div>
      {hata && (
        <p className="hata" role="alert">
          {hata}
        </p>
      )}
      <div className="sube-islemleri">
        <button type="button" disabled={bekliyor || !degisti} onClick={() => void kaydet()}>
          Kaydet
        </button>
        <button type="button" className="tehlike" disabled={bekliyor} onClick={() => void sil()}>
          Şubeyi sil
        </button>
      </div>
    </div>
  )
}

interface DersDuzenleProps {
  ders: Ders
  subeler: Sube[]
  hocalar: OgretimElemani[]
  yerlesimler: Yerlesim[]
  onDegisti: () => void
  kapat: () => void
}

function DersDuzenle({ ders, subeler, hocalar, yerlesimler, onDegisti, kapat }: DersDuzenleProps) {
  const [kod, setKod] = useState(ders.kod)
  const [ad, setAd] = useState(ders.ad)
  const [tur, setTur] = useState<DersTuru>(ders.tur)
  const [sanal, setSanal] = useState(ders.sanal)
  const [haftalik, setHaftalik] = useState(ders.haftalikSaat === null ? '' : String(ders.haftalikSaat))
  const [ifade, setIfade] = useState(ders.saatIfadesi ?? '')
  const [hata, setHata] = useState<string | null>(null)
  const [mesaj, setMesaj] = useState<string | null>(null)
  const [bekliyor, setBekliyor] = useState(false)

  const yerlesimsiz = tur === 'YERLESIMSIZ'
  const haftalikSayi = haftalik.trim() === '' ? null : Number(haftalik)
  const haftalikGecerli = haftalikSayi !== null && Number.isInteger(haftalikSayi) && haftalikSayi >= 1 && haftalikSayi <= 40
  const kontrol = yerlesimsiz
    ? null
    : haftalikGecerli
      ? ifadeKontrol(ifade, haftalikSayi)
      : { bloklar: [], hata: 'Haftalık saat 1 ile 40 arasında tam sayı olmalıdır' }
  const yerlesikSayisi = yerlesimler.filter((y) => y.dersId === ders.id).length

  async function kaydet() {
    if (kod.trim() === '' || ad.trim() === '') {
      setHata('Ders kodu ve adı boş olamaz')
      return
    }
    if (kontrol && kontrol.hata) {
      setHata(kontrol.hata)
      return
    }
    const yeniIfade = yerlesimsiz ? null : ifadeyiSadelestir(ifade) || String(haftalikSayi)
    const blokYapisiDegisiyor = yeniIfade !== ders.saatIfadesi || (tur === 'YERLESIMSIZ') !== (ders.tur === 'YERLESIMSIZ')
    if (blokYapisiDegisiyor && yerlesikSayisi > 0) {
      const metin =
        'Saat ifadesi ya da tür değişirse dersin blokları yeniden oluşturulabilir ve ' +
        `bu dersin ${yerlesikSayisi} yerleşik bloğunun yeri kaybolabilir. Devam edilsin mi?`
      if (!window.confirm(metin)) {
        return
      }
    }
    setBekliyor(true)
    setHata(null)
    setMesaj(null)
    try {
      const sonuc = await api<Ders>(`/api/dersler/${ders.id}`, {
        method: 'PUT',
        body: JSON.stringify({
          kod: kod.trim(),
          ad: ad.trim(),
          tur,
          sanal,
          haftalikSaat: yerlesimsiz ? null : haftalikSayi,
          saatIfadesi: yeniIfade,
        }),
      })
      setMesaj(
        sonuc.bloklar.length > 0
          ? `Ders kaydedildi. Bloklar: ${blokMetni(sonuc.bloklar)}`
          : 'Ders kaydedildi.',
      )
      onDegisti()
    } catch (e) {
      setHata(hataMetni(e))
    } finally {
      setBekliyor(false)
    }
  }

  async function dersiSil() {
    const metin =
      `"${ders.kod} ${ders.ad}" dersi silinecek. ${subeler.length} şube` +
      (yerlesikSayisi > 0 ? ` ve ${yerlesikSayisi} yerleşik blok` : '') +
      ' da silinir. Emin misiniz?'
    if (!window.confirm(metin)) {
      return
    }
    setBekliyor(true)
    setHata(null)
    try {
      await api<void>(`/api/dersler/${ders.id}`, { method: 'DELETE' })
      kapat()
      onDegisti()
    } catch (e) {
      setHata(hataMetni(e))
      setBekliyor(false)
    }
  }

  async function subeEkle() {
    setBekliyor(true)
    setHata(null)
    try {
      await api<Sube>('/api/subeler', { method: 'POST', body: JSON.stringify({ dersId: ders.id }) })
      onDegisti()
    } catch (e) {
      setHata(hataMetni(e))
    } finally {
      setBekliyor(false)
    }
  }

  return (
    <section className="kart duzenle-paneli">
      <div className="panel-baslik">
        <h2>
          {ders.kod} · {ders.ad}
        </h2>
        <button type="button" className="ikincil" onClick={kapat}>
          Kapat
        </button>
      </div>

      <div className="ice-form">
        <label>
          Ders kodu
          <input value={kod} maxLength={30} onChange={(olay) => setKod(olay.target.value)} />
        </label>
        <label>
          Ders adı
          <input value={ad} maxLength={200} onChange={(olay) => setAd(olay.target.value)} />
        </label>
        <label>
          Tür
          <select value={tur} onChange={(olay) => setTur(olay.target.value as DersTuru)}>
            {(Object.keys(TUR_ADLARI) as DersTuru[]).map((t) => (
              <option key={t} value={t}>
                {TUR_ADLARI[t]}
              </option>
            ))}
          </select>
        </label>
      </div>
      <label className="onay-kutusu">
        <input type="checkbox" checked={sanal} onChange={(olay) => setSanal(olay.target.checked)} />
        Sanal ders (yalnızca sanal sınıflara yerleştirilir)
      </label>

      {yerlesimsiz ? (
        <p className="soluk">Yerleşimsiz dersler programa yerleştirilmez, saat bilgisi tutulmaz.</p>
      ) : (
        <div className="ice-form">
          <label>
            Haftalık saat
            <input
              type="number"
              min={1}
              max={40}
              value={haftalik}
              onChange={(olay) => setHaftalik(olay.target.value)}
            />
          </label>
          <label>
            Saat ifadesi
            <input
              value={ifade}
              onChange={(olay) => setIfade(olay.target.value)}
              placeholder={haftalik !== '' ? haftalik : 'Örn. 3+2'}
            />
          </label>
        </div>
      )}
      {kontrol &&
        (kontrol.hata ? (
          <p className="hata">{kontrol.hata}</p>
        ) : (
          <p className="soluk">
            Bloklar: {blokMetni(kontrol.bloklar)} ({kontrol.bloklar.length} blok)
          </p>
        ))}

      {hata && (
        <p className="hata" role="alert">
          {hata}
        </p>
      )}
      {mesaj && <p className="basari-notu">{mesaj}</p>}
      <div className="arac-cubugu">
        <button type="button" disabled={bekliyor} onClick={() => void kaydet()}>
          {bekliyor ? 'Kaydediliyor…' : 'Dersi kaydet'}
        </button>
        <span className="bosluk" />
        <button type="button" className="tehlike" disabled={bekliyor} onClick={() => void dersiSil()}>
          Dersi sil
        </button>
      </div>

      <h3>Şubeler</h3>
      {subeler.length === 0 && <p className="soluk">Bu dersin şubesi yok.</p>}
      {subeler.map((s) => (
        <SubeSatiri
          key={s.id}
          sube={s}
          hocalar={hocalar}
          blokSayisi={ders.bloklar.length}
          yerlesik={yerlesikBlokSayisi(s.id, yerlesimler)}
          onDegisti={onDegisti}
        />
      ))}
      <p className="soluk">
        Hoca ya da öğrenci sayısı değiştiğinde yerleşik bloklar yeniden denetlenmez. Çakışmalar bir bloğu
        taşırken uyarı olarak görünür.
      </p>
      <button type="button" className="ikincil" disabled={bekliyor} onClick={() => void subeEkle()}>
        Şube ekle
      </button>
    </section>
  )
}

interface Props {
  proje: ProgramProjesi
  dersler: Ders[]
  yerlesimler: Yerlesim[]
  onDegisti: () => void
}

export default function DersYonetimi({ proje, dersler, yerlesimler, onDegisti }: Props) {
  const subeler = useYukle<Sube[]>('/api/subeler')
  const hocalar = useYukle<OgretimElemani[]>(`/api/ogretim-elemanlari?bolumId=${proje.bolumId}`)
  const [arama, setArama] = useState('')
  const [sadeceHocasiz, setSadeceHocasiz] = useState(false)
  const [seciliId, setSeciliId] = useState<number | null>(null)

  const hata = subeler.hata ?? hocalar.hata
  if (hata) {
    return (
      <p className="hata" role="alert">
        {hata}
      </p>
    )
  }
  if (!subeler.veri || !hocalar.veri) {
    return <p className="soluk">Yükleniyor…</p>
  }

  const dersIdleri = new Set(dersler.map((d) => d.id))
  const projeSubeleri = subeler.veri.filter((s) => dersIdleri.has(s.dersId))
  const siraliHocalar = [...hocalar.veri].sort((a, b) => a.adSoyad.localeCompare(b.adSoyad, 'tr'))
  const hocaMap = new Map(hocalar.veri.map((h) => [h.id, h]))
  const gorunen = dersleriSuz(dersler, projeSubeleri, arama, sadeceHocasiz)
  const hocasizSayisi = hocasizDersler(projeSubeleri).size
  const secili = seciliId !== null ? dersler.find((d) => d.id === seciliId) : undefined

  const degisti = () => {
    subeler.yenile()
    hocalar.yenile()
    onDegisti()
  }

  return (
    <>
      {secili && (
        <DersDuzenle
          key={secili.id}
          ders={secili}
          subeler={projeSubeleri.filter((s) => s.dersId === secili.id)}
          hocalar={siraliHocalar}
          yerlesimler={yerlesimler}
          onDegisti={degisti}
          kapat={() => setSeciliId(null)}
        />
      )}

      <div className="ice-form">
        <label>
          Ders ara (kod ya da ad)
          <input value={arama} onChange={(olay) => setArama(olay.target.value)} placeholder="Örn. COM 107" />
        </label>
        <label className="onay-kutusu">
          <input
            type="checkbox"
            checked={sadeceHocasiz}
            onChange={(olay) => setSadeceHocasiz(olay.target.checked)}
          />
          Yalnızca hocası eksik olanlar ({hocasizSayisi})
        </label>
        <span className="soluk">
          {gorunen.length} / {dersler.length} ders
        </span>
      </div>

      <div className="kaydirma buyuk">
        <table>
          <thead>
            <tr>
              <th>Kod</th>
              <th>Ders</th>
              <th>Tür</th>
              <th>Saat</th>
              <th>Şube</th>
              <th>Öğretim elemanı</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {dersler.length === 0 && (
              <tr>
                <td colSpan={7} className="soluk">
                  Bu projede henüz ders yok. Yukarıdaki içe aktarma bölümünden OBS listesini yükleyebilirsiniz.
                </td>
              </tr>
            )}
            {dersler.length > 0 && gorunen.length === 0 && (
              <tr>
                <td colSpan={7} className="soluk">
                  Eşleşen ders yok.
                </td>
              </tr>
            )}
            {gorunen.map((d) => {
              const dersSubeleri = projeSubeleri.filter((s) => s.dersId === d.id)
              const adlar = [
                ...new Set(
                  dersSubeleri
                    .map((s) => (s.ogretimElemaniId === null ? null : hocaMap.get(s.ogretimElemaniId)?.adSoyad))
                    .filter((a): a is string => a !== null && a !== undefined),
                ),
              ]
              const eksik = dersSubeleri.some((s) => s.ogretimElemaniId === null)
              return (
                <tr key={d.id} className={d.id === seciliId ? 'secili-satir' : undefined}>
                  <td>{d.kod}</td>
                  <td>
                    {d.ad} {d.sanal && <span className="rozet sanal">Sanal</span>}{' '}
                    {d.secmeli && (
                      <span className="rozet secmeli">
                        Seçmeli{d.secmeliGrup ? ` · ${d.secmeliGrup}` : ''}
                      </span>
                    )}
                  </td>
                  <td>{TUR_ADLARI[d.tur]}</td>
                  <td>{d.saatIfadesi ?? '—'}</td>
                  <td>{dersSubeleri.length}</td>
                  <td>
                    {adlar.join(', ')}
                    {eksik && <span className="rozet eksik">Hoca eksik</span>}
                    {adlar.length === 0 && !eksik && <span className="soluk">—</span>}
                  </td>
                  <td>
                    <button type="button" className="ikincil" onClick={() => setSeciliId(d.id)}>
                      Düzenle
                    </button>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </>
  )
}