import { useState } from 'react'
import { api, hataMetni } from './api'
import type { MusaitlikKaydi } from './hocaTipleri'
import {
  gunuDegistir,
  haritaOlustur,
  haritalarEsit,
  hucreAnahtari,
  hucreyiDegistir,
  izgaraDisiKayitlar,
  kayitlaraCevir,
  ozet,
  saatiDegistir,
} from './musaitlik'
import type { MusaitlikHaritasi } from './musaitlik'
import type { ZamanIzgarasi } from './tipler'
import { useYukle } from './useYukle'
import { GUN_ADLARI, slotSaatleri } from './zaman'

const HUCRE_METNI = { MUSAIT_DEGIL: '✕', TERCIH: '★' } as const
const DURUM_ADLARI = { MUSAIT_DEGIL: 'müsait değil', TERCIH: 'tercih ediyor' } as const

interface EditorProps {
  hocaId: number
  izgara: ZamanIzgarasi
  baslangic: MusaitlikKaydi[]
}

function MusaitlikEditoru({ hocaId, izgara, baslangic }: EditorProps) {
  const gunSayisi = izgara.gunSayisi
  const slotSayisi = izgara.gunlukSlotSayisi
  const [disarida] = useState(() => izgaraDisiKayitlar(baslangic, gunSayisi, slotSayisi))
  const [kayitli, setKayitli] = useState<MusaitlikHaritasi>(() =>
    haritaOlustur(baslangic, gunSayisi, slotSayisi),
  )
  const [harita, setHarita] = useState<MusaitlikHaritasi>(kayitli)
  const [hata, setHata] = useState<string | null>(null)
  const [mesaj, setMesaj] = useState<string | null>(null)
  const [bekliyor, setBekliyor] = useState(false)

  const gunler = Array.from({ length: gunSayisi }, (_, i) => i + 1)
  const slotlar = Array.from({ length: slotSayisi }, (_, i) => i + 1)
  const toplam = ozet(harita)
  const degisti = !haritalarEsit(harita, kayitli)

  async function kaydet() {
    setBekliyor(true)
    setHata(null)
    setMesaj(null)
    try {
      const yanit = await api<MusaitlikKaydi[]>(`/api/ogretim-elemanlari/${hocaId}/musaitlik`, {
        method: 'PUT',
        body: JSON.stringify({ kayitlar: kayitlaraCevir(harita, gunSayisi, slotSayisi, disarida) }),
      })
      const yeni = haritaOlustur(yanit, gunSayisi, slotSayisi)
      setKayitli(yeni)
      setHarita(yeni)
      setMesaj('Müsaitlik kaydedildi')
    } catch (e) {
      setHata(hataMetni(e))
    } finally {
      setBekliyor(false)
    }
  }

  return (
    <div>
      <p className="soluk">
        Hücreye tıklayınca sırayla <strong>✕ müsait değil</strong> (bu saate blok yerleştirilemez),{' '}
        <strong>★ tercih</strong> (şimdilik yalnızca kaydedilir, planlamada henüz kullanılmaz) ve boş (fark etmez)
        olur. Gün ya da ders saati başlığına tıklamak tamamını müsait değil yapar, yeniden tıklamak temizler.
      </p>
      <div className="musaitlik-kap">
        <table className="musaitlik">
          <thead>
            <tr>
              <th />
              {gunler.map((gun) => (
                <th key={gun}>
                  <button
                    type="button"
                    className="m-baslik"
                    title="Günün tamamını müsait değil yap ya da temizle"
                    onClick={() => setHarita(gunuDegistir(harita, gun, slotSayisi))}
                  >
                    {GUN_ADLARI[gun - 1]}
                  </button>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {slotlar.map((slot) => (
              <tr key={slot}>
                <th>
                  <button
                    type="button"
                    className="m-baslik m-saat"
                    title="Bu ders saatini bütün günlerde müsait değil yap ya da temizle"
                    onClick={() => setHarita(saatiDegistir(harita, slot, gunSayisi))}
                  >
                    <strong>{slot}. ders</strong>
                    <span>{slotSaatleri(izgara, slot)}</span>
                  </button>
                </th>
                {gunler.map((gun) => {
                  const durum = harita.get(hucreAnahtari(gun, slot))
                  const sinif =
                    durum === 'MUSAIT_DEGIL' ? 'm-hucre degil' : durum === 'TERCIH' ? 'm-hucre tercih' : 'm-hucre'
                  return (
                    <td key={gun}>
                      <button
                        type="button"
                        className={sinif}
                        aria-label={`${GUN_ADLARI[gun - 1]} ${slot}. ders: ${durum ? DURUM_ADLARI[durum] : 'fark etmez'}`}
                        onClick={() => setHarita(hucreyiDegistir(harita, gun, slot))}
                      >
                        {durum ? HUCRE_METNI[durum] : ''}
                      </button>
                    </td>
                  )
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="soluk">
        {toplam.musaitDegil} saat müsait değil, {toplam.tercih} saat tercih.
        {disarida.length > 0 &&
          ` Zaman ızgarasının dışında ${disarida.length} eski kayıt var, kaydederken olduğu gibi korunur.`}
      </p>
      {hata && (
        <p className="hata" role="alert">
          {hata}
        </p>
      )}
      {mesaj && !degisti && <p className="basari-notu">{mesaj}</p>}
      <div className="sube-islemleri">
        <button type="button" disabled={bekliyor || !degisti} onClick={() => void kaydet()}>
          {bekliyor ? 'Kaydediliyor…' : 'Müsaitliği kaydet'}
        </button>
        <button type="button" className="ikincil" disabled={bekliyor || !degisti} onClick={() => setHarita(kayitli)}>
          Değişiklikleri geri al
        </button>
        <button
          type="button"
          className="ikincil"
          disabled={bekliyor || harita.size === 0}
          onClick={() => setHarita(new Map())}
        >
          Tümünü temizle
        </button>
      </div>
    </div>
  )
}

interface Props {
  hocaId: number
  izgara: ZamanIzgarasi
}

export default function HocaMusaitlik({ hocaId, izgara }: Props) {
  const kayitlar = useYukle<MusaitlikKaydi[]>(`/api/ogretim-elemanlari/${hocaId}/musaitlik`)
  if (kayitlar.hata) {
    return (
      <p className="hata" role="alert">
        {kayitlar.hata}
      </p>
    )
  }
  if (!kayitlar.veri) {
    return <p className="soluk">Yükleniyor…</p>
  }
  return <MusaitlikEditoru key={hocaId} hocaId={hocaId} izgara={izgara} baslangic={kayitlar.veri} />
}
