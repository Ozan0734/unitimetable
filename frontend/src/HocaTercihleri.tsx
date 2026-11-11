import { useState } from 'react'
import { api, hataMetni } from './api'
import {
  EN_AZ_BOS_GUN,
  EN_COK_BOS_GUN,
  formlarEsit,
  formuOlustur,
  gorunenGunler,
  gunSec,
  kayitlariOlustur,
} from './gunTercihi'
import type { GunSatiri, TercihFormu } from './gunTercihi'
import type { GunTercihi, TercihKatiligi } from './hocaTipleri'
import type { ZamanIzgarasi } from './tipler'
import { useYukle } from './useYukle'
import { GUN_ADLARI } from './zaman'

interface KatilikProps {
  deger: TercihKatiligi
  degisti: (katilik: TercihKatiligi) => void
  kapali?: boolean
  aciklama: string
}

function KatilikSecici({ deger, degisti, kapali, aciklama }: KatilikProps) {
  return (
    <select
      aria-label={aciklama}
      value={deger}
      disabled={kapali}
      onChange={(olay) => degisti(olay.target.value as TercihKatiligi)}
    >
      <option value="TERCIH">Tercih (aykırıysa uyarı)</option>
      <option value="ZORUNLU">Zorunlu (aykırıysa engel)</option>
    </select>
  )
}

interface EditorProps {
  hocaId: number
  izgara: ZamanIzgarasi
  baslangic: GunTercihi[]
}

function TercihEditoru({ hocaId, izgara, baslangic }: EditorProps) {
  const [kayitli, setKayitli] = useState<TercihFormu>(() => formuOlustur(baslangic))
  const [form, setForm] = useState<TercihFormu>(kayitli)
  const [hata, setHata] = useState<string | null>(null)
  const [mesaj, setMesaj] = useState<string | null>(null)
  const [bekliyor, setBekliyor] = useState(false)

  const gunler = gorunenGunler(form, izgara.gunSayisi)
  const degisti = !formlarEsit(form, kayitli)

  function katilikDegistir(satir: GunSatiri, katilik: TercihKatiligi) {
    setForm({ ...form, [satir]: { ...form[satir], katilik } })
  }

  async function kaydet() {
    setBekliyor(true)
    setHata(null)
    setMesaj(null)
    try {
      const yanit = await api<GunTercihi[]>(`/api/ogretim-elemanlari/${hocaId}/gun-tercihleri`, {
        method: 'PUT',
        body: JSON.stringify({ kayitlar: kayitlariOlustur(form) }),
      })
      const yeni = formuOlustur(yanit)
      setKayitli(yeni)
      setForm(yeni)
      setMesaj('Gün tercihleri kaydedildi')
    } catch (e) {
      setHata(hataMetni(e))
    } finally {
      setBekliyor(false)
    }
  }

  function gunSecimi(satir: GunSatiri, baslik: string) {
    return (
      <div className="tercih-bolumu">
        <strong>{baslik}</strong>
        <div className="gun-cipleri">
          {gunler.map((gun) => (
            <button
              key={gun}
              type="button"
              className="gun-cipi"
              aria-pressed={form[satir].gunler.includes(gun)}
              onClick={() => setForm(gunSec(form, satir, gun))}
            >
              {GUN_ADLARI[gun - 1]}
            </button>
          ))}
        </div>
        <KatilikSecici
          deger={form[satir].katilik}
          degisti={(k) => katilikDegistir(satir, k)}
          kapali={form[satir].gunler.length === 0}
          aciklama={`${baslik} katılığı`}
        />
      </div>
    )
  }

  return (
    <div>
      <div className="tercih-bolumu">
        <label className="onay-kutusu">
          <input
            type="checkbox"
            checked={form.bosGun.acik}
            onChange={(olay) => setForm({ ...form, bosGun: { ...form.bosGun, acik: olay.target.checked } })}
          />
          Haftada en az
        </label>
        <input
          type="number"
          className="kucuk-sayi"
          aria-label="Boş gün sayısı"
          min={EN_AZ_BOS_GUN}
          max={EN_COK_BOS_GUN}
          value={form.bosGun.deger}
          disabled={!form.bosGun.acik}
          onChange={(olay) => setForm({ ...form, bosGun: { ...form.bosGun, deger: Number(olay.target.value) } })}
        />
        <span>gün ders olmasın (boş gün)</span>
        <KatilikSecici
          deger={form.bosGun.katilik}
          degisti={(k) => setForm({ ...form, bosGun: { ...form.bosGun, katilik: k } })}
          kapali={!form.bosGun.acik}
          aciklama="Boş gün katılığı"
        />
      </div>

      <div className="tercih-bolumu">
        <label className="onay-kutusu">
          <input
            type="checkbox"
            checked={form.ardisik.acik}
            onChange={(olay) => setForm({ ...form, ardisik: { ...form.ardisik, acik: olay.target.checked } })}
          />
          Derslerini ardışık günlere yerleştir
        </label>
        <label className="onay-kutusu">
          <input
            type="checkbox"
            checked={form.ardisik.asistanaBirak}
            disabled={!form.ardisik.acik}
            onChange={(olay) =>
              setForm({ ...form, ardisik: { ...form.ardisik, asistanaBirak: olay.target.checked } })
            }
          />
          Günleri asistan belirlesin
        </label>
        <KatilikSecici
          deger={form.ardisik.katilik}
          degisti={(k) => setForm({ ...form, ardisik: { ...form.ardisik, katilik: k } })}
          kapali={!form.ardisik.acik}
          aciklama="Ardışık gün katılığı"
        />
      </div>

      {gunSecimi('tercihEdilen', 'Tercih ettiği günler')}
      {gunSecimi('kacinilan', 'Kaçındığı günler')}

      <p className="soluk">
        Tercih edilen ve kaçınılan günlerde zorunlu işaretlenen kural bir bloğu yerleştirirken{' '}
        <strong>engel</strong> olur, tercih olan yalnızca uyarı verir. Aynı gün hem tercih edilen hem
        kaçınılan olamaz. Boş gün sayısı ve ardışık gün tercihleri şimdilik yalnızca kaydedilir, otomatik
        planlamaya sonraki adımlarda bağlanacak.
      </p>
      {hata && (
        <p className="hata" role="alert">
          {hata}
        </p>
      )}
      {mesaj && !degisti && <p className="basari-notu">{mesaj}</p>}
      <div className="sube-islemleri">
        <button type="button" disabled={bekliyor || !degisti} onClick={() => void kaydet()}>
          {bekliyor ? 'Kaydediliyor…' : 'Tercihleri kaydet'}
        </button>
        <button type="button" className="ikincil" disabled={bekliyor || !degisti} onClick={() => setForm(kayitli)}>
          Değişiklikleri geri al
        </button>
      </div>
    </div>
  )
}

interface Props {
  hocaId: number
  izgara: ZamanIzgarasi
}

export default function HocaTercihleri({ hocaId, izgara }: Props) {
  const kayitlar = useYukle<GunTercihi[]>(`/api/ogretim-elemanlari/${hocaId}/gun-tercihleri`)
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
  return <TercihEditoru key={hocaId} hocaId={hocaId} izgara={izgara} baslangic={kayitlar.veri} />
}
