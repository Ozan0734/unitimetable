import { Link, useParams } from 'react-router-dom'
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

  const yerlesen = yerlesimler.veri?.length
  const bekleyen = yerlesmeyenler.veri?.length

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
          <strong>{yerlesen ?? '…'}</strong>
          <span>Yerleşen blok</span>
        </div>
        <div className="ozet-kutu">
          <strong>{bekleyen ?? '…'}</strong>
          <span>Yerleşmeyen blok</span>
        </div>
      </div>

      {(dersler.hata ?? yerlesimler.hata ?? yerlesmeyenler.hata) && (
        <p className="hata" role="alert">
          {dersler.hata ?? yerlesimler.hata ?? yerlesmeyenler.hata}
        </p>
      )}

      <p className="soluk">Haftalık program ızgarası bir sonraki adımda burada olacak.</p>

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
          {dersler.veri?.length === 0 && (
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