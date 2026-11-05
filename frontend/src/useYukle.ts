import { useEffect, useState } from 'react'
import { api } from './api'

interface Sonuc<T> {
  yol: string
  veri?: T
  hata?: string
}

export interface YuklemeDurumu<T> {
  veri: T | undefined
  hata: string | undefined
  yukleniyor: boolean
  yenile: () => void
}

/** Verilen adresi çeker. Adres null ise bekler. yenile() aynı adresi yeniden çeker. */
export function useYukle<T>(yol: string | null): YuklemeDurumu<T> {
  const [sonuc, setSonuc] = useState<Sonuc<T> | null>(null)
  const [sayac, setSayac] = useState(0)

  useEffect(() => {
    if (yol === null) {
      return
    }
    let iptal = false
    api<T>(yol)
      .then((veri) => {
        if (!iptal) {
          setSonuc({ yol, veri })
        }
      })
      .catch((hata: unknown) => {
        if (!iptal) {
          setSonuc({ yol, hata: hata instanceof Error ? hata.message : 'Beklenmeyen hata' })
        }
      })
    return () => {
      iptal = true
    }
  }, [yol, sayac])

  const guncel = sonuc !== null && sonuc.yol === yol ? sonuc : null
  return {
    veri: guncel?.veri,
    hata: guncel?.hata,
    yukleniyor: yol !== null && guncel === null,
    yenile: () => setSayac((s) => s + 1),
  }
}