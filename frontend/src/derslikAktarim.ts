import type { DerslikTuru } from './tipler'

export const TUR_ADLARI: Record<DerslikTuru, string> = {
  SINIF: 'Sınıf',
  LABORATUVAR: 'Laboratuvar',
  AMFI: 'Amfi',
  SANAL: 'Sanal sınıf',
  SALON: 'Salon',
}

/** "MUHA-103=100, MUHA-108=100" gibi metni tek tek düzeltmelere ayırır. */
export function duzeltmeListesi(metin: string): string[] {
  return metin
    .split(/[,;\n]+/)
    .map((parca) => parca.trim())
    .filter((parca) => parca !== '')
}

/** Türlere göre derslik sayıları, TUR_ADLARI sırasıyla. Hiç olmayan türler listelenmez. */
export function turSayilari(derslikler: { tur: DerslikTuru }[]): [DerslikTuru, number][] {
  const sayac = new Map<DerslikTuru, number>()
  for (const d of derslikler) {
    sayac.set(d.tur, (sayac.get(d.tur) ?? 0) + 1)
  }
  return (Object.keys(TUR_ADLARI) as DerslikTuru[])
    .filter((tur) => sayac.has(tur))
    .map((tur) => [tur, sayac.get(tur) ?? 0])
}