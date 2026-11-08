import { describe, expect, it } from 'vitest'
import { duzeltmeListesi, turSayilari } from './derslikAktarim'

describe('duzeltmeListesi', () => {
  it('virgül, noktalı virgül ve satır sonuyla ayırır', () => {
    expect(duzeltmeListesi('MUHA-103=100, MUHA-108=100;\nMUHD-403=90')).toEqual([
      'MUHA-103=100',
      'MUHA-108=100',
      'MUHD-403=90',
    ])
  })

  it('boş metinde boş liste döner', () => {
    expect(duzeltmeListesi('')).toEqual([])
    expect(duzeltmeListesi('  , ;\n ')).toEqual([])
  })
})

describe('turSayilari', () => {
  it('türleri sabit sırayla sayar ve olmayanları atlar', () => {
    const sonuc = turSayilari([
      { tur: 'SALON' },
      { tur: 'SANAL' },
      { tur: 'SINIF' },
      { tur: 'SANAL' },
      { tur: 'SINIF' },
      { tur: 'SINIF' },
    ])
    expect(sonuc).toEqual([
      ['SINIF', 3],
      ['SANAL', 2],
      ['SALON', 1],
    ])
  })
})