import type { CSSProperties } from 'react'
import { seritleriBul } from './seritler'
import { GUN_ADLARI, slotSaatleri } from './zaman'
import type { ZamanIzgarasi } from './tipler'

export interface Kutu {
  id: number
  gun: number
  baslangic: number
  bitis: number
  baslik: string
  altSatirlar: string[]
  renk: string | null
  kilitli: boolean
  ipucu: string
}

interface Props {
  izgara: ZamanIzgarasi
  kutular: Kutu[]
}

const SATIR_YUKSEKLIGI = 64

export default function Izgara({ izgara, kutular }: Props) {
  const seritler = seritleriBul(kutular)
  const gunler = Array.from({ length: izgara.gunSayisi }, (_, i) => i + 1)
  const slotlar = Array.from({ length: izgara.gunlukSlotSayisi }, (_, i) => i + 1)

  const izgaraStili: CSSProperties = {
    gridTemplateColumns: `96px repeat(${izgara.gunSayisi}, minmax(130px, 1fr))`,
    gridTemplateRows: `auto repeat(${izgara.gunlukSlotSayisi}, ${SATIR_YUKSEKLIGI}px)`,
  }

  return (
    <div className="izgara-kap">
      <div className="izgara" style={izgaraStili}>
        <div className="izgara-baslik" style={{ gridColumn: 1, gridRow: 1 }} />
        {gunler.map((gun) => (
          <div key={`g${gun}`} className="izgara-baslik" style={{ gridColumn: gun + 1, gridRow: 1 }}>
            {GUN_ADLARI[gun - 1]}
          </div>
        ))}
        {slotlar.map((slot) => (
          <div key={`s${slot}`} className="izgara-saat" style={{ gridColumn: 1, gridRow: slot + 1 }}>
            <strong>{slot}. ders</strong>
            <span>{slotSaatleri(izgara, slot)}</span>
          </div>
        ))}
        {slotlar.flatMap((slot) =>
          gunler.map((gun) => (
            <div
              key={`h${gun}-${slot}`}
              className="izgara-hucre"
              style={{ gridColumn: gun + 1, gridRow: slot + 1 }}
            />
          )),
        )}
        {kutular.map((kutu) => {
          const serit = seritler.get(kutu.id) ?? { serit: 0, toplam: 1 }
          const genislik = 100 / serit.toplam
          const stil = {
            gridColumn: kutu.gun + 1,
            gridRow: `${kutu.baslangic + 1} / span ${kutu.bitis - kutu.baslangic + 1}`,
            width: `calc(${genislik}% - 4px)`,
            marginLeft: `calc(${serit.serit * genislik}% + 2px)`,
            '--k': kutu.renk ?? '',
          } as CSSProperties
          return (
            <div
              key={kutu.id}
              className={kutu.renk ? 'kutu' : 'kutu hocasiz'}
              style={stil}
              title={kutu.ipucu}
            >
              <div className="kutu-baslik">
                {kutu.baslik}
                {kutu.kilitli && <span className="kilit">Kilitli</span>}
              </div>
              {kutu.altSatirlar.map((satir, i) => (
                <div key={i} className="kutu-alt">
                  {satir}
                </div>
              ))}
            </div>
          )
        })}
      </div>
    </div>
  )
}