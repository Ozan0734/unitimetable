import { describe, expect, it } from 'vitest'
import { etkinRenk, hocaMetni, oneriRenk, PALET, renkGecerli, silmeOzeti } from './hocaYardimci'
import type { Ders, Sube } from './tipler'

function ders(id: number, kod: string): Ders {
  return {
    id,
    projeId: 1,
    bolumId: 1,
    kod,
    ad: kod,
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

describe('renk yardımcıları', () => {
  it('geçerli renk biçimini denetler', () => {
    expect(renkGecerli('#3B6EA8')).toBe(true)
    expect(renkGecerli('#3b6ea8')).toBe(true)
    expect(renkGecerli('3B6EA8')).toBe(false)
    expect(renkGecerli('#3B6EA')).toBe(false)
    expect(renkGecerli('kirmizi')).toBe(false)
  })

  it('rengi olmayan hoca için numaraya göre palet rengi verir', () => {
    expect(etkinRenk({ id: 3, renk: '#112233' })).toBe('#112233')
    expect(etkinRenk({ id: 3, renk: null })).toBe(PALET[3])
    expect(etkinRenk({ id: 13, renk: null })).toBe(PALET[3])
  })

  it('henüz kullanılmayan ilk palet rengini önerir', () => {
    expect(oneriRenk([])).toBe(PALET[0])
    expect(oneriRenk([PALET[0].toUpperCase(), null, PALET[1]])).toBe(PALET[2])
    expect(oneriRenk(PALET)).toBe(PALET[0])
  })
})

describe('hocaMetni', () => {
  it('unvan varsa başa ekler', () => {
    expect(hocaMetni({ adSoyad: 'ALİ VELİ', unvan: 'Prof. Dr.' })).toBe('Prof. Dr. ALİ VELİ')
    expect(hocaMetni({ adSoyad: 'ALİ VELİ', unvan: null })).toBe('ALİ VELİ')
  })
})

describe('silmeOzeti', () => {
  it('hocanın şube sayısını ve tekrarsız ders kodlarını verir', () => {
    const subeler = [sube(1, 10, 5), sube(2, 10, 5), sube(3, 11, 5), sube(4, 12, 6), sube(5, 13, null)]
    const dersler = [ders(10, 'COM 107'), ders(11, 'COM 101'), ders(12, 'COM 201'), ders(13, 'COM 301')]
    expect(silmeOzeti(5, subeler, dersler)).toEqual({ subeSayisi: 3, dersKodlari: ['COM 101', 'COM 107'] })
    expect(silmeOzeti(99, subeler, dersler)).toEqual({ subeSayisi: 0, dersKodlari: [] })
  })

  it('listede olmayan dersi atlar ama şubeyi sayar', () => {
    expect(silmeOzeti(5, [sube(1, 77, 5)], [])).toEqual({ subeSayisi: 1, dersKodlari: [] })
  })
})
