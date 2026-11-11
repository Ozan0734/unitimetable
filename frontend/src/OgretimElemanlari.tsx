import { useState } from 'react'
import type { FormEvent } from 'react'
import { api, hataMetni } from './api'
import HocaDuzenle from './HocaDuzenle'
import './hoca.css'
import { etkinRenk, hocaMetni, oneriRenk, renkGecerli, UNVANLAR } from './hocaYardimci'
import { sadelestir } from './obsAktarim'
import { useOturum } from './oturum'
import type { Bolum, Ders, OgretimElemani, Sube } from './tipler'
import { useYukle } from './useYukle'

export default function OgretimElemanlari() {
  const { kullanici } = useOturum()
  const bolumler = useYukle<Bolum[]>('/api/bolumler')
  const [secilenBolum, setSecilenBolum] = useState<number | null>(null)
  const [seciliHoca, setSeciliHoca] = useState<number | null>(null)
  const [arama, setArama] = useState('')
  const [ad, setAd] = useState('')
  const [unvan, setUnvan] = useState('')
  const [renk, setRenk] = useState('')
  const [oncelik, setOncelik] = useState('0')
  const [hata, setHata] = useState<string | null>(null)
  const [bekliyor, setBekliyor] = useState(false)

  const asistan = kullanici?.rol === 'ASISTAN'
  const secilebilirBolumler = (bolumler.veri ?? []).filter(
    (b) => kullanici !== null && (kullanici.rol === 'SUPERADMIN' || b.fakulteId === kullanici.fakulteId),
  )
  const bolumId: number | null = asistan
    ? (kullanici?.bolumId ?? null)
    : secilenBolum !== null && secilebilirBolumler.some((b) => b.id === secilenBolum)
      ? secilenBolum
      : (secilebilirBolumler[0]?.id ?? null)

  const hocalar = useYukle<OgretimElemani[]>(
    bolumId !== null ? `/api/ogretim-elemanlari?bolumId=${bolumId}` : null,
  )
  const subeler = useYukle<Sube[]>('/api/subeler')
  const dersler = useYukle<Ders[]>('/api/dersler')

  if (!kullanici) {
    return null
  }

  const bolumAdi = bolumler.veri?.find((b) => b.id === bolumId)?.ad ?? ''
  const aranan = sadelestir(arama.trim())
  const liste = [...(hocalar.veri ?? [])]
    .sort((a, b) => a.adSoyad.localeCompare(b.adSoyad, 'tr'))
    .filter((h) => aranan === '' || sadelestir(`${h.unvan ?? ''} ${h.adSoyad}`).includes(aranan))
  const secili = hocalar.veri?.find((h) => h.id === seciliHoca)
  const subeSayisi = (hocaId: number) => subeler.veri?.filter((s) => s.ogretimElemaniId === hocaId).length

  async function olustur(olay: FormEvent<HTMLFormElement>) {
    olay.preventDefault()
    if (bolumId === null) {
      setHata('Bölüm seçilmelidir')
      return
    }
    const oncelikSayisi = Number(oncelik)
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
    try {
      const yeni = await api<OgretimElemani>('/api/ogretim-elemanlari', {
        method: 'POST',
        body: JSON.stringify({
          bolumId,
          adSoyad: ad.trim(),
          unvan: unvan.trim() === '' ? null : unvan.trim(),
          renk: renk === '' ? null : renk,
          oncelik: oncelikSayisi,
        }),
      })
      setAd('')
      setUnvan('')
      setRenk('')
      hocalar.yenile()
      setSeciliHoca(yeni.id)
    } catch (e) {
      setHata(hataMetni(e))
    } finally {
      setBekliyor(false)
    }
  }

  return (
    <>
      <h2>Öğretim elemanları</h2>

      {!asistan && (
        <div className="ice-form">
          <label>
            Bölüm
            <select
              value={bolumId ?? ''}
              onChange={(olay) => {
                setSecilenBolum(Number(olay.target.value))
                setSeciliHoca(null)
              }}
            >
              {secilebilirBolumler.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.ad}
                </option>
              ))}
            </select>
          </label>
        </div>
      )}
      {bolumler.hata && (
        <p className="hata" role="alert">
          {bolumler.hata}
        </p>
      )}
      {asistan && bolumAdi !== '' && <p className="soluk">{bolumAdi}</p>}

      {secili && (
        <HocaDuzenle
          key={secili.id}
          hoca={secili}
          tumHocalar={hocalar.veri ?? []}
          subeler={subeler.veri ?? []}
          dersler={dersler.veri ?? []}
          onDegisti={() => {
            hocalar.yenile()
            subeler.yenile()
          }}
          kapat={() => setSeciliHoca(null)}
        />
      )}

      <section className="kart">
        <h2>Yeni öğretim elemanı</h2>
        <form className="form-satiri" onSubmit={(olay) => void olustur(olay)}>
          <label>
            Ad soyad
            <input value={ad} maxLength={200} onChange={(olay) => setAd(olay.target.value)} required />
          </label>
          <label>
            Unvan
            <input
              list="unvan-onerileri-yeni"
              value={unvan}
              maxLength={50}
              onChange={(olay) => setUnvan(olay.target.value)}
              placeholder="Örn. Doç. Dr."
            />
            <datalist id="unvan-onerileri-yeni">
              {UNVANLAR.map((u) => (
                <option key={u} value={u} />
              ))}
            </datalist>
          </label>
          <label>
            Öncelik
            <input
              type="number"
              min={0}
              max={1000}
              value={oncelik}
              onChange={(olay) => setOncelik(olay.target.value)}
            />
          </label>
          <label>
            Renk
            <span className="renk-secimi">
              <input
                type="color"
                aria-label="Renk"
                value={renk === '' ? '#2b5fa6' : renk}
                onChange={(olay) => setRenk(olay.target.value)}
              />
              <button
                type="button"
                className="ikincil"
                onClick={() => setRenk(oneriRenk((hocalar.veri ?? []).map((h) => h.renk)))}
              >
                Öneri
              </button>
            </span>
          </label>
          <button type="submit" disabled={bekliyor || bolumId === null}>
            {bekliyor ? 'Ekleniyor…' : 'Ekle'}
          </button>
        </form>
        <p className="soluk">Renk seçilmezse programda her hocaya otomatik bir renk verilir.</p>
        {hata && (
          <p className="hata" role="alert">
            {hata}
          </p>
        )}
      </section>

      <div className="ice-form">
        <label>
          Ara
          <input value={arama} onChange={(olay) => setArama(olay.target.value)} placeholder="Ad ya da unvan" />
        </label>
        <span className="soluk">
          {hocalar.veri ? `${liste.length} / ${hocalar.veri.length} öğretim elemanı` : ''}
        </span>
      </div>
      {hocalar.hata && (
        <p className="hata" role="alert">
          {hocalar.hata}
        </p>
      )}
      <div className="kaydirma buyuk">
        <table>
          <thead>
            <tr>
              <th>Renk</th>
              <th>Öğretim elemanı</th>
              <th>Öncelik</th>
              <th>Şube</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {hocalar.veri && hocalar.veri.length === 0 && (
              <tr>
                <td colSpan={5} className="soluk">
                  Bu bölümde henüz öğretim elemanı yok. Yukarıdan ekleyebilir ya da bir projede OBS ders
                  listesini aktararak otomatik oluşturabilirsiniz.
                </td>
              </tr>
            )}
            {hocalar.veri && hocalar.veri.length > 0 && liste.length === 0 && (
              <tr>
                <td colSpan={5} className="soluk">
                  Eşleşen öğretim elemanı yok.
                </td>
              </tr>
            )}
            {liste.map((h) => (
              <tr key={h.id} className={h.id === seciliHoca ? 'secili-satir' : undefined}>
                <td>
                  <span
                    className="renk-nokta"
                    style={{ background: etkinRenk(h) }}
                    title={h.renk === null ? 'Otomatik renk' : h.renk}
                  />
                </td>
                <td>{hocaMetni(h)}</td>
                <td>{h.oncelik}</td>
                <td>{subeSayisi(h.id) ?? '…'}</td>
                <td>
                  <button type="button" className="ikincil" onClick={() => setSeciliHoca(h.id)}>
                    Düzenle
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  )
}
