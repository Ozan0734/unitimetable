import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import ObsIceAktar from './ObsIceAktar'
import ProgramGorunumu from './ProgramGorunumu'
import { useYukle } from './useYukle'
import type { Ders, DersTuru, IslemKaydi, ProgramProjesi, Yerlesim, YerlesmeyenBlok } from './tipler'

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
  const gecmis = useYukle<IslemKaydi[]>(acik ? `/api/program-projeleri/${kimlik}/gecmis` : null)
  const [surum, setSurum] = useState(0)

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

  const ilkHata = dersler.hata ?? yerlesimler.hata ?? yerlesmeyenler.hata ?? gecmis.hata

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

      {ilkHata && (
        <p className="hata" role="alert">
          {ilkHata}
        </p>
      )}

      <section className="kart">
        <details>
          <summary>OBS ders listesinden ders aktar</summary>
          <ObsIceAktar
            proje={proje.veri}
            onKaydedildi={() => {
              dersler.yenile()
              yerlesimler.yenile()
              yerlesmeyenler.yenile()
              gecmis.yenile()
              setSurum((s) => s + 1)
            }}
          />
        </details>
      </section>

      {dersler.veri && yerlesimler.veri && yerlesmeyenler.veri && gecmis.veri && (
        <>
          <h2>Haftalık program</h2>
          <ProgramGorunumu
            key={surum}
            proje={proje.veri}
            dersler={dersler.veri}
            yerlesimler={yerlesimler.veri}
            yerlesmeyenler={yerlesmeyenler.veri}
            gecmis={gecmis.veri}
            onDegisti={() => {
              yerlesimler.yenile()
              yerlesmeyenler.yenile()
              gecmis.yenile()
            }}
          />
        </>
      )}

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
                {d.ad} {d.sanal && <span className="rozet sanal">Sanal</span>}{' '}
                {d.secmeli && (
                  <span className="rozet secmeli">
                    Seçmeli{d.secmeliGrup ? ` · ${d.secmeliGrup}` : ''}
                  </span>
                )}
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