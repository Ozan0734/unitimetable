import { describe, expect, it } from 'vitest'
import {
  derslikVarliklari,
  hocaVarliklari,
  hucreBasligi,
  hucreSatirlari,
  sinifVarliklari,
  tabloOlustur,
  varlikKutulari,
} from './programCikti'
import type { CiktiKutu, CiktiVerisi } from './programCikti'
import type { AkademikProgram, Derslik, Ders, OgretimElemani, Sinif, Sube, Yerlesim } from './tipler'

function ders(id: number, kod: string, ek: Partial<Ders> = {}): Ders {
  return {
    id,
    projeId: 1,
    bolumId: 1,
    kod,
    ad: `${kod} Dersi`,
    tur: 'NORMAL',
    sanal: false,
    secmeli: false,
    secmeliGrup: null,
    haftalikSaat: 3,
    saatIfadesi: '3',
    bloklar: [3],
    ...ek,
  }
}

function sube(id: number, dersId: number, hocaId: number | null, subeNo = 1): Sube {
  return {
    id,
    dersId,
    ogretimElemaniId: hocaId,
    subeNo,
    tahminiOgrenci: null,
    sinifCakismaIzni: false,
    derslikCakismaIzni: false,
    cokluDerslikIzni: false,
  }
}

function yerlesim(id: number, dersId: number, subeId: number, gun: number, bas: number, bit: number, derslikler: number[]): Yerlesim {
  return {
    id,
    blokId: id,
    subeId,
    dersId,
    gun,
    baslangicSlot: bas,
    bitisSlot: bit,
    sure: bit - bas + 1,
    derslikIdler: derslikler,
    kilitli: false,
  }
}

const hocalar: OgretimElemani[] = [
  { id: 5, bolumId: 1, adSoyad: 'ALİ VELİ', unvan: 'Prof. Dr.', renk: '#112233', oncelik: 0 },
  { id: 6, bolumId: 1, adSoyad: 'AYŞE DEMİR', unvan: null, renk: null, oncelik: 0 },
]
const derslikler: Derslik[] = [
  { id: 1, kurumId: 1, ad: 'MUHA-403', kapasite: 90, tur: 'SINIF' },
  { id: 2, kurumId: 1, ad: 'SANAL SINIF 1', kapasite: 150, tur: 'SANAL' },
]

const veri: CiktiVerisi = {
  dersler: [ders(1, 'COM 101'), ders(2, 'COM 201', { secmeli: true }), ders(3, 'ATA 101', { sanal: true })],
  subeler: [sube(1, 1, 5), sube(2, 2, 6, 2), sube(3, 3, null)],
  hocalar,
  derslikler,
  dersSiniflari: [
    { id: 1, dersId: 1, sinifId: 10 },
    { id: 2, dersId: 3, sinifId: 10 },
    { id: 3, dersId: 2, sinifId: 11 },
  ],
  yerlesimler: [
    yerlesim(1, 1, 1, 2, 1, 3, [1]),
    yerlesim(2, 2, 2, 1, 5, 6, [1]),
    yerlesim(3, 3, 3, 4, 1, 2, [2]),
    yerlesim(4, 1, 1, 6, 1, 2, [1]),
    yerlesim(5, 1, 1, 3, 8, 11, [1]),
  ],
  gunSayisi: 5,
  slotSayisi: 9,
}

describe('varlikKutulari', () => {
  it('sınıf görünümünde yalnızca o sınıfın derslerini gün ve saat sırasıyla verir', () => {
    const kutular = varlikKutulari('SINIF', 10, veri)
    expect(kutular.map((k) => k.kod)).toEqual(['COM 101', 'ATA 101'])
    expect(kutular[0]).toMatchObject({ gun: 2, baslangic: 1, bitis: 3, hoca: 'Prof. Dr. ALİ VELİ', derslik: 'MUHA-403', renk: '#112233' })
    expect(kutular[1].hoca).toBeNull()
    expect(kutular[1].renk).toBeNull()
    expect(kutular[1].sanal).toBe(true)
    expect(kutular[1].derslik).toBe('SANAL SINIF 1')
  })

  it('ızgaranın dışına taşan yerleşimleri atlar', () => {
    const kodlar = varlikKutulari('SINIF', 10, veri).map((k) => k.id)
    expect(kodlar).not.toContain(4)
    expect(kodlar).not.toContain(5)
  })

  it('hoca görünümünde yalnızca hocanın şubelerini verir', () => {
    const kutular = varlikKutulari('HOCA', 6, veri)
    expect(kutular).toHaveLength(1)
    expect(kutular[0].kod).toBe('COM 201')
    expect(kutular[0].subeNo).toBe(2)
    expect(kutular[0].secmeli).toBe(true)
    expect(kutular[0].renk).not.toBeNull()
  })

  it('derslik görünümünde yalnızca o dersliği kullanan blokları verir', () => {
    expect(varlikKutulari('DERSLIK', 2, veri).map((k) => k.kod)).toEqual(['ATA 101'])
    expect(varlikKutulari('DERSLIK', 1, veri).map((k) => k.id)).toEqual([2, 1])
    expect(varlikKutulari('DERSLIK', 99, veri)).toEqual([])
  })
})

function kutu(id: number, gun: number, baslangic: number, bitis: number): CiktiKutu {
  return {
    id,
    gun,
    baslangic,
    bitis,
    kod: `K${id}`,
    ad: 'Ders',
    hoca: null,
    derslik: 'A',
    subeNo: 1,
    secmeli: false,
    sanal: false,
    renk: null,
  }
}

