import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import DersYonetimi from './DersYonetimi'
import ObsIceAktar from './ObsIceAktar'
import ProgramGorunumu from './ProgramGorunumu'
import { useYukle } from './useYukle'
import type { Ders, IslemKaydi, ProgramProjesi, Yerlesim, YerlesmeyenBlok } from './tipler'

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

  const tumunuYenile = () => {
    dersler.yenile()
    yerlesimler.yenile()
    yerlesmeyenler.yenile()
    gecmis.yenile()
    setSurum((s) => s + 1)
  }

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
          <ObsIceAktar proje={proje.veri} onKaydedildi={tumunuYenile} />
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

      {dersler.veri && yerlesimler.veri && (
        <>
          <h2>Dersler ve şubeler</h2>
          <DersYonetimi
            proje={proje.veri}
            dersler={dersler.veri}
            yerlesimler={yerlesimler.veri}
            onDegisti={tumunuYenile}
          />
        </>
      )}
    </>
  )
}