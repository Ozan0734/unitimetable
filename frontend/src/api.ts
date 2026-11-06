export class ApiHatasi extends Error {
  durum: number
  alanlar: Record<string, string>
  govde: unknown

  constructor(durum: number, mesaj: string, alanlar: Record<string, string> = {}, govde: unknown = null) {
    super(mesaj)
    this.name = 'ApiHatasi'
    this.durum = durum
    this.alanlar = alanlar
    this.govde = govde
  }
}

interface HataGovdesi {
  mesaj?: string
  alanlar?: Record<string, string>
}

export async function api<T>(yol: string, secenekler: RequestInit = {}): Promise<T> {
  const basliklar = new Headers(secenekler.headers)
  if (typeof secenekler.body === 'string' && !basliklar.has('Content-Type')) {
    basliklar.set('Content-Type', 'application/json')
  }

  let cevap: Response
  try {
    cevap = await fetch(yol, { credentials: 'same-origin', ...secenekler, headers: basliklar })
  } catch {
    throw new ApiHatasi(0, 'Sunucuya ulaşılamadı. Backend çalışıyor mu?')
  }

  if (cevap.status === 204) {
    return undefined as T
  }

  const metin = await cevap.text()
  let veri: unknown = null
  if (metin) {
    try {
      veri = JSON.parse(metin)
    } catch {
      veri = null
    }
  }

  if (!cevap.ok) {
    const govde = (veri ?? {}) as HataGovdesi
    const varsayilan =
      cevap.status >= 500
        ? `Sunucuya ulaşılamadı ya da sunucu hata verdi (HTTP ${cevap.status})`
        : `Beklenmeyen hata (HTTP ${cevap.status})`
    throw new ApiHatasi(cevap.status, govde.mesaj ?? varsayilan, govde.alanlar ?? {}, veri)
  }

  return veri as T
}

export function hataMetni(hata: unknown): string {
  if (hata instanceof ApiHatasi) {
    const ayrintilar = Object.values(hata.alanlar)
    return ayrintilar.length > 0 ? `${hata.message}: ${ayrintilar.join('; ')}` : hata.message
  }
  return hata instanceof Error ? hata.message : 'Beklenmeyen hata'
}