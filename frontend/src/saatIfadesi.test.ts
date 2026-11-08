import { describe, expect, it } from 'vitest'
import { blokMetni, bloklariCoz, ifadeKontrol, ifadeyiSadelestir } from './saatIfadesi'

describe('bloklariCoz', () => {
  it('geçerli ifadeleri bloklara çevirir', () => {
    expect(bloklariCoz('3+2')).toEqual([3, 2])
    expect(bloklariCoz('2*2+1')).toEqual([2, 2, 1])
    expect(bloklariCoz('1*4')).toEqual([1, 1, 1, 1])
    expect(bloklariCoz(' 3 + 2 ')).toEqual([3, 2])
    expect(bloklariCoz('4')).toEqual([4])
  })

  it('geçersiz ifadelerde null döner', () => {
    for (const ifade of ['', '3+', '+3', 'a', '3*', '0', '2*0', '3++2', '3*2*2', '3-2', '1*99+1*99']) {
      expect(bloklariCoz(ifade), ifade).toBeNull()
    }
  })
})

describe('ifadeKontrol', () => {
  it('toplam haftalık saatle uyuşmazsa hata verir ama blokları yine de gösterir', () => {
    const sonuc = ifadeKontrol('4+4', 5)
    expect(sonuc.bloklar).toEqual([4, 4])
    expect(sonuc.hata).toContain('uyuşmuyor')
    expect(ifadeKontrol('3+2', 5)).toEqual({ bloklar: [3, 2], hata: null })
    expect(ifadeKontrol('3+', 5).hata).toContain('anlaşılamadı')
  })

  it('boş ifadede haftalık saat tek blok olur', () => {
    expect(ifadeKontrol('', 5)).toEqual({ bloklar: [5], hata: null })
    expect(ifadeKontrol('  ', null).hata).toContain('girilmelidir')
  })
})

describe('yardımcılar', () => {
  it('boşlukları atar ve blokları metne çevirir', () => {
    expect(ifadeyiSadelestir(' 2 * 2 + 1 ')).toBe('2*2+1')
    expect(blokMetni([2, 2, 1])).toBe('2 + 2 + 1')
  })
})