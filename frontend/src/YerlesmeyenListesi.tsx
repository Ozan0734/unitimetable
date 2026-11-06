import type { YerlesmeyenBlok } from './tipler'

interface Props {
  bloklar: YerlesmeyenBlok[]
  surukleBasla: (blok: YerlesmeyenBlok) => void
  surukleBitti: () => void
}

export default function YerlesmeyenListesi({ bloklar, surukleBasla, surukleBitti }: Props) {
  if (bloklar.length === 0) {
    return <p className="soluk">Yerleşmeyen blok yok, tüm dersler yerleşti.</p>
  }
  return (
    <div className="chip-listesi">
      {bloklar.map((b) => (
        <div
          key={b.blokId}
          className="chip"
          draggable
          title="Izgaraya sürükleyip bırakın"
          onDragStart={(olay) => {
            olay.dataTransfer.effectAllowed = 'move'
            olay.dataTransfer.setData('text/plain', String(b.blokId))
            surukleBasla(b)
          }}
          onDragEnd={surukleBitti}
        >
          <div>
            <strong>{b.dersKodu}</strong> <span className="soluk">Şube {b.subeNo} · Blok {b.siraNo}</span>{' '}
            <span className="rozet">{b.sure} saat</span>
          </div>
          <div className="chip-alt">{b.dersAdi}</div>
        </div>
      ))}
    </div>
  )
}