function genislikToplami(satir: ReturnType<typeof tabloOlustur>[number]['satirlar'][number]): number {
  return satir.reduce((t, h) => t + (h.tur === 'kutu' ? h.genislik : 1), 0)
}

describe('tabloOlustur', () => {
  it('boş programda her gün tek satır ve slot sayısı kadar boş hücre verir', () => {
    const tablo = tabloOlustur([], 5, 9)
    expect(tablo).toHaveLength(5)
    for (const gun of tablo) {
      expect(gun.satirlar).toHaveLength(1)
      expect(gun.satirlar[0]).toHaveLength(9)
      expect(gun.satirlar[0].every((h) => h.tur === 'bos')).toBe(true)
    }
  })

  it('çok saatlik blok tek hücre olur ve satırın toplamı bozulmaz', () => {
    const tablo = tabloOlustur([kutu(1, 2, 2, 4)], 5, 9)
    const satir = tablo[1].satirlar[0]
    expect(satir).toHaveLength(7)
    expect(satir[0]).toEqual({ tur: 'bos' })
    expect(satir[1]).toMatchObject({ tur: 'kutu', genislik: 3 })
    expect(genislikToplami(satir)).toBe(9)
  })

  it('çakışan bloklar aynı günün ayrı satırlarına gider', () => {
    const tablo = tabloOlustur([kutu(1, 3, 1, 1), kutu(2, 3, 1, 1), kutu(3, 3, 5, 5)], 5, 9)
    expect(tablo[2].satirlar).toHaveLength(2)
    for (const satir of tablo[2].satirlar) {
      expect(genislikToplami(satir)).toBe(9)
    }
    const hucreler = tablo[2].satirlar.flat().filter((h) => h.tur === 'kutu')
    expect(hucreler).toHaveLength(3)
    expect(tablo[0].satirlar).toHaveLength(1)
  })

  it('bloğun ardışık saatlerinden sonra gelen blok doğru yere oturur', () => {
    const tablo = tabloOlustur([kutu(1, 1, 1, 2), kutu(2, 1, 3, 3)], 5, 9)
    const satir = tablo[0].satirlar[0]
    expect(satir.slice(0, 2).every((h) => h.tur === 'kutu')).toBe(true)
    expect(genislikToplami(satir)).toBe(9)
  })
})

describe('hucre yardımcıları', () => {
  const k: CiktiKutu = { ...kutu(1, 1, 1, 1), hoca: 'Prof. Dr. ALİ VELİ', ad: 'Fizik', derslik: 'A403' }

  it('görünüme göre gereksiz satırı çıkarır', () => {
    expect(hucreSatirlari(k, 'SINIF')).toEqual(['Fizik', 'Prof. Dr. ALİ VELİ', 'A403'])
    expect(hucreSatirlari(k, 'HOCA')).toEqual(['Fizik', 'A403'])
    expect(hucreSatirlari(k, 'DERSLIK')).toEqual(['Fizik', 'Prof. Dr. ALİ VELİ'])
    expect(hucreSatirlari({ ...k, hoca: null }, 'SINIF')[1]).toBe('Hoca atanmamış')
  })

  it('şube numarasını yalnızca 1. şubeden sonra gösterir', () => {
    expect(hucreBasligi(k)).toBe('K1')
    expect(hucreBasligi({ ...k, subeNo: 2 })).toBe('K1 · Şube 2')
  })
})

describe('varlık listeleri', () => {
  const programlar: AkademikProgram[] = [
    { id: 3, bolumId: 1, ad: 'Bilgisayar Yüksek Lisans', seviye: 'YUKSEK_LISANS', dil: 'TR' },
    { id: 2, bolumId: 1, ad: 'Bilgisayar Türkçe', seviye: 'LISANS', dil: 'TR' },
    { id: 1, bolumId: 1, ad: 'Bilgisayar İngilizce', seviye: 'LISANS', dil: 'EN' },
    { id: 4, bolumId: 1, ad: 'Sınıfsız Program', seviye: 'DOKTORA', dil: 'TR' },
  ]
  const siniflar: Sinif[] = [
    { id: 12, programId: 1, ad: '2. Sınıf', duzey: 2 },
    { id: 11, programId: 1, ad: '1. Sınıf', duzey: 1 },
    { id: 21, programId: 2, ad: '1. Sınıf', duzey: 1 },
    { id: 31, programId: 3, ad: '1. Sınıf', duzey: 1 },
  ]

  it('sınıfları lisans, yüksek lisans sırasıyla ve İngilizce önce olacak şekilde dizer', () => {
    const liste = sinifVarliklari(programlar, siniflar)
    expect(liste.map((v) => v.id)).toEqual([11, 12, 21, 31])
    expect(liste[0].baslik).toBe('Bilgisayar İngilizce · 1. Sınıf')
    expect(liste[0].altBaslik).toBe('Lisans, İngilizce')
    expect(liste[3].altBaslik).toBe('Yüksek lisans, Türkçe')
  })

  it('hocaları ve derslikleri Türkçe sıralamayla listeler', () => {
    expect(hocaVarliklari(hocalar).map((v) => v.baslik)).toEqual(['Prof. Dr. ALİ VELİ', 'AYŞE DEMİR'])
    const dl = derslikVarliklari(derslikler)
    expect(dl.map((v) => v.baslik)).toEqual(['MUHA-403', 'SANAL SINIF 1'])
    expect(dl[0].altBaslik).toBe('90 kişilik')
  })
})
