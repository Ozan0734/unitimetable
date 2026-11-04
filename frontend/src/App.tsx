import Giris from './Giris'
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
        <strong>Ders Programı Planlama</strong>
        <span className="bosluk" />
        <span>
          {kullanici.adSoyad} · {ROL_ADLARI[kullanici.rol]}
        </span>
        <button type="button" className="ikincil" onClick={() => void cikisYap()}>
          Çıkış
        </button>
      </header>
      <main className="icerik">
        <p>Giriş başarılı. Bir sonraki adımda proje listesi ve haftalık program ızgarası burada olacak.</p>
      </main>
    </div>
  )
}