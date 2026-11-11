import { etkinRenk, hocaMetni } from './hocaYardimci'
import { seritleriBul } from './seritler'
import type {
  AkademikProgram,
  Derslik,
  Dil,
  Ders,
  DersSinif,
  OgretimElemani,
  Seviye,
  Sinif,
  Sube,
  Yerlesim,
} from './tipler'

export type CiktiGorunumu = 'SINIF' | 'HOCA' | 'DERSLIK'

export interface CiktiVerisi {
  dersler: Ders[]
  yerlesimler: Yerlesim[]
  subeler: Sube[]
  hocalar: OgretimElemani[]
  derslikler: Derslik[]
  dersSiniflari: DersSinif[]
  gunSayisi: number
  slotSayisi: number
}

export interface CiktiKutu {
  id: number
  gun: number
  baslangic: number
  bitis: number
  kod: string
  ad: string
  hoca: string | null
  derslik: string
  subeNo: number
  secmeli: boolean
  sanal: boolean
  renk: string | null
}

export interface Varlik {
  id: number
  baslik: string
  altBaslik: string
}

export type TabloHucre = { tur: 'bos' } | { tur: 'kutu'; kutu: CiktiKutu; genislik: number }

export interface TabloGunu {
  gun: number
  satirlar: TabloHucre[][]
}

const SEVIYE_SIRASI: Record<Seviye, number> = { LISANS: 0, YUKSEK_LISANS: 1, DOKTORA: 2 }
const SEVIYE_ADLARI: Record<Seviye, string> = { LISANS: 'Lisans', YUKSEK_LISANS: 'Yüksek lisans', DOKTORA: 'Doktora' }
const DIL_SIRASI: Record<Dil, number> = { EN: 0, TR: 1 }
const DIL_ADLARI: Record<Dil, string> = { EN: 'İngilizce', TR: 'Türkçe' }

/** Seçilen sınıfın, hocanın ya da dersliğin programdaki blokları, gün ve saat sırasıyla. */
export function varlikKutulari(gorunum: CiktiGorunumu, varlikId: number, veri: CiktiVerisi): CiktiKutu[] {
  const dersMap = new Map(veri.dersler.map((d) => [d.id, d]))
  const subeMap = new Map(veri.subeler.map((s) => [s.id, s]))
  const hocaMap = new Map(veri.hocalar.map((h) => [h.id, h]))
  const derslikMap = new Map(veri.derslikler.map((d) => [d.id, d]))
  const sinifDersleri = new Set(veri.dersSiniflari.filter((x) => x.sinifId === varlikId).map((x) => x.dersId))

  const kutular: CiktiKutu[] = []
  for (const y of veri.yerlesimler) {
    const ders = dersMap.get(y.dersId)
    const sube = subeMap.get(y.subeId)
    if (!ders || !sube) {
      continue
    }
    if (gorunum === 'SINIF' && !sinifDersleri.has(y.dersId)) {
      continue
    }
    if (gorunum === 'HOCA' && sube.ogretimElemaniId !== varlikId) {
      continue
    }
    if (gorunum === 'DERSLIK' && !y.derslikIdler.includes(varlikId)) {
      continue
    }
    if (y.gun < 1 || y.gun > veri.gunSayisi || y.baslangicSlot < 1 || y.bitisSlot > veri.slotSayisi) {
      continue
    }
    const hoca = sube.ogretimElemaniId !== null ? hocaMap.get(sube.ogretimElemaniId) : undefined
    kutular.push({
      id: y.id,
      gun: y.gun,
      baslangic: y.baslangicSlot,
      bitis: y.bitisSlot,
      kod: ders.kod,
      ad: ders.ad,
      hoca: hoca ? hocaMetni(hoca) : null,
      derslik: y.derslikIdler.map((id) => derslikMap.get(id)?.ad ?? `Derslik ${id}`).join(', '),
      subeNo: sube.subeNo,
      secmeli: ders.secmeli,
      sanal: ders.sanal,
      renk: hoca ? etkinRenk(hoca) : null,
    })
  }
  return kutular.sort((a, b) => a.gun - b.gun || a.baslangic - b.baslangic || a.id - b.id)
}

