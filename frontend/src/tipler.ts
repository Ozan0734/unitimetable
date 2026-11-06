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

export interface ZamanIzgarasi {
  gunSayisi: number
  gunlukSlotSayisi: number
  dersSuresiDk: number
  araSuresiDk: number
  baslangicSaati: string
}

export interface Sube {
  id: number
  dersId: number
  ogretimElemaniId: number | null
  subeNo: number
  tahminiOgrenci: number | null
  sinifCakismaIzni: boolean
  derslikCakismaIzni: boolean
  cokluDerslikIzni: boolean
}

export interface OgretimElemani {
  id: number
  bolumId: number
  adSoyad: string
  unvan: string | null
  renk: string | null
  oncelik: number
}

export type DerslikTuru = 'SINIF' | 'LABORATUVAR' | 'AMFI' | 'SANAL' | 'SALON'

export interface Derslik {
  id: number
  kurumId: number
  ad: string
  kapasite: number
  tur: DerslikTuru
}

export type Seviye = 'LISANS' | 'YUKSEK_LISANS' | 'DOKTORA'
export type Dil = 'TR' | 'EN'

export interface AkademikProgram {
  id: number
  bolumId: number
  ad: string
  seviye: Seviye
  dil: Dil
}

export interface Sinif {
  id: number
  programId: number
  ad: string
  duzey: number
}

export interface DersSinif {
  id: number
  dersId: number
  sinifId: number
}

export type IhlalTuru = 'ENGEL' | 'UYARI' | 'BILGI'

export interface Ihlal {
  tur: IhlalTuru
  kod: string
  mesaj: string
  iliskiliBlokId: number | null
}

export type YerDurumu = 'UYGUN' | 'UYARILI' | 'ENGELLI'

export interface UygunYer {
  gun: number
  baslangicSlot: number
  bitisSlot: number
  derslikId: number
  durum: YerDurumu
  ihlaller: Ihlal[]
}

export interface CakismaGovdesi {
  mesaj: string
  onayGerekli: boolean
  ihlaller: Ihlal[]
}