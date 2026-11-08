import { useState } from 'react'
import { api, hataMetni } from './api'
import { aramaEslesir, formAlanlari, grupDurumu, kodListesi, sinifTahmini } from './obsAktarim'
import { useYukle } from './useYukle'
import type { AkademikProgram, ObsDers, ObsKayitSonucu, ObsOnizleme, ProgramProjesi, Seviye } from './tipler'

type Donem = 'GUZ' | 'BAHAR'

interface Props {
  proje: ProgramProjesi
  onKaydedildi: () => void
}

const VARSAYILAN_SANAL = 'ATA, TDB, ISG'
const SEVIYE_ADLARI: Record<Seviye, string> = {
  LISANS: 'Lisans',
  YUKSEK_LISANS: 'Yüksek lisans',
  DOKTORA: 'Doktora',
}

function Etiketler({ ders, sanalOnekleri, seminerKodlari }: {
  ders: ObsDers
  sanalOnekleri: string[]
  seminerKodlari: string[]
}) {
  const tahmin = sinifTahmini(ders, sanalOnekleri, seminerKodlari)
  return (
    <>
      {tahmin.tur === 'SEMINER' && <span className="rozet">Seminer</span>}
      {tahmin.tur === 'YERLESIMSIZ' && <span className="rozet yerlesimsiz">Yerleşimsiz</span>}
      {tahmin.sanal && <span className="rozet sanal">Sanal</span>}
    </>
  )
}

