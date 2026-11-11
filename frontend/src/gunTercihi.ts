import type { GunTercihi, GunTercihTuru, TercihKatiligi } from './hocaTipleri'

export interface GunTercihKaydi {
  tur: GunTercihTuru
  deger?: number
  gun?: number
  katilik: TercihKatiligi
  asistanaBirakildi?: boolean
}

export type GunSatiri = 'tercihEdilen' | 'kacinilan'

export interface TercihFormu {
  bosGun: { acik: boolean; deger: number; katilik: TercihKatiligi }
  ardisik: { acik: boolean; asistanaBirak: boolean; katilik: TercihKatiligi }
  tercihEdilen: { gunler: number[]; katilik: TercihKatiligi }
  kacinilan: { gunler: number[]; katilik: TercihKatiligi }
}

export const EN_AZ_BOS_GUN = 1
export const EN_COK_BOS_GUN = 6

function kisitla(deger: number): number {
  if (!Number.isFinite(deger)) {
    return EN_AZ_BOS_GUN
  }
  return Math.min(EN_COK_BOS_GUN, Math.max(EN_AZ_BOS_GUN, Math.round(deger)))
}

export function bosForm(): TercihFormu {
  return {
    bosGun: { acik: false, deger: 1, katilik: 'TERCIH' },
    ardisik: { acik: false, asistanaBirak: false, katilik: 'TERCIH' },
    tercihEdilen: { gunler: [], katilik: 'TERCIH' },
    kacinilan: { gunler: [], katilik: 'TERCIH' },
  }
}

function gunleri(kayitlar: GunTercihi[], tur: GunTercihTuru): number[] {
  return kayitlar
    .flatMap((k) => (k.tur === tur && k.gun !== null ? [k.gun] : []))
    .sort((a, b) => a - b)
}

/** Günlerde bir tane bile zorunlu kayıt varsa satırın tamamı zorunlu sayılır. */
function satirKatiligi(kayitlar: GunTercihi[], tur: GunTercihTuru): TercihKatiligi {
  return kayitlar.some((k) => k.tur === tur && k.katilik === 'ZORUNLU') ? 'ZORUNLU' : 'TERCIH'
}

export function formuOlustur(kayitlar: GunTercihi[]): TercihFormu {
  const form = bosForm()
  const bos = kayitlar.find((k) => k.tur === 'BOS_GUN_SAYISI')
  if (bos) {
    form.bosGun = { acik: true, deger: kisitla(bos.deger ?? EN_AZ_BOS_GUN), katilik: bos.katilik }
  }
  const ardisik = kayitlar.find((k) => k.tur === 'ARDISIK_GUN')
  if (ardisik) {
    form.ardisik = { acik: true, asistanaBirak: ardisik.asistanaBirakildi, katilik: ardisik.katilik }
  }
  form.tercihEdilen = {
    gunler: gunleri(kayitlar, 'TERCIH_EDILEN_GUN'),
    katilik: satirKatiligi(kayitlar, 'TERCIH_EDILEN_GUN'),
  }
  form.kacinilan = {
    gunler: gunleri(kayitlar, 'KACINILAN_GUN'),
    katilik: satirKatiligi(kayitlar, 'KACINILAN_GUN'),
  }
  return form
}

/** Sunucuya gönderilecek liste. Kapalı bölümler gönderilmez. */
export function kayitlariOlustur(form: TercihFormu): GunTercihKaydi[] {
  const kayitlar: GunTercihKaydi[] = []
  if (form.bosGun.acik) {
    kayitlar.push({ tur: 'BOS_GUN_SAYISI', deger: kisitla(form.bosGun.deger), katilik: form.bosGun.katilik })
  }
  if (form.ardisik.acik) {
    kayitlar.push({
      tur: 'ARDISIK_GUN',
      katilik: form.ardisik.katilik,
      asistanaBirakildi: form.ardisik.asistanaBirak,
    })
  }
  for (const gun of [...form.tercihEdilen.gunler].sort((a, b) => a - b)) {
    kayitlar.push({ tur: 'TERCIH_EDILEN_GUN', gun, katilik: form.tercihEdilen.katilik })
  }
  for (const gun of [...form.kacinilan.gunler].sort((a, b) => a - b)) {
    kayitlar.push({ tur: 'KACINILAN_GUN', gun, katilik: form.kacinilan.katilik })
  }
  return kayitlar
}

/** Günü bir satırda açıp kapatır. Aynı gün hem tercih edilen hem kaçınılan olamayacağı için diğer satırdan çıkarır. */
export function gunSec(form: TercihFormu, satir: GunSatiri, gun: number): TercihFormu {
  const diger: GunSatiri = satir === 'tercihEdilen' ? 'kacinilan' : 'tercihEdilen'
  const yeni: TercihFormu = {
    ...form,
    tercihEdilen: { ...form.tercihEdilen, gunler: [...form.tercihEdilen.gunler] },
    kacinilan: { ...form.kacinilan, gunler: [...form.kacinilan.gunler] },
  }
  if (yeni[satir].gunler.includes(gun)) {
    yeni[satir].gunler = yeni[satir].gunler.filter((g) => g !== gun)
  } else {
    yeni[satir].gunler = [...yeni[satir].gunler, gun].sort((a, b) => a - b)
    yeni[diger].gunler = yeni[diger].gunler.filter((g) => g !== gun)
  }
  return yeni
}

export function formlarEsit(a: TercihFormu, b: TercihFormu): boolean {
  return JSON.stringify(kayitlariOlustur(a)) === JSON.stringify(kayitlariOlustur(b))
}

/** Izgaradaki günler ve kayıtlarda geçen (ızgara dışı) günler. Kayıtlı bir gün listeden kaybolmasın diye. */
export function gorunenGunler(form: TercihFormu, gunSayisi: number): number[] {
  const hepsi = new Set<number>(Array.from({ length: gunSayisi }, (_, i) => i + 1))
  for (const g of [...form.tercihEdilen.gunler, ...form.kacinilan.gunler]) {
    hepsi.add(g)
  }
  return [...hepsi].sort((a, b) => a - b)
}
