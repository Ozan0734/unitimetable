import { Link, Navigate, Route, Routes } from 'react-router-dom'
import Giris from './Giris'
import ProjeDetay from './ProjeDetay'
import ProjeListesi from './ProjeListesi'
import { useOturum } from './oturum'
import type { Rol } from './tipler'

const ROL_ADLARI: Record<Rol, string> = {
  SUPERADMIN: 'Süperadmin',
  FAKULTE: 'Fakülte sorumlusu',
  ASISTAN: 'Bölüm sorumlusu asistan',
}

export default function App() {
  const { kullanici, yukleniyor, cikisYap } = useOturum()

  if (yukleniyor) {
    return <p className="ortala">Yükleniyor…</p>
  }
  if (!kullanici) {
    return <Giris />
  }

  return (
    <div className="sayfa">
      <header className="ust-cubuk">
        <strong>
          <Link to="/projeler">Ders Programı Planlama</Link>
        </strong>
        <span className="bosluk" />
        <span>
          {kullanici.adSoyad} · {ROL_ADLARI[kullanici.rol]}
        </span>
        <button type="button" className="ikincil" onClick={() => void cikisYap()}>
          Çıkış
        </button>
      </header>
      <main className="icerik">
        <Routes>
          <Route path="/" element={<Navigate to="/projeler" replace />} />
          <Route path="/projeler" element={<ProjeListesi />} />
          <Route path="/projeler/:projeId" element={<ProjeDetay />} />
          <Route path="*" element={<p>Sayfa bulunamadı.</p>} />
        </Routes>
      </main>
    </div>
  )
}