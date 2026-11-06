import { useRef } from 'react'
import type { CSSProperties } from 'react'
import { anahtar, hucreBul } from './birakma'
import type { Geometri } from './birakma'
import { seritleriBul } from './seritler'
import { GUN_ADLARI, slotSaatleri } from './zaman'
import type { YerDurumu, ZamanIzgarasi } from './tipler'

export interface Kutu {
  id: number
  blokId: number
  sure: number
  derslikIdler: number[]
  gun: number
  baslangic: number
  bitis: number
  baslik: string
  altSatirlar: string[]
  renk: string | null
  kilitli: boolean
  hareketEdebilir: boolean
  ipucu: string
}

export interface Hayalet {
  gun: number
  baslangic: number
  sure: number
  durum: YerDurumu
}

interface Props {
  izgara: ZamanIzgarasi
  kutular: Kutu[]
  suruklenen: boolean
  hucreRenkleri: Map<string, YerDurumu> | null
  hayalet: Hayalet | null
  seciliId: number | null
  kutuTikla: (kutu: Kutu) => void
  kutuSurukleBasla: (kutu: Kutu, ofset: number) => void
  surukleBitti: () => void
  ustunde: (hucre: { gun: number; slot: number } | null) => void
  birak: (hucre: { gun: number; slot: number }) => void
}

const SATIR_YUKSEKLIGI = 64
const SAAT_SUTUNU = 96

const DURUM_SINIFLARI: Record<YerDurumu, string> = {
  UYGUN: 'hucre-uygun',
  UYARILI: 'hucre-uyarili',
  ENGELLI: 'hucre-engelli',
}

export default function Izgara({
  izgara,
  kutular,
  suruklenen,
  hucreRenkleri,
  hayalet,
  seciliId,
  kutuTikla,
  kutuSurukleBasla,
  surukleBitti,
  ustunde,
  birak,
}: Props) {
  const izgaraRef = useRef<HTMLDivElement>(null)
  const baslikRef = useRef<HTMLDivElement>(null)
  const seritler = seritleriBul(kutular)
  const gunler = Array.from({ length: izgara.gunSayisi }, (_, i) => i + 1)
  const slotlar = Array.from({ length: izgara.gunlukSlotSayisi }, (_, i) => i + 1)

  const izgaraStili: CSSProperties = {
    gridTemplateColumns: `${SAAT_SUTUNU}px repeat(${izgara.gunSayisi}, minmax(130px, 1fr))`,
    gridTemplateRows: `auto repeat(${izgara.gunlukSlotSayisi}, ${SATIR_YUKSEKLIGI}px)`,
  }

  function geometri(): Geometri | null {
    const eleman = izgaraRef.current
    const baslik = baslikRef.current
    if (!eleman || !baslik) {
      return null
    }
    const kutu = eleman.getBoundingClientRect()
    return {
      sol: kutu.left,
      ust: kutu.top,
      genislik: kutu.width,
      baslikYuksekligi: baslik.offsetHeight,
      saatSutunu: SAAT_SUTUNU,
      satirYuksekligi: SATIR_YUKSEKLIGI,
      gunSayisi: izgara.gunSayisi,
      slotSayisi: izgara.gunlukSlotSayisi,
    }
  }

  return (
    <div className="izgara-kap">
      <div
        ref={izgaraRef}
        className="izgara"
        style={izgaraStili}
        onDragOver={(olay) => {
          if (!suruklenen) {
            return
          }
          olay.preventDefault()
          olay.dataTransfer.dropEffect = 'move'
          const g = geometri()
          ustunde(g ? hucreBul(olay.clientX, olay.clientY, g) : null)
        }}
        onDragLeave={(olay) => {
          if (!olay.currentTarget.contains(olay.relatedTarget as Node | null)) {
            ustunde(null)
          }
        }}
        onDrop={(olay) => {
          if (!suruklenen) {
            return
          }
          olay.preventDefault()
          const g = geometri()
          const hucre = g ? hucreBul(olay.clientX, olay.clientY, g) : null
          if (hucre) {
            birak(hucre)
          }
        }}
      >
        <div ref={baslikRef} className="izgara-baslik" style={{ gridColumn: 1, gridRow: 1 }} />
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
          gunler.map((gun) => {
            const durum = hucreRenkleri?.get(anahtar(gun, slot))
            return (
              <div
                key={`h${gun}-${slot}`}
                className={durum ? `izgara-hucre ${DURUM_SINIFLARI[durum]}` : 'izgara-hucre'}
                style={{ gridColumn: gun + 1, gridRow: slot + 1 }}
              />
            )
          }),
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
          const siniflar = ['kutu', kutu.renk ? '' : 'hocasiz', seciliId === kutu.id ? 'secili' : '']
            .filter((s) => s !== '')
            .join(' ')
          return (
            <div
              key={kutu.id}
              className={siniflar}
              style={stil}
              title={kutu.ipucu}
              draggable={kutu.hareketEdebilir}
              onClick={() => kutuTikla(kutu)}
              onDragStart={(olay) => {
                olay.dataTransfer.effectAllowed = 'move'
                olay.dataTransfer.setData('text/plain', String(kutu.blokId))
                const ust = olay.currentTarget.getBoundingClientRect().top
                const satir = Math.floor((olay.clientY - ust) / SATIR_YUKSEKLIGI)
                kutuSurukleBasla(kutu, Math.min(kutu.sure - 1, Math.max(0, satir)))
              }}
              onDragEnd={surukleBitti}
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
        {hayalet && (
          <div
            className={`hayalet ${DURUM_SINIFLARI[hayalet.durum]}`}
            style={{
              gridColumn: hayalet.gun + 1,
              gridRow: `${hayalet.baslangic + 1} / span ${hayalet.sure}`,
            }}
          />
        )}
      </div>
    </div>
  )
}