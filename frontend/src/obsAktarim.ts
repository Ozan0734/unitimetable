import type { ObsDers, ObsSecmeliGrup } from './tipler'

export type TahminiTur = 'NORMAL' | 'YERLESIMSIZ' | 'SEMINER'

export interface KayitSecimi {
  donem: 'GUZ' | 'BAHAR'
  projeId: number
  programId: number
  secmeliDersler: string[]
  sanalOnekleri: string[]
  seminerKodlari: string[]
}

/** Büyük küçük harf, Türkçe karakter ve aksan farkını yok sayarak karşılaştırma için sadeleştirir. */
export function sadelestir(metin: string): string {
  return metin
    .toLocaleLowerCase('tr')
    .replace(/ı/g, 'i')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
}

export function aramaEslesir(ders: ObsDers, arama: string): boolean {
  const aranan = sadelestir(arama.trim())
  if (aranan === '') {
    return true
  }
  return sadelestir(`${ders.kod} ${ders.ad} ${ders.hocaAd}`).includes(aranan)
}

/** Virgül, noktalı virgül ya da satır sonuyla ayrılmış kodları temizleyip tekrarsız listeler. */
export function kodListesi(metin: string): string[] {
  const sonuc: string[] = []
  for (const parca of metin.split(/[,;\n]+/)) {
    const kod = parca.trim().replace(/\s+/g, ' ').toUpperCase()
    if (kod !== '' && !sonuc.includes(kod)) {
      sonuc.push(kod)
    }
  }
  return sonuc
}

/**
 * Kaydet isteğinin form alanları. Boş seçmeli listesi gönderilmez. Sanal önekleri her zaman gönderilir:
 * boş gönderilirse hiçbir ders sanal sayılmaz, gönderilmezse sunucunun varsayılanı (ATA, TDB, ISG) kullanılır.
 */
export function formAlanlari(secim: KayitSecimi): [string, string][] {
  const alanlar: [string, string][] = [
    ['donem', secim.donem],
    ['projeId', String(secim.projeId)],
    ['programId', String(secim.programId)],
    ['sanalOnekleri', secim.sanalOnekleri.join(',')],
  ]
  if (secim.secmeliDersler.length > 0) {
    alanlar.push(['secmeliDersler', secim.secmeliDersler.join(',')])
  }
  if (secim.seminerKodlari.length > 0) {
    alanlar.push(['seminerKodlari', secim.seminerKodlari.join(',')])
  }
  return alanlar
}

function sanalMi(kod: string, onekler: string[]): boolean {
  return onekler.some((onek) => {
    if (!kod.startsWith(onek)) {
      return false
    }
    if (kod.length === onek.length) {
      return true
    }
    const sonraki = kod.charAt(onek.length)
    return sonraki === ' ' || sonraki === '.' || /\d/.test(sonraki)
  })
}

function bitirmeMi(ad: string): boolean {
  const buyuk = ad.toUpperCase()
  return buyuk.includes('GRADUATION') || buyuk.includes('BITIRME') || ad.toLocaleUpperCase('tr').includes('BİTİRME')
}

/** Sunucudaki sınıflandırmanın önizleme için yaklaşık kopyası. Kesin karar kayıtta sunucuda verilir. */
export function sinifTahmini(
  ders: ObsDers,
  sanalOnekleri: string[],
  seminerKodlari: string[],
): { tur: TahminiTur; sanal: boolean } {
  const kod = ders.kod.toUpperCase()
  if (seminerKodlari.includes(kod)) {
    return { tur: 'SEMINER', sanal: true }
  }
  if (ders.haftalikSaat === 0 || bitirmeMi(ders.ad)) {
    return { tur: 'YERLESIMSIZ', sanal: false }
  }
  return { tur: 'NORMAL', sanal: sanalMi(kod, sanalOnekleri) }
}

export function grupDurumu(grup: ObsSecmeliGrup, secilen: Set<string>): { secili: number; toplam: number } {
  return { secili: grup.dersler.filter((d) => secilen.has(d.kod)).length, toplam: grup.dersler.length }
}