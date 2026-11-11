import { describe, expect, it } from 'vitest'
import {
  bosForm,
  formlarEsit,
  formuOlustur,
  gorunenGunler,
  gunSec,
  kayitlariOlustur,
} from './gunTercihi'
import type { GunTercihi } from './hocaTipleri'

function kayit(
  tur: GunTercihi['tur'],
  ek: Partial<Pick<GunTercihi, 'deger' | 'gun' | 'katilik' | 'asistanaBirakildi'>> = {},
): GunTercihi {
  return { tur, deger: null, gun: null, katilik: 'TERCIH', asistanaBirakildi: false, ...ek }
}

describe('formuOlustur', () => {
  it('kayıt yoksa her şey kapalıdır', () => {
    expect(formuOlustur([])).toEqual(bosForm())
  })

  it('sunucudaki kayıtları forma çevirir', () => {
    const form = formuOlustur([
      kayit('BOS_GUN_SAYISI', { deger: 2 }),
      kayit('ARDISIK_GUN', { asistanaBirakildi: true }),
      kayit('KACINILAN_GUN', { gun: 5, katilik: 'ZORUNLU' }),
      kayit('TERCIH_EDILEN_GUN', { gun: 3 }),
      kayit('TERCIH_EDILEN_GUN', { gun: 2 }),
    ])
    expect(form.bosGun).toEqual({ acik: true, deger: 2, katilik: 'TERCIH' })
    expect(form.ardisik).toEqual({ acik: true, asistanaBirak: true, katilik: 'TERCIH' })
    expect(form.kacinilan).toEqual({ gunler: [5], katilik: 'ZORUNLU' })
    expect(form.tercihEdilen).toEqual({ gunler: [2, 3], katilik: 'TERCIH' })
  })

  it('satırda bir tane zorunlu kayıt varsa satırın tamamı zorunlu olur', () => {
    const form = formuOlustur([
      kayit('KACINILAN_GUN', { gun: 1 }),
      kayit('KACINILAN_GUN', { gun: 5, katilik: 'ZORUNLU' }),
    ])
    expect(form.kacinilan.katilik).toBe('ZORUNLU')
  })
})

describe('kayitlariOlustur', () => {
  it('kapalı bölümleri göndermez', () => {
    expect(kayitlariOlustur(bosForm())).toEqual([])
  })

  it('açık bölümleri sunucunun beklediği biçimde ve sırayla verir', () => {
    let form = bosForm()
    form = { ...form, bosGun: { acik: true, deger: 2, katilik: 'ZORUNLU' } }
    form = { ...form, ardisik: { acik: true, asistanaBirak: true, katilik: 'TERCIH' } }
    form = gunSec(form, 'kacinilan', 5)
    form = gunSec(form, 'tercihEdilen', 3)
    form = gunSec(form, 'tercihEdilen', 2)
    expect(kayitlariOlustur(form)).toEqual([
      { tur: 'BOS_GUN_SAYISI', deger: 2, katilik: 'ZORUNLU' },
      { tur: 'ARDISIK_GUN', katilik: 'TERCIH', asistanaBirakildi: true },
      { tur: 'TERCIH_EDILEN_GUN', gun: 2, katilik: 'TERCIH' },
      { tur: 'TERCIH_EDILEN_GUN', gun: 3, katilik: 'TERCIH' },
      { tur: 'KACINILAN_GUN', gun: 5, katilik: 'TERCIH' },
    ])
  })

  it('boş gün sayısını 1 ile 6 arasına çeker', () => {
    const alti = { ...bosForm(), bosGun: { acik: true, deger: 9, katilik: 'TERCIH' as const } }
    const bir = { ...bosForm(), bosGun: { acik: true, deger: Number.NaN, katilik: 'TERCIH' as const } }
    expect(kayitlariOlustur(alti)[0].deger).toBe(6)
    expect(kayitlariOlustur(bir)[0].deger).toBe(1)
  })

  it('formdan gidip gelince aynı kayıtlar çıkar', () => {
    const once: GunTercihi[] = [
      kayit('BOS_GUN_SAYISI', { deger: 3, katilik: 'ZORUNLU' }),
      kayit('TERCIH_EDILEN_GUN', { gun: 1 }),
      kayit('KACINILAN_GUN', { gun: 4, katilik: 'ZORUNLU' }),
    ]
    const sonra = kayitlariOlustur(formuOlustur(once))
    expect(sonra).toEqual([
      { tur: 'BOS_GUN_SAYISI', deger: 3, katilik: 'ZORUNLU' },
      { tur: 'TERCIH_EDILEN_GUN', gun: 1, katilik: 'TERCIH' },
      { tur: 'KACINILAN_GUN', gun: 4, katilik: 'ZORUNLU' },
    ])
  })
})

describe('gunSec', () => {
  it('günü açıp kapatır', () => {
    const acik = gunSec(bosForm(), 'kacinilan', 5)
    expect(acik.kacinilan.gunler).toEqual([5])
    expect(gunSec(acik, 'kacinilan', 5).kacinilan.gunler).toEqual([])
  })

  it('aynı günü diğer satırdan çıkarır', () => {
    let form = gunSec(bosForm(), 'tercihEdilen', 2)
    form = gunSec(form, 'kacinilan', 2)
    expect(form.kacinilan.gunler).toEqual([2])
    expect(form.tercihEdilen.gunler).toEqual([])
  })

  it('verilen formu değiştirmez', () => {
    const form = bosForm()
    gunSec(form, 'kacinilan', 1)
    expect(form.kacinilan.gunler).toEqual([])
  })
})

describe('formlarEsit ve gorunenGunler', () => {
  it('kapalı bölümlerin gizli değerlerini yok sayar', () => {
    const a = bosForm()
    const b = { ...bosForm(), bosGun: { acik: false, deger: 4, katilik: 'ZORUNLU' as const } }
    expect(formlarEsit(a, b)).toBe(true)
    expect(formlarEsit(a, gunSec(a, 'kacinilan', 1))).toBe(false)
  })

  it('ızgara dışındaki kayıtlı günler de listede görünür', () => {
    const form = gunSec(gunSec(bosForm(), 'kacinilan', 6), 'tercihEdilen', 2)
    expect(gorunenGunler(form, 5)).toEqual([1, 2, 3, 4, 5, 6])
    expect(gorunenGunler(bosForm(), 5)).toEqual([1, 2, 3, 4, 5])
  })
})
