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
  secmeli: boolean
  secmeliGrup: string | null
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
export interface BlokDurumu {
  gun: number
  baslangicSlot: number
  kilitli: boolean
  derslikIdler: number[]
}

export type IslemTuru = 'YERLESTIR' | 'TASI' | 'KALDIR' | 'KILIT'

export interface IslemKaydi {
  id: number
  blokId: number
  tur: IslemTuru
  onceki: BlokDurumu | null
  sonraki: BlokDurumu | null
  geriAlindi: boolean
  tarih: string
}

export interface PlanYerlesen {
  blokId: number
  dersKodu: string
  subeNo: number
  siraNo: number
  gun: number
  baslangicSlot: number
  bitisSlot: number
  derslikId: number
  uyariSayisi: number
}

export interface PlanYerlesmeyen {
  blokId: number
  dersKodu: string
  subeNo: number
  siraNo: number
  sure: number
  sebep: string
}

export interface PlanSonucu {
  yerlesenSayisi: number
  yerlesmeyenSayisi: number
  sureDoldu: boolean
  gecenMs: number
  yerlesenler: PlanYerlesen[]
  yerlesmeyenler: PlanYerlesmeyen[]
}

export interface TemizleSonucu {
  silinen: number
  kilitliKalan: number
}
export interface ObsDers {
  satir: number
  kod: string
  ad: string
  zorunlu: boolean
  sinif: number
  donem: string
  teo: number
  uyg: number
  lab: number
  haftalikSaat: number
  onerilenSaatIfadesi: string
  kredi: string
  akts: string
  hocaUnvan: string
  hocaAd: string
  intibak: string
  onKosul: string
  etiket: string
}

export interface ObsSecmeliGrup {
  satir: number
  kod: string
  ad: string
  sinif: number
  dersler: ObsDers[]
}

export interface ObsHoca {
  unvan: string
  ad: string
  dersSayisi: number
}

export interface ObsUyari {
  satir: number
  kod: string
  mesaj: string
}

export interface ObsOnizleme {
  donem: string
  toplamSatir: number
  donemSatiri: number
  digerDonemSatiri: number
  zorunluSayisi: number
  secmeliGrupSayisi: number
  secmeliDersSayisi: number
  grupsuzSecmeliSayisi: number
  zorunluDersler: ObsDers[]
  secmeliGruplar: ObsSecmeliGrup[]
  grupsuzSecmeliler: ObsDers[]
  hocalar: ObsHoca[]
  uyarilar: ObsUyari[]
}

export interface ObsAtlanan {
  satir: number
  kod: string
  sebep: string
}

export interface ObsKayitSonucu {
  olusturulanDers: number
  olusturulanSube: number
  olusturulanBlok: number
  yerlesimsizDers: number
  sanalDers: number
  seminerDers: number
  hocasizSube: number
  eslesenHoca: number
  olusturulanHocalar: string[]
  olusturulanSiniflar: string[]
  atlananlar: ObsAtlanan[]
}

export type DerslikKararDurumu = 'EKLENECEK' | 'ZATEN_VAR' | 'PASIF' | 'GECERSIZ'

export interface DerslikKarar {
  satir: number
  kod: string
  ad: string
  tur: DerslikTuru | null
  kapasite: number | null
  dosyadakiKapasite: number | null
  duzeltildi: boolean
  durum: DerslikKararDurumu
  sebep: string | null
}

export interface DerslikOnizleme {
  toplamSatir: number
  eklenecek: number
  zatenVar: number
  pasif: number
  gecersiz: number
  eklenecekTurler: Record<string, number>
  dersliklar: DerslikKarar[]
  uyarilar: string[]
}

export interface DerslikKayitSonucu {
  olusturulan: number
  olusturulanTurler: Record<string, number>
  atlananlar: DerslikKarar[]
  uyarilar: string[]
}