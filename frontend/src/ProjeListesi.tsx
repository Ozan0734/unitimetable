import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { api, hataMetni } from './api'
import { useOturum } from './oturum'
import { useYukle } from './useYukle'
import type { Bolum, ProgramProjesi, ProjeDurumu } from './tipler'

const DURUM_ADLARI: Record<ProjeDurumu, string> = { TASLAK: 'Taslak', YAYINDA: 'Yayında' }

export default function ProjeListesi() {
  const { kullanici } = useOturum()
  const projeler = useYukle<ProgramProjesi[]>('/api/program-projeleri')
  const bolumler = useYukle<Bolum[]>('/api/bolumler')
  const [ad, setAd] = useState('')
  const [donem, setDonem] = useState('')
  const [bolumId, setBolumId] = useState('')
  const [hata, setHata] = useState<string | null>(null)
  const [bekliyor, setBekliyor] = useState(false)

  if (!kullanici) {
    return null
  }

  const asistan = kullanici.rol === 'ASISTAN'
  const secilebilirBolumler = (bolumler.veri ?? []).filter(
    (b) => kullanici.rol === 'SUPERADMIN' || b.fakulteId === kullanici.fakulteId,
  )
  const bolumAdi = (id: number) => bolumler.veri?.find((b) => b.id === id)?.ad ?? `Bölüm ${id}`

  async function olustur(olay: FormEvent<HTMLFormElement>) {
    olay.preventDefault()
    if (!kullanici) {
      return
    }
    const hedefBolum = asistan ? kullanici.bolumId : Number(bolumId)
    if (!hedefBolum) {
      setHata('Bölüm seçilmelidir')
      return
    }
    setHata(null)
    setBekliyor(true)
    try {
      await api<ProgramProjesi>('/api/program-projeleri', {
        method: 'POST',
        body: JSON.stringify({
          kurumId: kullanici.kurumId,
          bolumId: hedefBolum,
          ad: ad.trim(),
          donem: donem.trim(),
        }),
      })
      setAd('')
      projeler.yenile()
    } catch (e) {
      setHata(hataMetni(e))
    } finally {
      setBekliyor(false)
    }
  }

  async function sil(proje: ProgramProjesi) {
    const onay = window.confirm(
      `"${proje.ad}" projesi silinecek. Projedeki tüm dersler, şubeler ve yerleşimler de silinir. Emin misiniz?`,
    )
    if (!onay) {
      return
    }
    try {
      await api<void>(`/api/program-projeleri/${proje.id}`, { method: 'DELETE' })
      projeler.yenile()
    } catch (e) {
      setHata(hataMetni(e))
    }
  }

  return (
    <>
      <section className="kart">
        <h2>Yeni proje</h2>
        <form className="form-satiri" onSubmit={(olay) => void olustur(olay)}>
          <label>
            Proje adı
            <input
              value={ad}
              onChange={(olay) => setAd(olay.target.value)}
              placeholder="Bilgisayar Mühendisliği Güz Programı"
              required
            />
          </label>
          <label>
            Dönem
            <input
              value={donem}
              onChange={(olay) => setDonem(olay.target.value)}
              placeholder="2026-2027 Güz"
              required
            />
          </label>
          {!asistan && (
            <label>
              Bölüm
              <select value={bolumId} onChange={(olay) => setBolumId(olay.target.value)} required>
                <option value="">Seçiniz</option>
                {secilebilirBolumler.map((b) => (
                  <option key={b.id} value={b.id}>
                    {b.ad}
                  </option>
                ))}
              </select>
            </label>
          )}
          <button type="submit" disabled={bekliyor}>
            {bekliyor ? 'Oluşturuluyor…' : 'Oluştur'}
          </button>
        </form>
        {hata && (
          <p className="hata" role="alert">
            {hata}
          </p>
        )}
      </section>

      <h2>Projeler</h2>
      {projeler.hata && (
        <p className="hata" role="alert">
          {projeler.hata}
        </p>
      )}
      {projeler.yukleniyor && <p className="soluk">Yükleniyor…</p>}
      {projeler.veri && (
        <table>
          <thead>
            <tr>
              <th>Proje</th>
              <th>Bölüm</th>
              <th>Dönem</th>
              <th>Durum</th>
              <th>Oluşturma</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {projeler.veri.length === 0 && (
              <tr>
                <td colSpan={6} className="soluk">
                  Henüz proje yok. Yukarıdan yeni bir proje oluşturabilirsiniz.
                </td>
              </tr>
            )}
            {projeler.veri.map((p) => (
              <tr key={p.id}>
                <td>
                  <Link to={`/projeler/${p.id}`}>{p.ad}</Link>
                </td>
                <td>{bolumAdi(p.bolumId)}</td>
                <td>{p.donem}</td>
                <td>
                  <span className={p.durum === 'YAYINDA' ? 'rozet yayinda' : 'rozet'}>
                    {DURUM_ADLARI[p.durum]}
                  </span>
                </td>
                <td>{new Date(p.olusturmaTarihi).toLocaleDateString('tr-TR')}</td>
                <td>
                  <button type="button" className="tehlike" onClick={() => void sil(p)}>
                    Sil
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  )
}