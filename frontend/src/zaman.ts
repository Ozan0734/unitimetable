import type { ZamanIzgarasi } from './tipler'

export const GUN_ADLARI = ['Pazartesi', 'Salı', 'Çarşamba', 'Perşembe', 'Cuma', 'Cumartesi', 'Pazar']

function saatMetni(dakika: number): string {
  const saat = Math.floor(dakika / 60) % 24
  const dk = dakika % 60
  return `${String(saat).padStart(2, '0')}:${String(dk).padStart(2, '0')}`
}

function slotAraligi(izgara: ZamanIzgarasi, slot: number): [number, number] {
  const [saat, dakika] = izgara.baslangicSaati.split(':').map(Number)
  const baslangic = saat * 60 + dakika + (slot - 1) * (izgara.dersSuresiDk + izgara.araSuresiDk)
  return [baslangic, baslangic + izgara.dersSuresiDk]
}

/** Tek bir ders saatinin aralığı. Örnek: 1. ders için "08:30–09:15". */
export function slotSaatleri(izgara: ZamanIzgarasi, slot: number): string {
  const [baslangic, bitis] = slotAraligi(izgara, slot)
  return `${saatMetni(baslangic)}–${saatMetni(bitis)}`
}

/** Ardışık ders saatlerinden oluşan bloğun aralığı. Örnek: 1-3. dersler için "08:30–11:15". */
export function aralikMetni(izgara: ZamanIzgarasi, ilkSlot: number, sonSlot: number): string {
  const [baslangic] = slotAraligi(izgara, ilkSlot)
  const [, bitis] = slotAraligi(izgara, sonSlot)
  return `${saatMetni(baslangic)}–${saatMetni(bitis)}`
}