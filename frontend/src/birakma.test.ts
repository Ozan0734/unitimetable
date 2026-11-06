import { describe, expect, it } from 'vitest'
import { anahtar, baslangicSlotu, enIyiYerler, hucreBul } from './birakma'
import type { Geometri } from './birakma'
import type { UygunYer, YerDurumu } from './tipler'

function yer(gun: number, slot: number, derslikId: number, durum: YerDurumu): UygunYer {
  return { gun, baslangicSlot: slot, bitisSlot: slot, derslikId, durum, ihlaller: [] }
}

describe('enIyiYerler', () => {
  it('aynı hücrede en iyi durumu seçer', () => {
    const sonuc = enIyiYerler([yer(1, 1, 10, 'ENGELLI'), yer(1, 1, 11, 'UYARILI'), yer(1, 1, 12, 'UYGUN')], [])
    expect(sonuc.get(anahtar(1, 1))?.derslikId).toBe(12)
    expect(sonuc.get(anahtar(1, 1))?.durum).toBe('UYGUN')
  })

  it('eşitlikte tercih edilen derslik öne geçer', () => {
    const yerler = [yer(1, 2, 10, 'UYGUN'), yer(1, 2, 11, 'UYGUN')]
    expect(enIyiYerler(yerler, [11]).get(anahtar(1, 2))?.derslikId).toBe(11)
    expect(enIyiYerler(yerler, []).get(anahtar(1, 2))?.derslikId).toBe(10)
  })

  it('yalnızca engelli sonuç varsa engelli kalır', () => {
    const sonuc = enIyiYerler([yer(2, 3, 10, 'ENGELLI')], [])
    expect(sonuc.get(anahtar(2, 3))?.durum).toBe('ENGELLI')
  })

  it('farklı hücreler ayrı tutulur', () => {
    const sonuc = enIyiYerler([yer(1, 1, 10, 'UYGUN'), yer(1, 2, 10, 'UYARILI'), yer(2, 1, 10, 'UYGUN')], [])
    expect(sonuc.size).toBe(3)
  })
})

describe('hucreBul', () => {
  const g: Geometri = {
    sol: 100,
    ust: 50,
    genislik: 96 + 5 * 130,
    baslikYuksekligi: 40,
    saatSutunu: 96,
    satirYuksekligi: 64,
    gunSayisi: 5,
    slotSayisi: 9,
  }

  it('fare konumundan gün ve saati bulur', () => {
    expect(hucreBul(100 + 96 + 10, 50 + 40 + 10, g)).toEqual({ gun: 1, slot: 1 })
    expect(hucreBul(100 + 96 + 130 * 2 + 5, 50 + 40 + 64 * 3 + 1, g)).toEqual({ gun: 3, slot: 4 })
  })

  it('ızgara dışındaki konumlar null döner', () => {
    expect(hucreBul(150, 100, g)).toBeNull()
    expect(hucreBul(100 + 96 + 10, 60, g)).toBeNull()
    expect(hucreBul(100 + 746 + 1, 100, g)).toBeNull()
    expect(hucreBul(100 + 96 + 10, 50 + 40 + 64 * 9 + 1, g)).toBeNull()
  })
})

describe('baslangicSlotu', () => {
  it('tutulan satırı çıkararak başlangıcı bulur ve sığmayanı reddeder', () => {
    expect(baslangicSlotu(5, 1, 3, 9)).toBe(4)
    expect(baslangicSlotu(1, 1, 2, 9)).toBeNull()
    expect(baslangicSlotu(8, 0, 3, 9)).toBeNull()
    expect(baslangicSlotu(7, 0, 3, 9)).toBe(7)
  })
})