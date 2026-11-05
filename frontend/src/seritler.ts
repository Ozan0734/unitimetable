export interface AralikKutu {
  id: number
  gun: number
  baslangic: number
  bitis: number
}

export interface Serit {
  serit: number
  toplam: number
}

/**
 * Aynı gün içinde çakışan blokları yan yana şeritlere dağıtır.
 * Saat aralıkları uçları dahil sayılır: 1-3 ile 3-4 çakışır, 1-3 ile 4-5 çakışmaz.
 */
export function seritleriBul(kutular: AralikKutu[]): Map<number, Serit> {
  const sonuc = new Map<number, Serit>()
  const gunler = new Map<number, AralikKutu[]>()
  for (const kutu of kutular) {
    const liste = gunler.get(kutu.gun) ?? []
    liste.push(kutu)
    gunler.set(kutu.gun, liste)
  }

  for (const liste of gunler.values()) {
    liste.sort((a, b) => a.baslangic - b.baslangic || a.bitis - b.bitis || a.id - b.id)

    let kume: { id: number; serit: number }[] = []
    let seritBitisleri: number[] = []
    let kumeBitisi = -1

    const kumeyiBitir = () => {
      for (const oge of kume) {
        sonuc.set(oge.id, { serit: oge.serit, toplam: seritBitisleri.length })
      }
      kume = []
      seritBitisleri = []
      kumeBitisi = -1
    }

    for (const kutu of liste) {
      if (kume.length > 0 && kutu.baslangic > kumeBitisi) {
        kumeyiBitir()
      }
      let serit = seritBitisleri.findIndex((bitis) => bitis < kutu.baslangic)
      if (serit === -1) {
        serit = seritBitisleri.length
        seritBitisleri.push(kutu.bitis)
      } else {
        seritBitisleri[serit] = kutu.bitis
      }
      kume.push({ id: kutu.id, serit })
      kumeBitisi = Math.max(kumeBitisi, kutu.bitis)
    }
    if (kume.length > 0) {
      kumeyiBitir()
    }
  }
  return sonuc
}