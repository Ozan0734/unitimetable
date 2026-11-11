import { useState } from 'react'
import { api, hataMetni } from './api'
import HocaMusaitlik from './HocaMusaitlik'
import HocaTercihleri from './HocaTercihleri'
import { etkinRenk, hocaMetni, oneriRenk, renkGecerli, silmeOzeti, UNVANLAR } from './hocaYardimci'
import type { Ders, OgretimElemani, Sube, ZamanIzgarasi } from './tipler'
import { useYukle } from './useYukle'

interface Props {
  hoca: OgretimElemani
  tumHocalar: OgretimElemani[]
  subeler: Sube[]
  dersler: Ders[]
  onDegisti: () => void
  kapat: () => void
}

export default function HocaDuzenle({ hoca, tumHocalar, subeler, dersler, onDegisti, kapat }: Props) {
  const izgara = useYukle<ZamanIzgarasi>('/api/zaman-izgarasi')
  const [adSoyad, setAdSoyad] = useState(hoca.adSoyad)
  const [unvan, setUnvan] = useState(hoca.unvan ?? '')
  const [renk, setRenk] = useState(hoca.renk ?? '')
  const [oncelik, setOncelik] = useState(String(hoca.oncelik))
  const [hata, setHata] = useState<string | null>(null)
  const [mesaj, setMesaj] = useState<string | null>(null)
  const [bekliyor, setBekliyor] = useState(false)

  const digerRenkler = tumHocalar.filter((h) => h.id !== hoca.id).map((h) => h.renk)
  const ozet = silmeOzeti(hoca.id, subeler, dersler)

  async function kaydet() {
    const oncelikSayisi = Number(oncelik)
    if (adSoyad.trim() === '') {
      setHata('Ad soyad boş olamaz')
      return
    }
    if (renk !== '' && !renkGecerli(renk)) {
      setHata('Renk #RRGGBB biçiminde olmalıdır')
      return
    }
    if (!Number.isInteger(oncelikSayisi) || oncelikSayisi < 0 || oncelikSayisi > 1000) {
      setHata('Öncelik 0 ile 1000 arasında tam sayı olmalıdır')
      return
    }
    setBekliyor(true)
    setHata(null)
    setMesaj(null)
    try {
      await api<OgretimElemani>(`/api/ogretim-elemanlari/${hoca.id}`, {
        method: 'PUT',
        body: JSON.stringify({
          adSoyad: adSoyad.trim(),
          unvan: unvan.trim() === '' ? null : unvan.trim(),
          renk: renk === '' ? null : renk,
          oncelik: oncelikSayisi,
        }),
      })
      setMesaj('Bilgiler kaydedildi')
      onDegisti()
    } catch (e) {
      setHata(hataMetni(e))
    } finally {
      setBekliyor(false)
    }
  }

  async function sil() {
    const sube =
      ozet.subeSayisi > 0
        ? `${ozet.subeSayisi} şubenin öğretim elemanı boşalacak (şubeler ve yerleşimler silinmez)` +
          (ozet.dersKodlari.length > 0 ? `: ${ozet.dersKodlari.join(', ')}` : '') +
          '. '
        : ''
    const metin = `"${hocaMetni(hoca)}" silinecek. ${sube}Müsaitlik ve gün tercihleri de silinir. Emin misiniz?`
    if (!window.confirm(metin)) {
      return
    }
    setBekliyor(true)
    setHata(null)
    try {
      await api<void>(`/api/ogretim-elemanlari/${hoca.id}`, { method: 'DELETE' })
      kapat()
      onDegisti()
    } catch (e) {
      setHata(hataMetni(e))
      setBekliyor(false)
    }
  }

  return (
    <section className="kart duzenle-paneli">
      <div className="panel-baslik">
        <h2>{hocaMetni(hoca)}</h2>
        <button type="button" className="ikincil" onClick={kapat}>
          Kapat
        </button>
      </div>

      <div className="ice-form">
        <label>
          Ad soyad
          <input value={adSoyad} maxLength={200} onChange={(olay) => setAdSoyad(olay.target.value)} />
        </label>
        <label>
          Unvan
          <input
            list="unvan-onerileri"
            value={unvan}
            maxLength={50}
            onChange={(olay) => setUnvan(olay.target.value)}
            placeholder="Örn. Doç. Dr."
          />
          <datalist id="unvan-onerileri">
            {UNVANLAR.map((u) => (
              <option key={u} value={u} />
            ))}
          </datalist>
        </label>
        <label>
          Öncelik (0-1000)
          <input
            type="number"
            min={0}
            max={1000}
            value={oncelik}
            onChange={(olay) => setOncelik(olay.target.value)}
          />
        </label>
      </div>

      <div className="renk-satiri">
        <span>Programdaki rengi:</span>
        <input
          type="color"
          aria-label="Renk"
          value={renk === '' ? etkinRenk(hoca) : renk}
          onChange={(olay) => setRenk(olay.target.value)}
        />
        <span className="soluk">{renk === '' ? 'Otomatik' : renk}</span>
        <button type="button" className="ikincil" onClick={() => setRenk(oneriRenk(digerRenkler))}>
          Renk önerisi
        </button>
        <button type="button" className="ikincil" disabled={renk === ''} onClick={() => setRenk('')}>
          Otomatik renge dön
        </button>
      </div>

      {hata && (
        <p className="hata" role="alert">
          {hata}
        </p>
      )}
      {mesaj && <p className="basari-notu">{mesaj}</p>}
      <div className="arac-cubugu">
        <button type="button" disabled={bekliyor} onClick={() => void kaydet()}>
          {bekliyor ? 'Kaydediliyor…' : 'Bilgileri kaydet'}
        </button>
        <span className="bosluk" />
        <button type="button" className="tehlike" disabled={bekliyor} onClick={() => void sil()}>
          Öğretim elemanını sil
        </button>
      </div>

      {izgara.hata && (
        <p className="hata" role="alert">
          {izgara.hata}
        </p>
      )}
      {izgara.veri && (
        <>
          <h3>Müsaitlik</h3>
          <HocaMusaitlik hocaId={hoca.id} izgara={izgara.veri} />
          <h3>Gün tercihleri</h3>
          <HocaTercihleri hocaId={hoca.id} izgara={izgara.veri} />
        </>
      )}
    </section>
  )
}
