import type { Ders, Sube } from './tipler'

export const PALET = [
  '#2b5fa6',
  '#c2410c',
  '#15803d',
  '#7e22ce',
  '#b45309',
  '#0e7490',
  '#be185d',
  '#4d7c0f',
  '#6d28d9',
  '#b91c1c',
]

export const UNVANLAR = [
  'Prof. Dr.',
  'Doç. Dr.',
  'Dr. Öğr. Üyesi',
  'Öğr. Gör. Dr.',
  'Öğr. Gör.',
  'Arş. Gör. Dr.',
  'Arş. Gör.',
]

export function renkGecerli(renk: string): boolean {
  return /^#[0-9A-Fa-f]{6}$/.test(renk)
}

/** Programda kutuların rengi: hocanın rengi, yoksa numarasına göre paletten. */
export function etkinRenk(hoca: { id: number; renk: string | null }): string {
  return hoca.renk ?? PALET[hoca.id % PALET.length]
}

/** Paletten henüz kullanılmayan ilk renk. Hepsi kullanılıyorsa paletin başına döner. */
export function oneriRenk(kullanilan: (string | null)[]): string {
  const kullanilanlar = new Set(
    kullanilan.filter((r): r is string => r !== null).map((r) => r.toLowerCase()),
  )
  const bos = PALET.find((r) => !kullanilanlar.has(r.toLowerCase()))
  return bos ?? PALET[kullanilanlar.size % PALET.length]
}

export function hocaMetni(hoca: { adSoyad: string; unvan: string | null }): string {
  return hoca.unvan ? `${hoca.unvan} ${hoca.adSoyad}` : hoca.adSoyad
}

/** Hoca silinirse hocasız kalacak şube sayısı ve bu şubelerin ders kodları. */
export function silmeOzeti(
  hocaId: number,
  subeler: Sube[],
  dersler: Ders[],
): { subeSayisi: number; dersKodlari: string[] } {
  const hocaninSubeleri = subeler.filter((s) => s.ogretimElemaniId === hocaId)
  const dersAdlari = new Map(dersler.map((d) => [d.id, d.kod]))
  const kodlar = new Set<string>()
  for (const s of hocaninSubeleri) {
    const kod = dersAdlari.get(s.dersId)
    if (kod !== undefined) {
      kodlar.add(kod)
    }
  }
  return {
    subeSayisi: hocaninSubeleri.length,
    dersKodlari: [...kodlar].sort((a, b) => a.localeCompare(b, 'tr')),
  }
}
