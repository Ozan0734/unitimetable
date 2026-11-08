import { describe, expect, it } from 'vitest'
import { dersleriSuz, hocasizDersler, yerlesikBlokSayisi } from './dersFiltre'
import type { Ders, Sube } from './tipler'

function ders(id: number, kod: string, ad: string): Ders {
  return {
    id,
    projeId: 1,
    bolumId: 1,
    kod,
    ad,
    tur: 'NORMAL',
    sanal: false,
    secmeli: false,
    secmeliGrup: null,
    haftalikSaat: 3,
    saatIfadesi: '3',
    bloklar: [3],
  }
}

function sube(id: number, dersId: number, hocaId: number | null): Sube {
  return {
    id,
    dersId,
    ogretimElemaniId: hocaId,
    subeNo: 1,
    tahminiOgrenci: null,
    sinifCakismaIzni: false,
    derslikCakismaIzni: false,
    cokluDerslikIzni: false,
  }
}

describe('dersleriSuz', () => {
  const dersler = [ders(1, 'COM 101', 'INTRODUCTION'), ders(2, 'ATA 101', 'ATATÜRK İLKELERİ')]

  it('aramada Türkçe karakter ve büyük küçük harf farkını yok sayar', () => {
    expect(dersleriSuz(dersler, [], 'ataturk', false).map((d) => d.id)).toEqual([2])
    expect(dersleriSuz(dersler, [], 'com', false).map((d) => d.id)).toEqual([1])
    expect(dersleriSuz(dersler, [], '', false)).toHaveLength(2)
  })

  it('hocasız filtresi yalnızca hocası eksik şubesi olan dersleri bırakır', () => {
    const subeler = [sube(1, 1, 5), sube(2, 2, null)]
    expect(dersleriSuz(dersler, subeler, '', true).map((d) => d.id)).toEqual([2])
    expect(dersleriSuz(dersler, subeler, '', false)).toHaveLength(2)
    expect(dersleriSuz(dersler, [], '', true)).toHaveLength(0)
    expect([...hocasizDersler(subeler)]).toEqual([2])
  })
})

describe('yerlesikBlokSayisi', () => {
  it('şubeye ait yerleşimleri sayar', () => {
    expect(yerlesikBlokSayisi(7, [{ subeId: 7 }, { subeId: 8 }, { subeId: 7 }])).toBe(2)
    expect(yerlesikBlokSayisi(9, [{ subeId: 7 }])).toBe(0)
  })
})