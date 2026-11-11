import { describe, expect, it } from 'vitest'
import {
  gunuDegistir,
  haritaOlustur,
  haritalarEsit,
  hucreAnahtari,
  hucreyiDegistir,
  izgaraDisiKayitlar,
  kayitlaraCevir,
  ozet,
  saatiDegistir,
  sonrakiDurum,
} from './musaitlik'
import type { MusaitlikKaydi } from './hocaTipleri'

const GUN = 5
const SLOT = 9

describe('sonrakiDurum', () => {
  it('boş, müsait değil, tercih, boş sırasıyla döner', () => {
    expect(sonrakiDurum(undefined)).toBe('MUSAIT_DEGIL')
    expect(sonrakiDurum('MUSAIT_DEGIL')).toBe('TERCIH')
    expect(sonrakiDurum('TERCIH')).toBeUndefined()
  })
})

describe('hucreyiDegistir', () => {
  it('hücreyi sırayla değiştirir ve eski haritayı bozmaz', () => {
    const bos = haritaOlustur([], GUN, SLOT)
    const bir = hucreyiDegistir(bos, 2, 3)
    expect(bir.get(hucreAnahtari(2, 3))).toBe('MUSAIT_DEGIL')
    expect(bos.size).toBe(0)
    const iki = hucreyiDegistir(bir, 2, 3)
    expect(iki.get(hucreAnahtari(2, 3))).toBe('TERCIH')
    const uc = hucreyiDegistir(iki, 2, 3)
    expect(uc.has(hucreAnahtari(2, 3))).toBe(false)
  })
})

describe('gunuDegistir ve saatiDegistir', () => {
  it('günü tamamen müsait değil yapar, tekrar basınca temizler', () => {
    const harita = gunuDegistir(haritaOlustur([], GUN, SLOT), 3, SLOT)
    expect(harita.size).toBe(SLOT)
    expect(harita.get(hucreAnahtari(3, 9))).toBe('MUSAIT_DEGIL')
    expect(gunuDegistir(harita, 3, SLOT).size).toBe(0)
  })

  it('kısmen dolu günde önce hepsini müsait değil yapar (tercih hücreleri de)', () => {
    const baslangic = haritaOlustur([{ gun: 1, slot: 2, durum: 'TERCIH' }], GUN, SLOT)
    const harita = gunuDegistir(baslangic, 1, SLOT)
    expect(harita.size).toBe(SLOT)
    expect([...harita.values()].every((d) => d === 'MUSAIT_DEGIL')).toBe(true)
  })

  it('saati bütün günlerde değiştirir ve diğer hücrelere dokunmaz', () => {
    const baslangic = haritaOlustur([{ gun: 2, slot: 1, durum: 'TERCIH' }], GUN, SLOT)
    const harita = saatiDegistir(baslangic, 4, GUN)
    expect(harita.size).toBe(GUN + 1)
    expect(harita.get(hucreAnahtari(5, 4))).toBe('MUSAIT_DEGIL')
    expect(harita.get(hucreAnahtari(2, 1))).toBe('TERCIH')
    expect(saatiDegistir(harita, 4, GUN).size).toBe(1)
  })
})

describe('sunucuya gönderilecek liste', () => {
  const kayitlar: MusaitlikKaydi[] = [
    { gun: 5, slot: 5, durum: 'MUSAIT_DEGIL' },
    { gun: 1, slot: 2, durum: 'MUSAIT_DEGIL' },
    { gun: 1, slot: 1, durum: 'TERCIH' },
    { gun: 6, slot: 1, durum: 'MUSAIT_DEGIL' },
    { gun: 2, slot: 12, durum: 'TERCIH' },
  ]

  it('ızgara dışındaki kayıtlar haritaya girmez ama ayrıca korunur', () => {
    expect(haritaOlustur(kayitlar, GUN, SLOT).size).toBe(3)
    expect(izgaraDisiKayitlar(kayitlar, GUN, SLOT)).toEqual([
      { gun: 6, slot: 1, durum: 'MUSAIT_DEGIL' },
      { gun: 2, slot: 12, durum: 'TERCIH' },
    ])
  })

  it('değişiklik yapılmasa da liste aynı kayıtları gün ve saat sırasıyla verir', () => {
    const harita = haritaOlustur(kayitlar, GUN, SLOT)
    const gonderilecek = kayitlaraCevir(harita, GUN, SLOT, izgaraDisiKayitlar(kayitlar, GUN, SLOT))
    expect(gonderilecek.map((k) => `${k.gun}-${k.slot}`)).toEqual(['1-1', '1-2', '2-12', '5-5', '6-1'])
  })

  it('boşaltılan hücreler listeden çıkar, ızgara dışı kayıtlar kalır', () => {
    const harita = haritaOlustur(kayitlar, GUN, SLOT)
    const tercih = hucreyiDegistir(harita, 5, 5) // müsait değil -> tercih
    expect(tercih.get(hucreAnahtari(5, 5))).toBe('TERCIH')
    const bosaltilmis = hucreyiDegistir(tercih, 5, 5) // tercih -> boş
    const gonderilecek = kayitlaraCevir(bosaltilmis, GUN, SLOT, izgaraDisiKayitlar(kayitlar, GUN, SLOT))
    expect(gonderilecek.some((k) => k.gun === 5 && k.slot === 5)).toBe(false)
    expect(gonderilecek).toHaveLength(4)
  })
})

describe('ozet ve haritalarEsit', () => {
  it('müsait değil ve tercih sayılarını verir', () => {
    const harita = haritaOlustur(
      [
        { gun: 1, slot: 1, durum: 'MUSAIT_DEGIL' },
        { gun: 1, slot: 2, durum: 'MUSAIT_DEGIL' },
        { gun: 2, slot: 1, durum: 'TERCIH' },
      ],
      GUN,
      SLOT,
    )
    expect(ozet(harita)).toEqual({ musaitDegil: 2, tercih: 1 })
  })

  it('aynı içerikli haritaları eşit sayar', () => {
    const a = haritaOlustur([{ gun: 1, slot: 1, durum: 'TERCIH' }], GUN, SLOT)
    const b = haritaOlustur([{ gun: 1, slot: 1, durum: 'TERCIH' }], GUN, SLOT)
    const c = haritaOlustur([{ gun: 1, slot: 1, durum: 'MUSAIT_DEGIL' }], GUN, SLOT)
    expect(haritalarEsit(a, b)).toBe(true)
    expect(haritalarEsit(a, c)).toBe(false)
    expect(haritalarEsit(a, new Map())).toBe(false)
  })
})
