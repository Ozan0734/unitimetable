import { Link, useParams } from 'react-router-dom'
import ProgramGorunumu from './ProgramGorunumu'
import { useYukle } from './useYukle'
import type { Ders, DersTuru, ProgramProjesi, Yerlesim, YerlesmeyenBlok } from './tipler'

const TUR_ADLARI: Record<DersTuru, string> = {
  NORMAL: 'Normal',
  YERLESIMSIZ: 'Yerleşimsiz',
  SEMINER: 'Seminer',
}

export default function ProjeDetay() {
  const { projeId } = useParams()
  const kimlik = projeId ? encodeURIComponent(projeId) : null
  const proje = useYukle<ProgramProjesi>(kimlik ? `/api/program-projeleri/${kimlik}` : null)
  const acik = proje.veri !== undefined
  const dersler = useYukle<Ders[]>(acik ? `/api/dersler?projeId=${kimlik}` : null)
  const yerlesimler = useYukle<Yerlesim[]>(acik ? `/api/yerlesimler?projeId=${kimlik}` : null)
  const yerlesmeyenler = useYukle<YerlesmeyenBlok[]>(acik ? `/api/yerlesmeyen-bloklar?projeId=${kimlik}` : null)

  if (proje.hata) {
    return (
      <>
        <p className="hata" role="alert">
          {proje.hata}
        </p>
        <Link to="/projeler">← Projelere dön</Link>
      </>
    )
  }
  if (!proje.veri) {
    return <p className="soluk">Yükleniyor…</p>
  }

  const dersMap = new Map((dersler.veri ?? []).map((d) => [d.id, d]))

  return (
    <>
      <p>
        <Link to="/projeler">← Projelere dön</Link>
      </p>
      <h2>{proje.veri.ad}</h2>
      <p className="soluk">
        {proje.veri.donem} · {proje.veri.durum === 'YAYINDA' ? 'Yayında' : 'Taslak'}
      </p>

      <div className="ozet">
        <div className="ozet-kutu">
          <strong>{dersler.veri?.length ?? '…'}</strong>
          <span>Ders</span>
        </div>
        <div className="ozet-kutu">
          <strong>{yerlesimler.veri?.length ?? '…'}</strong>
          <span>Yerleşen blok</span>
        </div>
        <div className="ozet-kutu">
          <strong>{yerlesmeyenler.veri?.length ?? '…'}</strong>
          <span>Yerleşmeyen blok</span>
        </div>
      </div>

      {(dersler.hata ?? yerlesimler.hata ?? yerlesmeyenler.hata) && (
        <p className="hata" role="alert">
          {dersler.hata ?? yerlesimler.hata ?? yerlesmeyenler.hata}
        </p>
      )}

      {dersler.veri && yerlesimler.veri && (
        <>
          <h2>Haftalık program</h2>
          <ProgramGorunumu proje={proje.veri} dersler={dersler.veri} yerlesimler={yerlesimler.veri} />
        </>
      )}

      <h2>Yerleşmeyen bloklar</h2>
      <table>
        <thead>
          <tr>
            <th>Ders</th>
            <th>Şube</th>
            <th>Blok</th>
            <th>Süre</th>
          </tr>
        </thead>
        <tbody>
          {yerlesmeyenler.veri?.length === 0 && (
            <tr>
              <td colSpan={4} className="soluk">
                Yerleşmeyen blok yok.
              </td>
            </tr>
          )}
          {yerlesmeyenler.veri?.map((b) => (
            <tr key={b.blokId}>
              <td>
                {b.dersKodu} {b.dersAdi}
              </td>
              <td>{b.subeNo}</td>
              <td>{b.siraNo}</td>
              <td>{b.sure} saat</td>
            </tr>
          ))}
        </tbody>
      </table>

      <h2>Dersler</h2>
      <table>
        <thead>
          <tr>
            <th>Kod</th>
            <th>Ad</th>
            <th>Tür</th>
            <th>Haftalık saat</th>
            <th>Saat ifadesi</th>
          </tr>
        </thead>
        <tbody>
          {dersMap.size === 0 && dersler.veri && (
            <tr>
              <td colSpan={5} className="soluk">
                Bu projede henüz ders yok.
              </td>
            </tr>
          )}
          {dersler.veri?.map((d) => (
            <tr key={d.id}>
              <td>{d.kod}</td>
              <td>
                {d.ad} {d.sanal && <span className="rozet sanal">Sanal</span>}
              </td>
              <td>{TUR_ADLARI[d.tur]}</td>
              <td>{d.haftalikSaat ?? '—'}</td>
              <td>{d.saatIfadesi ?? '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  )
}