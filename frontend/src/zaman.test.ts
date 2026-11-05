import { describe, expect, it } from 'vitest'
import { aralikMetni, slotSaatleri } from './zaman'
import type { ZamanIzgarasi } from './tipler'

const izgara: ZamanIzgarasi = {
  gunSayisi: 5,
  gunlukSlotSayisi: 9,
  dersSuresiDk: 45,
  araSuresiDk: 15,
  baslangicSaati: '08:30',
}

describe('slotSaatleri', () => {
  it('45 dakika ders ve 15 dakika aralıkla saatleri hesaplar', () => {
    expect(slotSaatleri(izgara, 1)).toBe('08:30–09:15')
    expect(slotSaatleri(izgara, 2)).toBe('09:30–10:15')
    expect(slotSaatleri(izgara, 5)).toBe('12:30–13:15')
    expect(slotSaatleri(izgara, 9)).toBe('16:30–17:15')
  })

  it('saniyeli başlangıç saatini de okur', () => {
    expect(slotSaatleri({ ...izgara, baslangicSaati: '08:30:00' }, 1)).toBe('08:30–09:15')
  })

  it('farklı süreli ızgaralarda çalışır', () => {
    const baska: ZamanIzgarasi = { ...izgara, dersSuresiDk: 50, araSuresiDk: 10, baslangicSaati: '09:00' }
    expect(slotSaatleri(baska, 3)).toBe('11:00–11:50')
  })
})

describe('aralikMetni', () => {
  it('blok boyunca ara dahil tek aralık verir', () => {
    expect(aralikMetni(izgara, 1, 3)).toBe('08:30–11:15')
    expect(aralikMetni(izgara, 4, 5)).toBe('11:30–13:15')
    expect(aralikMetni(izgara, 7, 7)).toBe('14:30–15:15')
  })
})