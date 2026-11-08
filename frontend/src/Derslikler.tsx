import { useState } from 'react'
import { api, hataMetni } from './api'
import { duzeltmeListesi, TUR_ADLARI, turSayilari } from './derslikAktarim'
import { useOturum } from './oturum'
import { useYukle } from './useYukle'
import type {
  Derslik,
  DerslikKarar,
  DerslikKararDurumu,
  DerslikKayitSonucu,
  DerslikOnizleme,
  DerslikTuru,
} from './tipler'

const TUR_SIRASI = Object.keys(TUR_ADLARI) as DerslikTuru[]
const DURUM_ADLARI: Record<DerslikKararDurumu, string> = {
  EKLENECEK: 'Eklenecek',
  ZATEN_VAR: 'Zaten var',
  PASIF: 'Pasif',
  GECERSIZ: 'Geçersiz',
}

function turMetni(sayilar: Record<string, number>): string {
  const parcalar = TUR_SIRASI.filter((t) => sayilar[t]).map(
    (t) => `${sayilar[t]} ${TUR_ADLARI[t].toLocaleLowerCase('tr')}`,
  )
  return parcalar.length > 0 ? parcalar.join(', ') : 'yok'
}

function dikkatGerekenler(kararlar: DerslikKarar[]): DerslikKarar[] {
  return kararlar.filter((k) => k.durum !== 'EKLENECEK' || k.duzeltildi)
}

