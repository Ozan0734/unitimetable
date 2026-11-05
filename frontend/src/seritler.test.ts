import { describe, expect, it } from 'vitest'
import { seritleriBul } from './seritler'

describe('seritleriBul', () => {
  it('çakışmayan bloklar tek şeritte kalır', () => {
    const sonuc = seritleriBul([
      { id: 1, gun: 1, baslangic: 1, bitis: 3 },
      { id: 2, gun: 1, baslangic: 4, bitis: 5 },
    ])
    expect(sonuc.get(1)).toEqual({ serit: 0, toplam: 1 })
    expect(sonuc.get(2)).toEqual({ serit: 0, toplam: 1 })
  })

  it('çakışan bloklar yan yana yerleşir', () => {
    const sonuc = seritleriBul([
      { id: 1, gun: 2, baslangic: 1, bitis: 3 },
      { id: 2, gun: 2, baslangic: 2, bitis: 4 },
    ])
    expect(sonuc.get(1)).toEqual({ serit: 0, toplam: 2 })
    expect(sonuc.get(2)).toEqual({ serit: 1, toplam: 2 })
  })

  it('uçları paylaşan bloklar çakışmış sayılır', () => {
    const sonuc = seritleriBul([
      { id: 1, gun: 1, baslangic: 1, bitis: 3 },
      { id: 2, gun: 1, baslangic: 3, bitis: 4 },
    ])
    expect(sonuc.get(1)).toEqual({ serit: 0, toplam: 2 })
    expect(sonuc.get(2)).toEqual({ serit: 1, toplam: 2 })
  })

  it('farklı günler birbirini etkilemez', () => {
    const sonuc = seritleriBul([
      { id: 1, gun: 1, baslangic: 1, bitis: 3 },
      { id: 2, gun: 2, baslangic: 1, bitis: 3 },
    ])
    expect(sonuc.get(1)).toEqual({ serit: 0, toplam: 1 })
    expect(sonuc.get(2)).toEqual({ serit: 0, toplam: 1 })
  })

  it('biten bloğun şeridi yeniden kullanılır', () => {
    const sonuc = seritleriBul([
      { id: 1, gun: 1, baslangic: 1, bitis: 2 },
      { id: 2, gun: 1, baslangic: 1, bitis: 4 },
      { id: 3, gun: 1, baslangic: 3, bitis: 4 },
    ])
    expect(sonuc.get(1)).toEqual({ serit: 0, toplam: 2 })
    expect(sonuc.get(2)).toEqual({ serit: 1, toplam: 2 })
    expect(sonuc.get(3)).toEqual({ serit: 0, toplam: 2 })
  })

  it('ayrı kümelerde şerit sayısı ayrı hesaplanır', () => {
    const sonuc = seritleriBul([
      { id: 1, gun: 1, baslangic: 1, bitis: 2 },
      { id: 2, gun: 1, baslangic: 1, bitis: 2 },
      { id: 3, gun: 1, baslangic: 5, bitis: 6 },
    ])
    expect(sonuc.get(1)?.toplam).toBe(2)
    expect(sonuc.get(3)).toEqual({ serit: 0, toplam: 1 })
  })
})