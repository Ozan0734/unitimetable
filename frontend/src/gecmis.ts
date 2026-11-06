import type { IslemKaydi, IslemTuru, PlanSonucu } from './tipler'

export const ISLEM_ADLARI: Record<IslemTuru, string> = {
  YERLESTIR: 'Yerleştirme',
  TASI: 'Taşıma',
  KALDIR: 'Kaldırma',
  KILIT: 'Kilit değişikliği',
}

export interface GecmisDurumu {
  sonGeriAlinabilir: IslemKaydi | null
  sonYinelenebilir: IslemKaydi | null
}

/**
 * Geri al, en son yapılmış ve henüz geri alınmamış işlemi geri alır.
 * Yinele, geri alınmış işlemlerden en eskisini yeniden uygular. Backend de aynı sırayı kullanır.
 */
export function gecmisDurumu(gecmis: IslemKaydi[]): GecmisDurumu {
  const yeniden = [...gecmis].sort((a, b) => b.id - a.id)
  const sonGeriAlinabilir = yeniden.find((k) => !k.geriAlindi) ?? null
  const geriAlinanlar = yeniden.filter((k) => k.geriAlindi)
  const sonYinelenebilir = geriAlinanlar.length > 0 ? geriAlinanlar[geriAlinanlar.length - 1] : null
  return { sonGeriAlinabilir, sonYinelenebilir }
}

export interface Ozet {
  tur: 'basari' | 'uyari' | 'hata'
  baslik: string
  satirlar: string[]
}

const EN_FAZLA_SATIR = 8

function saniye(ms: number): string {
  return String(Math.round(ms / 100) / 10).replace('.', ',')
}

/** Otomatik planlama sonucunu kullanıcıya gösterilecek bildirime çevirir. */
export function planOzeti(sonuc: PlanSonucu): Ozet {
  if (sonuc.yerlesenSayisi === 0 && sonuc.yerlesmeyenSayisi === 0) {
    return { tur: 'basari', baslik: 'Yerleştirilecek blok yok', satirlar: ['Tüm bloklar zaten yerleşik.'] }
  }
  if (sonuc.yerlesmeyenSayisi === 0) {
    return {
      tur: 'basari',
      baslik: `${sonuc.yerlesenSayisi} blok yerleştirildi (${saniye(sonuc.gecenMs)} sn)`,
      satirlar: [],
    }
  }
  const satirlar = sonuc.yerlesmeyenler
    .slice(0, EN_FAZLA_SATIR)
    .map((b) => `${b.dersKodu} Şube ${b.subeNo}, ${b.siraNo}. blok (${b.sure} saat): ${b.sebep}`)
  if (sonuc.yerlesmeyenler.length > EN_FAZLA_SATIR) {
    satirlar.push(`… ve ${sonuc.yerlesmeyenler.length - EN_FAZLA_SATIR} blok daha`)
  }
  if (sonuc.sureDoldu) {
    satirlar.push('Süre doldu, kalan bloklar için deneme yapılmadı.')
  }
  return {
    tur: sonuc.yerlesenSayisi === 0 ? 'hata' : 'uyari',
    baslik: `${sonuc.yerlesenSayisi} blok yerleştirildi, ${sonuc.yerlesmeyenSayisi} blok yerleştirilemedi (${saniye(sonuc.gecenMs)} sn)`,
    satirlar,
  }
}