export type MusaitlikDurumu = 'MUSAIT_DEGIL' | 'TERCIH'

export interface MusaitlikKaydi {
  gun: number
  slot: number
  durum: MusaitlikDurumu
}

export type GunTercihTuru = 'BOS_GUN_SAYISI' | 'ARDISIK_GUN' | 'TERCIH_EDILEN_GUN' | 'KACINILAN_GUN'
export type TercihKatiligi = 'ZORUNLU' | 'TERCIH'

export interface GunTercihi {
  tur: GunTercihTuru
  deger: number | null
  gun: number | null
  katilik: TercihKatiligi
  asistanaBirakildi: boolean
}
