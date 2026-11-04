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