/**
 * Haftalık tablo: her gün bir ya da daha fazla satırdır. Aynı saatte çakışan bloklar (örneğin seminer
 * grupları) o günün ayrı satırlarına dağıtılır. Her satırın hücre genişlikleri toplamı slotSayisi'na eşittir.
 */
export function tabloOlustur(kutular: CiktiKutu[], gunSayisi: number, slotSayisi: number): TabloGunu[] {
  const gunler: TabloGunu[] = []
  for (let gun = 1; gun <= gunSayisi; gun++) {
    const gunKutulari = kutular.filter((k) => k.gun === gun)
    const seritler = seritleriBul(gunKutulari)
    let satirSayisi = 1
    for (const k of gunKutulari) {
      satirSayisi = Math.max(satirSayisi, (seritler.get(k.id)?.serit ?? 0) + 1)
    }

    const satirlar: TabloHucre[][] = []
    for (let satir = 0; satir < satirSayisi; satir++) {
      const buSatirdakiler = gunKutulari
        .filter((k) => (seritler.get(k.id)?.serit ?? 0) === satir)
        .sort((a, b) => a.baslangic - b.baslangic)
      const hucreler: TabloHucre[] = []
      let slot = 1
      while (slot <= slotSayisi) {
        const kutu = buSatirdakiler.find((k) => k.baslangic === slot)
        if (kutu) {
          const genislik = Math.max(1, Math.min(kutu.bitis, slotSayisi) - slot + 1)
          hucreler.push({ tur: 'kutu', kutu, genislik })
          slot += genislik
        } else {
          hucreler.push({ tur: 'bos' })
          slot += 1
        }
      }
      satirlar.push(hucreler)
    }
    gunler.push({ gun, satirlar })
  }
  return gunler
}

/** Hücrede kodun altında gösterilecek satırlar. Görünüme göre gereksiz bilgi (örneğin hocanın kendi adı) çıkarılır. */
export function hucreSatirlari(kutu: CiktiKutu, gorunum: CiktiGorunumu): string[] {
  const hoca = kutu.hoca ?? 'Hoca atanmamış'
  if (gorunum === 'HOCA') {
    return [kutu.ad, kutu.derslik]
  }
  if (gorunum === 'DERSLIK') {
    return [kutu.ad, hoca]
  }
  return [kutu.ad, hoca, kutu.derslik]
}

export function hucreBasligi(kutu: CiktiKutu): string {
  return kutu.subeNo > 1 ? `${kutu.kod} · Şube ${kutu.subeNo}` : kutu.kod
}

/** Önce lisans, sonra yüksek lisans, doktora. Her grupta İngilizce Türkçe'den önce, sınıflar 1'den başlayarak. */
export function sinifVarliklari(programlar: AkademikProgram[], siniflar: Sinif[]): Varlik[] {
  const sonuc: Varlik[] = []
  const siraliProgramlar = [...programlar].sort(
    (a, b) =>
      SEVIYE_SIRASI[a.seviye] - SEVIYE_SIRASI[b.seviye] ||
      DIL_SIRASI[a.dil] - DIL_SIRASI[b.dil] ||
      a.ad.localeCompare(b.ad, 'tr'),
  )
  for (const program of siraliProgramlar) {
    const programSiniflari = siniflar.filter((s) => s.programId === program.id).sort((a, b) => a.duzey - b.duzey)
    for (const sinif of programSiniflari) {
      sonuc.push({
        id: sinif.id,
        baslik: `${program.ad} · ${sinif.ad}`,
        altBaslik: `${SEVIYE_ADLARI[program.seviye]}, ${DIL_ADLARI[program.dil]}`,
      })
    }
  }
  return sonuc
}

export function hocaVarliklari(hocalar: OgretimElemani[]): Varlik[] {
  return [...hocalar]
    .sort((a, b) => a.adSoyad.localeCompare(b.adSoyad, 'tr'))
    .map((h) => ({ id: h.id, baslik: hocaMetni(h), altBaslik: 'Öğretim elemanı' }))
}

export function derslikVarliklari(derslikler: Derslik[]): Varlik[] {
  return [...derslikler]
    .sort((a, b) => a.ad.localeCompare(b.ad, 'tr'))
    .map((d) => ({ id: d.id, baslik: d.ad, altBaslik: `${d.kapasite} kişilik` }))
}
