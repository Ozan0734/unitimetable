import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { api, ApiHatasi } from './api'
import { OturumBaglami } from './oturum'
import type { OturumDegeri } from './oturum'
import type { Kullanici } from './tipler'

export function OturumSaglayici({ children }: { children: ReactNode }) {
  const [kullanici, setKullanici] = useState<Kullanici | null>(null)
  const [yukleniyor, setYukleniyor] = useState(true)

  useEffect(() => {
    let iptal = false
    api<Kullanici>('/api/oturum')
      .then((k) => {
        if (!iptal) {
          setKullanici(k)
        }
      })
      .catch((hata: unknown) => {
        if (!(hata instanceof ApiHatasi && hata.durum === 401)) {
          console.error(hata)
        }
      })
      .finally(() => {
        if (!iptal) {
          setYukleniyor(false)
        }
      })
    return () => {
      iptal = true
    }
  }, [])

  const girisYap = useCallback(async (eposta: string, sifre: string) => {
    const k = await api<Kullanici>('/api/giris', {
      method: 'POST',
      body: JSON.stringify({ eposta, sifre }),
    })
    setKullanici(k)
  }, [])

  const cikisYap = useCallback(async () => {
    await api<void>('/api/cikis', { method: 'POST' })
    setKullanici(null)
  }, [])

  const deger = useMemo<OturumDegeri>(
    () => ({ kullanici, yukleniyor, girisYap, cikisYap }),
    [kullanici, yukleniyor, girisYap, cikisYap],
  )

  return <OturumBaglami.Provider value={deger}>{children}</OturumBaglami.Provider>
}