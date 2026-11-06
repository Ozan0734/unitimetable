import { describe, expect, it } from 'vitest'
import { gecmisDurumu, planOzeti } from './gecmis'
import type { IslemKaydi, PlanSonucu, PlanYerlesmeyen } from './tipler'

function kayit(id: number, geriAlindi: boolean): IslemKaydi {
  return { id, blokId: 1, tur: 'TASI', onceki: null, sonraki: null, geriAlindi, tarih: '2026-11-06T10:00:00' }
}

function yerlesmeyen(blokId: number): PlanYerlesmeyen {
  return { blokId, dersKodu: 'COM 101', subeNo: 1, siraNo: 2, sure: 3, sebep: 'ENGEL HOCA_CAKISMA (12)' }
}

function sonuc(yerlesen: number, yerlesmeyenler: PlanYerlesmeyen[], sureDoldu = false, gecenMs = 2000): PlanSonucu {
  return {
    yerlesenSayisi: yerlesen,
    yerlesmeyenSayisi: yerlesmeyenler.length,
    sureDoldu,
    gecenMs,
    yerlesenler: [],
    yerlesmeyenler,
  }
}

describe('gecmisDurumu', () => {
  it('geçmiş boşsa ne geri alınır ne yinelenir', () => {
    const durum = gecmisDurumu([])
    expect(durum.sonGeriAlinabilir).toBeNull()
    expect(durum.sonYinelenebilir).toBeNull()
  })

  it('hiçbir işlem geri alınmamışsa yalnızca geri alınabilir', () => {
    const durum = gecmisDurumu([kayit(2, false), kayit(1, false), kayit(3, false)])
    expect(durum.sonGeriAlinabilir?.id).toBe(3)
    expect(durum.sonYinelenebilir).toBeNull()
  })

  it('geri alınanlardan en eskisi yinelenir, kalanlardan en yenisi geri alınır', () => {
    const durum = gecmisDurumu([kayit(3, true), kayit(1, false), kayit(2, true)])
    expect(durum.sonGeriAlinabilir?.id).toBe(1)
    expect(durum.sonYinelenebilir?.id).toBe(2)
  })

  it('hepsi geri alınmışsa geri alınacak işlem kalmaz', () => {
    const durum = gecmisDurumu([kayit(1, true), kayit(2, true)])
    expect(durum.sonGeriAlinabilir).toBeNull()
    expect(durum.sonYinelenebilir?.id).toBe(1)
  })
})

describe('planOzeti', () => {
  it('yerleştirilecek blok yoksa bunu söyler', () => {
    const ozet = planOzeti(sonuc(0, []))
    expect(ozet.tur).toBe('basari')
    expect(ozet.baslik).toBe('Yerleştirilecek blok yok')
  })

  it('hepsi yerleşince süreyi saniye olarak gösterir', () => {
    const ozet = planOzeti(sonuc(3, [], false, 2000))
    expect(ozet.tur).toBe('basari')
    expect(ozet.baslik).toBe('3 blok yerleştirildi (2 sn)')
    expect(ozet.satirlar).toEqual([])
    expect(planOzeti(sonuc(3, [], false, 1500)).baslik).toBe('3 blok yerleştirildi (1,5 sn)')
  })

  it('yerleşemeyenleri nedenleriyle listeler ve uzun listeyi keser', () => {
    const hepsi = Array.from({ length: 10 }, (_, i) => yerlesmeyen(i + 1))
    const ozet = planOzeti(sonuc(2, hepsi))
    expect(ozet.tur).toBe('uyari')
    expect(ozet.baslik).toBe('2 blok yerleştirildi, 10 blok yerleştirilemedi (2 sn)')
    expect(ozet.satirlar).toHaveLength(9)
    expect(ozet.satirlar[0]).toBe('COM 101 Şube 1, 2. blok (3 saat): ENGEL HOCA_CAKISMA (12)')
    expect(ozet.satirlar[8]).toBe('… ve 2 blok daha')
  })

  it('hiçbir blok yerleşmediyse hata olarak işaretler', () => {
    expect(planOzeti(sonuc(0, [yerlesmeyen(1)])).tur).toBe('hata')
  })

  it('süre dolduysa bunu belirtir', () => {
    const ozet = planOzeti(sonuc(1, [yerlesmeyen(1)], true))
    expect(ozet.satirlar[ozet.satirlar.length - 1]).toBe('Süre doldu, kalan bloklar için deneme yapılmadı.')
  })
})