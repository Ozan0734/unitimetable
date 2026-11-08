export interface IfadeSonucu {
  bloklar: number[]
  hata: string | null
}

const EN_FAZLA_BLOK = 40

/** Boşlukları atar: "3 + 2" yazımı "3+2" olur. Sunucuya bu hâliyle gönderilir. */
export function ifadeyiSadelestir(ifade: string): string {
  return ifade.replace(/\s+/g, '')
}

/**
 * "3+2" -> [3, 2], "2*2+1" -> [2, 2, 1], "1*4" -> [1, 1, 1, 1].
 * Çarpımda ilk sayı bloğun süresi, ikincisi blok adedidir. Biçim geçersizse null döner.
 */
export function bloklariCoz(ifade: string): number[] | null {
  const temiz = ifadeyiSadelestir(ifade)
  if (temiz === '') {
    return null
  }
  const bloklar: number[] = []
  for (const parca of temiz.split('+')) {
    const eslesme = /^(\d{1,3})(?:\*(\d{1,3}))?$/.exec(parca)
    if (!eslesme) {
      return null
    }
    const sure = Number(eslesme[1])
    const adet = eslesme[2] === undefined ? 1 : Number(eslesme[2])
    if (sure < 1 || adet < 1) {
      return null
    }
    for (let i = 0; i < adet; i++) {
      bloklar.push(sure)
    }
    if (bloklar.length > EN_FAZLA_BLOK) {
      return null
    }
  }
  return bloklar
}

/** İfade boşsa haftalık saatin kendisi tek blok sayılır. Toplam haftalık saatle uyuşmalıdır. */
export function ifadeKontrol(ifade: string, haftalikSaat: number | null): IfadeSonucu {
  const temiz = ifadeyiSadelestir(ifade)
  const kullanilan = temiz === '' && haftalikSaat !== null ? String(haftalikSaat) : temiz
  if (kullanilan === '') {
    return { bloklar: [], hata: 'Saat ifadesi ya da haftalık saat girilmelidir' }
  }
  const bloklar = bloklariCoz(kullanilan)
  if (bloklar === null) {
    return {
      bloklar: [],
      hata: 'Saat ifadesi anlaşılamadı. Örnekler: 3+2, 2*2+1 (2 saatlik 2 blok ve 1 saatlik blok), 1*4',
    }
  }
  const toplam = bloklar.reduce((a, b) => a + b, 0)
  if (haftalikSaat !== null && toplam !== haftalikSaat) {
    return { bloklar, hata: `İfadenin toplamı ${toplam} saat, haftalık saat ${haftalikSaat} ile uyuşmuyor` }
  }
  return { bloklar, hata: null }
}

export function blokMetni(bloklar: number[]): string {
  return bloklar.join(' + ')
}