function KararTablosu({ kararlar }: { kararlar: DerslikKarar[] }) {
  if (kararlar.length === 0) {
    return null
  }
  return (
    <div className="kaydirma">
      <table>
        <thead>
          <tr>
            <th>Satır</th>
            <th>Derslik</th>
            <th>Durum</th>
            <th>Açıklama</th>
          </tr>
        </thead>
        <tbody>
          {kararlar.map((k) => (
            <tr key={`${k.satir}-${k.ad}`}>
              <td>{k.satir}</td>
              <td>{k.ad}</td>
              <td>{DURUM_ADLARI[k.durum]}</td>
              <td>
                {k.duzeltildi && k.durum === 'EKLENECEK'
                  ? `Kontenjan ${k.dosyadakiKapasite} → ${k.kapasite} olarak düzeltilecek`
                  : (k.sebep ?? '')}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default function Derslikler() {
  const { kullanici } = useOturum()
  const kurumId = kullanici?.kurumId ?? null
  const liste = useYukle<Derslik[]>(kurumId !== null ? `/api/derslikler?kurumId=${kurumId}` : null)
  const [dosya, setDosya] = useState<File | null>(null)
  const [duzeltme, setDuzeltme] = useState('')
  const [onizleme, setOnizleme] = useState<DerslikOnizleme | null>(null)
  const [sonuc, setSonuc] = useState<DerslikKayitSonucu | null>(null)
  const [hata, setHata] = useState<string | null>(null)
  const [mesgul, setMesgul] = useState(false)

  if (!kullanici) {
    return null
  }
  const yonetebilir = kullanici.rol !== 'ASISTAN'

  const sirali = [...(liste.veri ?? [])].sort(
    (a, b) => TUR_SIRASI.indexOf(a.tur) - TUR_SIRASI.indexOf(b.tur) || a.ad.localeCompare(b.ad, 'tr'),
  )

  function formOlustur(): FormData | null {
    if (!dosya) {
      return null
    }
    const form = new FormData()
    form.append('dosya', dosya)
    for (const d of duzeltmeListesi(duzeltme)) {
      form.append('duzeltmeler', d)
    }
    return form
  }

  async function onizle() {
    const form = formOlustur()
    if (!form) {
      setHata('Önce bir Excel (.xlsx) dosyası seçin')
      return
    }
    setMesgul(true)
    setHata(null)
    setSonuc(null)
    try {
      setOnizleme(
        await api<DerslikOnizleme>('/api/ice-aktarma/derslik-listesi/onizleme', { method: 'POST', body: form }),
      )
    } catch (e) {
      setOnizleme(null)
      setHata(hataMetni(e))
    } finally {
      setMesgul(false)
    }
  }

  async function kaydet() {
    const form = formOlustur()
    if (!form || !onizleme) {
      return
    }
    if (!window.confirm(`${onizleme.eklenecek} derslik kuruma eklenecek. Devam edilsin mi?`)) {
      return
    }
    setMesgul(true)
    setHata(null)
    try {
      const kayit = await api<DerslikKayitSonucu>('/api/ice-aktarma/derslik-listesi/kaydet', {
        method: 'POST',
        body: form,
      })
      setSonuc(kayit)
      setOnizleme(null)
      liste.yenile()
    } catch (e) {
      setHata(hataMetni(e))
    } finally {
      setMesgul(false)
    }
  }

  return (
    <>
      <h2>Derslikler</h2>
      {liste.hata && (
        <p className="hata" role="alert">
          {liste.hata}
        </p>
      )}
      {liste.veri && (
        <div className="ozet">
          <div className="ozet-kutu">
            <strong>{liste.veri.length}</strong>
            <span>Toplam derslik</span>
          </div>
          {turSayilari(liste.veri).map(([tur, sayi]) => (
            <div key={tur} className="ozet-kutu">
              <strong>{sayi}</strong>
              <span>{TUR_ADLARI[tur]}</span>
            </div>
          ))}
        </div>
      )}
      {liste.veri && !liste.veri.some((d) => d.tur === 'SANAL') && (
        <p className="uyari-notu">
          Kurumda sanal sınıf tanımlı değil. Sanal olarak işaretli dersler (Atatürk İlkeleri, Türk Dili, İş
          Sağlığı ve Güvenliği gibi) yerleşebilmek için sanal sınıf gerektirir. Derslik listesini aşağıdan
          yükleyin.
        </p>
      )}

      {yonetebilir ? (
        <section className="kart">
          <h2>Excel'den derslik yükle</h2>
          <div className="ice-form">
            <label>
              Derslik listesi (.xlsx)
              <input
                type="file"
                accept=".xlsx"
                onChange={(olay) => {
                  setDosya(olay.target.files?.[0] ?? null)
                  setOnizleme(null)
                  setSonuc(null)
                  setHata(null)
                }}
              />
            </label>
            <label>
              Kontenjan düzeltmeleri (isteğe bağlı)
              <input
                value={duzeltme}
                onChange={(olay) => {
                  setDuzeltme(olay.target.value)
                  setOnizleme(null)
                }}
                placeholder="MUHA-103=100, MUHA-108=100, MUHD-403=90"
              />
            </label>
            <button type="button" disabled={mesgul || !dosya} onClick={() => void onizle()}>
              {mesgul && !onizleme ? 'Okunuyor…' : 'Önizle'}
            </button>
          </div>

          {hata && (
            <p className="hata" role="alert">
              {hata}
            </p>
          )}

          {sonuc && (
            <div className="bildirim basari" role="status">
              <strong>
                {sonuc.olusturulan} derslik eklendi ({turMetni(sonuc.olusturulanTurler)})
              </strong>
              {sonuc.atlananlar.length > 0 && <div>{sonuc.atlananlar.length} satır atlandı.</div>}
              {sonuc.uyarilar.map((u, i) => (
                <div key={i}>{u}</div>
              ))}
              <button type="button" className="kapat" aria-label="Kapat" onClick={() => setSonuc(null)}>
                ×
              </button>
            </div>
          )}

          {onizleme && (
            <>
              <div className="mini-ozet">
                <div>
                  <strong>{onizleme.toplamSatir}</strong>
                  Satır
                </div>
                <div>
                  <strong>{onizleme.eklenecek}</strong>
                  Eklenecek
                </div>
                <div>
                  <strong>{onizleme.zatenVar}</strong>
                  Zaten var
                </div>
                <div>
                  <strong>{onizleme.pasif}</strong>
                  Pasif
                </div>
                <div>
                  <strong>{onizleme.gecersiz}</strong>
                  Geçersiz
                </div>
              </div>
              <p>Eklenecek türler: {turMetni(onizleme.eklenecekTurler)}</p>
              {onizleme.uyarilar.map((u, i) => (
                <p key={i} className="uyari-notu">
                  {u}
                </p>
              ))}
              <KararTablosu kararlar={dikkatGerekenler(onizleme.dersliklar)} />
              <button type="button" disabled={mesgul || onizleme.eklenecek === 0} onClick={() => void kaydet()}>
                {mesgul ? 'Kaydediliyor…' : `Kaydet (${onizleme.eklenecek} derslik)`}
              </button>
            </>
          )}
        </section>
      ) : (
        <p className="soluk">Derslik listesini yalnızca süperadmin ve fakülte sorumlusu güncelleyebilir.</p>
      )}

      <div className="kaydirma buyuk">
        <table>
          <thead>
            <tr>
              <th>Derslik</th>
              <th>Tür</th>
              <th>Kontenjan</th>
            </tr>
          </thead>
          <tbody>
            {liste.veri && sirali.length === 0 && (
              <tr>
                <td colSpan={3} className="soluk">
                  Henüz derslik yok.
                </td>
              </tr>
            )}
            {sirali.map((d) => (
              <tr key={d.id}>
                <td>{d.ad}</td>
                <td>{TUR_ADLARI[d.tur]}</td>
                <td>{d.kapasite}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  )
}