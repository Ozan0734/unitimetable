export type Rol = 'SUPERADMIN' | 'FAKULTE' | 'ASISTAN'

export interface Kullanici {
  id: number
  kurumId: number
  fakulteId: number | null
  bolumId: number | null
  adSoyad: string
  eposta: string
  rol: Rol
}

export interface Bolum {
  id: number
  fakulteId: number
  ad: string
}

export type ProjeDurumu = 'TASLAK' | 'YAYINDA'

export interface ProgramProjesi {
  id: number
  kurumId: number
  bolumId: number
  ad: string
  donem: string
  durum: ProjeDurumu
  olusturmaTarihi: string
}

export type DersTuru = 'NORMAL' | 'YERLESIMSIZ' | 'SEMINER'

export interface Ders {
  id: number
  projeId: number
  bolumId: number
  kod: string
  ad: string
  tur: DersTuru
  sanal: boolean
  haftalikSaat: number | null
  saatIfadesi: string | null
  bloklar: number[]
}

export interface Yerlesim {
  id: number
  blokId: number
  subeId: number
  dersId: number
  gun: number
  baslangicSlot: number
  bitisSlot: number
  sure: number
  derslikIdler: number[]
  kilitli: boolean
}

export interface YerlesmeyenBlok {
  blokId: number
  subeId: number
  dersId: number
  dersKodu: string
  dersAdi: string
  subeNo: number
  siraNo: number
  sure: number
}