import type { Ihlal, UygunYer, YerDurumu } from './tipler'

export interface Hucre {
  gun: number
  baslangic: number
  derslikId: number
  durum: YerDurumu
  ihlaller: Ihlal[]
}

export interface Geometri {
  sol: number
  ust: number
  genislik: number
  baslikYuksekligi: number
  saatSutunu: number
  satirYuksekligi: number
  gunSayisi: number
  slotSayisi: number
}

const SIRA: Record<YerDurumu, number> = { UYGUN: 0, UYARILI: 1, ENGELLI: 2 }

export function anahtar(gun: number, baslangic: number): string {
  return `${gun}-${baslangic}`
}

function dahaIyi(aday: Hucre, mevcut: Hucre, tercihDerslikler: number[]): boolean {
  const fark = SIRA[aday.durum] - SIRA[mevcut.durum]
  if (fark !== 0) {
    return fark < 0
  }
  return tercihDerslikler.includes(aday.derslikId) && !tercihDerslikler.includes(mevcut.derslikId)
}

/**
 * Her başlangıç hücresi için en iyi derslik sonucunu seçer: önce uygun, sonra uyarılı, sonra engelli.
 * Eşitlikte tercih edilen derslik (bloğun şu anki dersliği) öne geçer.
 */
export function enIyiYerler(yerler: UygunYer[], tercihDerslikler: number[]): Map<string, Hucre> {
  const sonuc = new Map<string, Hucre>()
  for (const yer of yerler) {
    const k = anahtar(yer.gun, yer.baslangicSlot)
    const aday: Hucre = {
      gun: yer.gun,
      baslangic: yer.baslangicSlot,
      derslikId: yer.derslikId,
      durum: yer.durum,
      ihlaller: yer.ihlaller,
    }
    const mevcut = sonuc.get(k)
    if (!mevcut || dahaIyi(aday, mevcut, tercihDerslikler)) {
      sonuc.set(k, aday)
    }
  }
  return sonuc
}

/** Fare konumundan (gun, slot) hücresini bulur. Izgara dışındaysa null. */
export function hucreBul(x: number, y: number, g: Geometri): { gun: number; slot: number } | null {
  const ix = x - g.sol - g.saatSutunu
  const iy = y - g.ust - g.baslikYuksekligi
  if (ix < 0 || iy < 0) {
    return null
  }
  const gunGenisligi = (g.genislik - g.saatSutunu) / g.gunSayisi
  const gun = Math.floor(ix / gunGenisligi) + 1
  const slot = Math.floor(iy / g.satirYuksekligi) + 1
  if (gun > g.gunSayisi || slot > g.slotSayisi) {
    return null
  }
  return { gun, slot }
}

/** Fare altındaki slot ve tutulan satır farkından bloğun başlangıç saatini bulur. Sığmıyorsa null. */
export function baslangicSlotu(slot: number, ofset: number, sure: number, slotSayisi: number): number | null {
  const baslangic = slot - ofset
  if (baslangic < 1 || baslangic + sure - 1 > slotSayisi) {
    return null
  }
  return baslangic
}