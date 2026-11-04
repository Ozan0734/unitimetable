import { createContext, useContext } from 'react'
import type { Kullanici } from './tipler'

export interface OturumDegeri {
  kullanici: Kullanici | null
  yukleniyor: boolean
  girisYap: (eposta: string, sifre: string) => Promise<void>
  cikisYap: () => Promise<void>
}

export const OturumBaglami = createContext<OturumDegeri | null>(null)

export function useOturum(): OturumDegeri {
  const deger = useContext(OturumBaglami)
  if (!deger) {
    throw new Error('useOturum, OturumSaglayici içinde kullanılmalı')
  }
  return deger
}