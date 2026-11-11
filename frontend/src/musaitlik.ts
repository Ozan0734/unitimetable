import type { MusaitlikDurumu, MusaitlikKaydi } from './hocaTipleri'

export type MusaitlikHaritasi = Map<string, MusaitlikDurumu>

export function hucreAnahtari(gun: number, slot: number): string {
  return `${gun}-${slot}`
}

function icerde(gun: number, slot: number, gunSayisi: number, slotSayisi: number): boolean {
  return gun >= 1 && gun <= gunSayisi && slot >= 1 && slot <= slotSayisi
}

/** Yalnızca zaman ızgarasının içindeki kayıtlardan hücre haritası kurar. */
export function haritaOlustur(
  kayitlar: MusaitlikKaydi[],
  gunSayisi: number,
  slotSayisi: number,
): MusaitlikHaritasi {
  const harita: MusaitlikHaritasi = new Map()
  for (const k of kayitlar) {
    if (icerde(k.gun, k.slot, gunSayisi, slotSayisi)) {
      harita.set(hucreAnahtari(k.gun, k.slot), k.durum)
    }
  }
  return harita
}

/** Izgaranın dışında kalan eski kayıtlar. Kaydederken olduğu gibi geri gönderilir, kaybolmaz. */
export function izgaraDisiKayitlar(
  kayitlar: MusaitlikKaydi[],
  gunSayisi: number,
  slotSayisi: number,
): MusaitlikKaydi[] {
  return kayitlar
    .filter((k) => !icerde(k.gun, k.slot, gunSayisi, slotSayisi))
    .map((k) => ({ gun: k.gun, slot: k.slot, durum: k.durum }))
}

/** Boş (fark etmez) -> müsait değil -> tercih -> boş. */
export function sonrakiDurum(durum: MusaitlikDurumu | undefined): MusaitlikDurumu | undefined {
  if (durum === undefined) {
    return 'MUSAIT_DEGIL'
  }
  if (durum === 'MUSAIT_DEGIL') {
    return 'TERCIH'
  }
  return undefined
}

export function hucreyiDegistir(harita: MusaitlikHaritasi, gun: number, slot: number): MusaitlikHaritasi {
  const yeni: MusaitlikHaritasi = new Map(harita)
  const anahtar = hucreAnahtari(gun, slot)
  const sonraki = sonrakiDurum(harita.get(anahtar))
  if (sonraki === undefined) {
    yeni.delete(anahtar)
  } else {
    yeni.set(anahtar, sonraki)
  }
  return yeni
}

/** Hücrelerin hepsi "müsait değil" ise hepsini boşaltır, değilse hepsini "müsait değil" yapar. */
function topluDegistir(harita: MusaitlikHaritasi, hucreler: string[]): MusaitlikHaritasi {
  const hepsiDegil = hucreler.every((h) => harita.get(h) === 'MUSAIT_DEGIL')
  const yeni: MusaitlikHaritasi = new Map(harita)
  for (const h of hucreler) {
    if (hepsiDegil) {
      yeni.delete(h)
    } else {
      yeni.set(h, 'MUSAIT_DEGIL')
    }
  }
  return yeni
}

export function gunuDegistir(harita: MusaitlikHaritasi, gun: number, slotSayisi: number): MusaitlikHaritasi {
  const hucreler = Array.from({ length: slotSayisi }, (_, i) => hucreAnahtari(gun, i + 1))
  return topluDegistir(harita, hucreler)
}

export function saatiDegistir(harita: MusaitlikHaritasi, slot: number, gunSayisi: number): MusaitlikHaritasi {
  const hucreler = Array.from({ length: gunSayisi }, (_, i) => hucreAnahtari(i + 1, slot))
  return topluDegistir(harita, hucreler)
}

/** Sunucuya gönderilecek liste: ızgaradaki hücreler ve ızgara dışındaki eski kayıtlar, gün ve saat sırasıyla. */
export function kayitlaraCevir(
  harita: MusaitlikHaritasi,
  gunSayisi: number,
  slotSayisi: number,
  disaridakiler: MusaitlikKaydi[],
): MusaitlikKaydi[] {
  const sonuc: MusaitlikKaydi[] = [...disaridakiler]
  for (let gun = 1; gun <= gunSayisi; gun++) {
    for (let slot = 1; slot <= slotSayisi; slot++) {
      const durum = harita.get(hucreAnahtari(gun, slot))
      if (durum !== undefined) {
        sonuc.push({ gun, slot, durum })
      }
    }
  }
  return sonuc.sort((a, b) => a.gun - b.gun || a.slot - b.slot)
}

export function ozet(harita: MusaitlikHaritasi): { musaitDegil: number; tercih: number } {
  let musaitDegil = 0
  let tercih = 0
  for (const durum of harita.values()) {
    if (durum === 'MUSAIT_DEGIL') {
      musaitDegil++
    } else {
      tercih++
    }
  }
  return { musaitDegil, tercih }
}

export function haritalarEsit(a: MusaitlikHaritasi, b: MusaitlikHaritasi): boolean {
  if (a.size !== b.size) {
    return false
  }
  for (const [anahtar, durum] of a) {
    if (b.get(anahtar) !== durum) {
      return false
    }
  }
  return true
}
