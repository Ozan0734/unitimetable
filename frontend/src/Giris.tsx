import { useState } from 'react'
import type { FormEvent } from 'react'
import { useOturum } from './oturum'

export default function Giris() {
  const { girisYap } = useOturum()
  const [eposta, setEposta] = useState('')
  const [sifre, setSifre] = useState('')
  const [hata, setHata] = useState<string | null>(null)
  const [bekliyor, setBekliyor] = useState(false)

  async function gonder(olay: FormEvent<HTMLFormElement>) {
    olay.preventDefault()
    setHata(null)
    setBekliyor(true)
    try {
      await girisYap(eposta, sifre)
    } catch (e) {
      setHata(e instanceof Error ? e.message : 'Giriş yapılamadı')
    } finally {
      setBekliyor(false)
    }
  }

  return (
    <div className="ortala">
      <form className="giris-kutusu" onSubmit={(olay) => void gonder(olay)}>
        <h1>Ders Programı Planlama</h1>
        <label>
          E-posta
          <input
            type="email"
            value={eposta}
            onChange={(olay) => setEposta(olay.target.value)}
            autoComplete="username"
            required
            autoFocus
          />
        </label>
        <label>
          Şifre
          <input
            type="password"
            value={sifre}
            onChange={(olay) => setSifre(olay.target.value)}
            autoComplete="current-password"
            required
          />
        </label>
        {hata && (
          <p className="hata" role="alert">
            {hata}
          </p>
        )}
        <button type="submit" disabled={bekliyor}>
          {bekliyor ? 'Giriş yapılıyor…' : 'Giriş yap'}
        </button>
      </form>
    </div>
  )
}