export default function ObsIceAktar({ proje, onKaydedildi }: Props) {
  const programlar = useYukle<AkademikProgram[]>(`/api/programlar?bolumId=${proje.bolumId}`)
  const [dosya, setDosya] = useState<File | null>(null)
  const [donem, setDonem] = useState<Donem>('GUZ')
  const [programId, setProgramId] = useState<number | null>(null)
  const [onizleme, setOnizleme] = useState<ObsOnizleme | null>(null)
  const [secilen, setSecilen] = useState<Set<string>>(new Set())
  const [arama, setArama] = useState('')
  const [sanalMetni, setSanalMetni] = useState(VARSAYILAN_SANAL)
  const [seminerMetni, setSeminerMetni] = useState('')
  const [mesgul, setMesgul] = useState(false)
  const [hata, setHata] = useState<string | null>(null)
  const [sonuc, setSonuc] = useState<ObsKayitSonucu | null>(null)

  const programListesi = programlar.veri ?? []
  const etkinProgram = programId ?? (programListesi.length === 1 ? programListesi[0].id : null)
  const sanalOnekleri = kodListesi(sanalMetni)
  const seminerKodlari = kodListesi(seminerMetni)
  const aramaVar = arama.trim() !== ''

  function girdiDegisti() {
    setOnizleme(null)
    setSonuc(null)
    setHata(null)
  }

  function sec(kod: string) {
    setSecilen((onceki) => {
      const yeni = new Set(onceki)
      if (yeni.has(kod)) {
        yeni.delete(kod)
      } else {
        yeni.add(kod)
      }
      return yeni
    })
  }

  function toplu(dersler: ObsDers[], ekle: boolean) {
    setSecilen((onceki) => {
      const yeni = new Set(onceki)
      for (const d of dersler) {
        if (ekle) {
          yeni.add(d.kod)
        } else {
          yeni.delete(d.kod)
        }
      }
      return yeni
    })
  }

  async function onizle() {
    if (!dosya) {
      setHata('Önce bir Excel (.xlsx) dosyası seçin')
      return
    }
    setMesgul(true)
    setHata(null)
    setSonuc(null)
    try {
      const form = new FormData()
      form.append('dosya', dosya)
      form.append('donem', donem)
      const veri = await api<ObsOnizleme>('/api/ice-aktarma/obs-ders-listesi/onizleme', {
        method: 'POST',
        body: form,
      })
      setOnizleme(veri)
      setSecilen(new Set())
      setArama('')
    } catch (e) {
      setOnizleme(null)
      setHata(hataMetni(e))
    } finally {
      setMesgul(false)
    }
  }

  async function kaydet() {
    if (!dosya || !onizleme || etkinProgram === null) {
      return
    }
    const secimler = [...secilen]
    const metin = `${onizleme.zorunluSayisi} zorunlu ve ${secimler.length} seçmeli ders "${proje.ad}" projesine eklenecek. Devam edilsin mi?`
    if (!window.confirm(metin)) {
      return
    }
    setMesgul(true)
    setHata(null)
    try {
      const form = new FormData()
      form.append('dosya', dosya)
      const alanlar = formAlanlari({
        donem,
        projeId: proje.id,
        programId: etkinProgram,
        secmeliDersler: secimler,
        sanalOnekleri,
        seminerKodlari,
      })
      for (const [ad, deger] of alanlar) {
        form.append(ad, deger)
      }
      const kayit = await api<ObsKayitSonucu>('/api/ice-aktarma/obs-ders-listesi/kaydet', {
        method: 'POST',
        body: form,
      })
      setSonuc(kayit)
      setOnizleme(null)
      setSecilen(new Set())
      onKaydedildi()
    } catch (e) {
      setHata(hataMetni(e))
    } finally {
      setMesgul(false)
    }
  }

  return (
    <div>
      <div className="ice-form">
        <label>
          Excel dosyası (OBS dönem ders listesi)
          <input
            type="file"
            accept=".xlsx"
            onChange={(olay) => {
              setDosya(olay.target.files?.[0] ?? null)
              girdiDegisti()
            }}
          />
        </label>
        <label>
          Dönem
          <select
            value={donem}
            onChange={(olay) => {
              setDonem(olay.target.value as Donem)
              girdiDegisti()
            }}
          >
            <option value="GUZ">Güz</option>
            <option value="BAHAR">Bahar</option>
          </select>
        </label>
        <label>
          Program
          <select
            value={etkinProgram ?? ''}
            onChange={(olay) => setProgramId(olay.target.value === '' ? null : Number(olay.target.value))}
          >
            <option value="">Seçiniz</option>
            {programListesi.map((p) => (
              <option key={p.id} value={p.id}>
                {p.ad} ({SEVIYE_ADLARI[p.seviye]}, {p.dil === 'TR' ? 'Türkçe' : 'İngilizce'})
              </option>
            ))}
          </select>
        </label>
        <button type="button" disabled={mesgul || !dosya} onClick={() => void onizle()}>
          {mesgul && !onizleme ? 'Okunuyor…' : 'Önizle'}
        </button>
      </div>

      {programlar.hata && (
        <p className="hata" role="alert">
          {programlar.hata}
        </p>
      )}
      {programlar.veri && programListesi.length === 0 && (
        <p className="hata" role="alert">
          Bu bölümde tanımlı program yok. Dersler programın sınıflarına bağlanır, önce bir program eklenmeli
          (süperadmin ya da fakülte sorumlusu ekleyebilir).
        </p>
      )}
      {hata && (
        <p className="hata" role="alert">
          {hata}
        </p>
      )}

      {sonuc && (
        <div className="bildirim basari" role="status">
          <strong>
            {sonuc.olusturulanDers} ders, {sonuc.olusturulanSube} şube ve {sonuc.olusturulanBlok} blok eklendi
          </strong>
          <div>
            {sonuc.yerlesimsizDers} yerleşimsiz, {sonuc.sanalDers} sanal, {sonuc.seminerDers} seminer ders,{' '}
            {sonuc.hocasizSube} hocasız şube
          </div>
          <div>
            {sonuc.olusturulanHocalar.length} yeni öğretim elemanı eklendi, {sonuc.eslesenHoca} mevcut
            öğretim elemanı eşleşti
          </div>
          {sonuc.olusturulanSiniflar.length > 0 && <div>Açılan sınıflar: {sonuc.olusturulanSiniflar.join(', ')}</div>}
          {sonuc.atlananlar.length > 0 && (
            <>
              <div>{sonuc.atlananlar.length} satır atlandı:</div>
              {sonuc.atlananlar.slice(0, 10).map((a) => (
                <div key={`${a.satir}-${a.kod}`}>
                  Satır {a.satir}, {a.kod}: {a.sebep}
                </div>
              ))}
              {sonuc.atlananlar.length > 10 && <div>… ve {sonuc.atlananlar.length - 10} satır daha</div>}
            </>
          )}
          <button type="button" className="kapat" aria-label="Kapat" onClick={() => setSonuc(null)}>
            ×
          </button>
        </div>
      )}

      {onizleme && (
        <>
          <div className="mini-ozet">
            <div>
              <strong>{onizleme.donemSatiri}</strong>
              {donem === 'GUZ' ? 'Güz' : 'Bahar'} satırı
            </div>
            <div>
              <strong>{onizleme.zorunluSayisi}</strong>
              Zorunlu ders
            </div>
            <div>
              <strong>{onizleme.secmeliGrupSayisi}</strong>
              Seçmeli grup
            </div>
            <div>
              <strong>{onizleme.secmeliDersSayisi + onizleme.grupsuzSecmeliSayisi}</strong>
              Seçmeli ders
            </div>
            <div>
              <strong>{onizleme.hocalar.length}</strong>
              Öğretim elemanı
            </div>
            <div>
              <strong>{onizleme.uyarilar.length}</strong>
              Uyarı
            </div>
          </div>
          {onizleme.digerDonemSatiri > 0 && (
            <p className="soluk">
              Dosyadaki diğer dönemin {onizleme.digerDonemSatiri} satırı yok sayılıyor.
            </p>
          )}

          {onizleme.uyarilar.length > 0 && (
            <details className="kart">
              <summary>{onizleme.uyarilar.length} uyarı</summary>
              <ul>
                {onizleme.uyarilar.map((u, i) => (
                  <li key={i}>
                    Satır {u.satir}, {u.kod}: {u.mesaj}
                  </li>
                ))}
              </ul>
            </details>
          )}

          <div className="ice-form">
            <label>
              Sanal sayılacak ders kodu önekleri
              <input value={sanalMetni} onChange={(olay) => setSanalMetni(olay.target.value)} />
            </label>
            <label>
              Seminer olarak açılacak ders kodları
              <input
                value={seminerMetni}
                onChange={(olay) => setSeminerMetni(olay.target.value)}
                placeholder="COM 401"
              />
            </label>
          </div>

          <h2>Zorunlu dersler</h2>
          <div className="kaydirma">
            <table>
              <thead>
                <tr>
                  <th>Sınıf</th>
                  <th>Kod</th>
                  <th>Ders</th>
                  <th>Öğretim elemanı</th>
                  <th>Saat</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {onizleme.zorunluDersler.map((d) => (
                  <tr key={d.satir}>
                    <td>{d.sinif}</td>
                    <td>{d.kod}</td>
                    <td>{d.ad}</td>
                    <td>{d.hocaAd ? `${d.hocaUnvan} ${d.hocaAd}`.trim() : <span className="soluk">Yok</span>}</td>
                    <td>{d.onerilenSaatIfadesi || '—'}</td>
                    <td>
                      <Etiketler ders={d} sanalOnekleri={sanalOnekleri} seminerKodlari={seminerKodlari} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <h2>Seçmeli dersler</h2>
          <div className="ice-form">
            <label>
              Ara (kod, ders adı ya da öğretim elemanı)
              <input value={arama} onChange={(olay) => setArama(olay.target.value)} placeholder="Örn. 101.11" />
            </label>
            <span className="soluk">{secilen.size} seçmeli ders seçili</span>
          </div>

          {onizleme.secmeliGruplar.map((grup) => {
            const gorunen = grup.dersler.filter((d) => aramaEslesir(d, arama))
            if (aramaVar && gorunen.length === 0) {
              return null
            }
            const durum = grupDurumu(grup, secilen)
            return (
              <details key={grup.kod} className="secmeli-grup" open={aramaVar ? true : undefined}>
                <summary>
                  {grup.kod} {grup.ad} · {grup.sinif}. sınıf · {durum.secili}/{durum.toplam} seçili
                </summary>
                <div className="secmeli-islemler">
                  <button type="button" className="ikincil" onClick={() => toplu(gorunen, true)}>
                    Görünenleri seç
                  </button>
                  <button type="button" className="ikincil" onClick={() => toplu(gorunen, false)}>
                    Görünenleri bırak
                  </button>
                </div>
                {gorunen.map((d) => (
                  <label key={d.satir} className="secmeli-ders">
                    <input type="checkbox" checked={secilen.has(d.kod)} onChange={() => sec(d.kod)} />
                    <span>
                      <strong>{d.kod}</strong> {d.ad}
                    </span>
                    <span className="soluk">
                      {d.onerilenSaatIfadesi || '—'}
                      {d.hocaAd ? ` · ${d.hocaAd}` : ''}
                    </span>
                  </label>
                ))}
              </details>
            )
          })}

          {onizleme.grupsuzSecmeliler.length > 0 && (
            <details className="secmeli-grup" open={aramaVar ? true : undefined}>
              <summary>Gruba bağlı olmayan seçmeliler ({onizleme.grupsuzSecmeliler.length})</summary>
              <div className="secmeli-islemler">
                <button
                  type="button"
                  className="ikincil"
                  onClick={() => toplu(onizleme.grupsuzSecmeliler.filter((d) => aramaEslesir(d, arama)), true)}
                >
                  Görünenleri seç
                </button>
                <button
                  type="button"
                  className="ikincil"
                  onClick={() => toplu(onizleme.grupsuzSecmeliler.filter((d) => aramaEslesir(d, arama)), false)}
                >
                  Görünenleri bırak
                </button>
              </div>
              {onizleme.grupsuzSecmeliler
                .filter((d) => aramaEslesir(d, arama))
                .map((d) => (
                  <label key={d.satir} className="secmeli-ders">
                    <input type="checkbox" checked={secilen.has(d.kod)} onChange={() => sec(d.kod)} />
                    <span>
                      <strong>{d.kod}</strong> {d.ad}
                    </span>
                    <span className="soluk">{d.etiket}</span>
                  </label>
                ))}
            </details>
          )}

          <p>
            <button type="button" disabled={mesgul || etkinProgram === null} onClick={() => void kaydet()}>
              {mesgul ? 'Kaydediliyor…' : `Kaydet (${onizleme.zorunluSayisi} zorunlu + ${secilen.size} seçmeli)`}
            </button>{' '}
            {etkinProgram === null && <span className="soluk">Kaydetmek için program seçin.</span>}
          </p>
        </>
      )}
    </div>
  )
}