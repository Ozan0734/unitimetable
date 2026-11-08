import { sadelestir } from './obsAktarim'
import type { Ders, Sube } from './tipler'

/** En az bir şubesinde öğretim elemanı atanmamış olan derslerin numaraları. */
export function hocasizDersler(subeler: Sube[]): Set<number> {
  return new Set(subeler.filter((s) => s.ogretimElemaniId === null).map((s) => s.dersId))
}

export function dersleriSuz(dersler: Ders[], subeler: Sube[], arama: string, sadeceHocasiz: boolean): Ders[] {
  const aranan = sadelestir(arama.trim())
  const hocasiz = hocasizDersler(subeler)
  return dersler.filter((d) => {
    if (sadeceHocasiz && !hocasiz.has(d.id)) {
      return false
    }
    return aranan === '' || sadelestir(`${d.kod} ${d.ad}`).includes(aranan)
  })
}

export function yerlesikBlokSayisi(subeId: number, yerlesimler: { subeId: number }[]): number {
  return yerlesimler.filter((y) => y.subeId === subeId).length
}