import { useState } from 'react'
import type { CSSProperties } from 'react'
import { Link, useParams } from 'react-router-dom'
import './ciktiSayfasi.css'
import {
  derslikVarliklari,
  hocaVarliklari,
  hucreBasligi,
  hucreSatirlari,
  sinifVarliklari,
  tabloOlustur,
  varlikKutulari,
} from './programCikti'
import type { CiktiGorunumu, CiktiVerisi, TabloGunu, Varlik } from './programCikti'
import type {
  AkademikProgram,
  Bolum,
  Derslik,
  Ders,
  DersSinif,
  OgretimElemani,
  ProgramProjesi,
  Sinif,
  Sube,
  Yerlesim,
  ZamanIzgarasi,
} from './tipler'
import { useYukle } from './useYukle'
import { GUN_ADLARI, slotSaatleri } from './zaman'

interface Fakulte {
  id: number
  kurumId: number
  ad: string
}

const GORUNUM_ADLARI: Record<CiktiGorunumu, string> = {
  SINIF: 'Sınıf',
  HOCA: 'Öğretim elemanı',
  DERSLIK: 'Derslik',
}

interface SayfaProps {
  varlik: Varlik
  gorunum: CiktiGorunumu
  tablo: TabloGunu[]
  izgara: ZamanIzgarasi
  kurumSatiri: string
  donem: string
  projeAdi: string
  tarih: string
}

function CiktiSayfasi({ varlik, gorunum, tablo, izgara, kurumSatiri, donem, projeAdi, tarih }: SayfaProps) {
  const slotlar = Array.from({ length: izgara.gunlukSlotSayisi }, (_, i) => i + 1)
  return (
    <section className="cikti-sayfa">
      <div className="cikti-ust">
        <div className="cikti-logo-alani">
          <img
            className="cikti-logo"
            src="/alku-logo.png"
            alt=""
            onError={(olay) => {
              olay.currentTarget.style.display = 'none'
            }}
          />
        </div>
        <div className="cikti-baslik">
          <h1>{varlik.baslik}</h1>
          <div className="cikti-alt">
            {varlik.altBaslik} · {donem}
          </div>
        </div>
        <div className="cikti-logo-alani" />
      </div>
      {kurumSatiri !== '' && <div className="cikti-kurum">{kurumSatiri}</div>}

      <table className="cikti-tablo">
        <thead>
          <tr>
            <th className="gun-hucre" />
            {slotlar.map((slot) => (
              <th key={slot}>
                <strong>{slot}.Ders</strong>
                <span>{slotSaatleri(izgara, slot).replace('–', ' - ')}</span>
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {tablo.map((gun) =>
            gun.satirlar.map((satir, satirNo) => (
              <tr key={`${gun.gun}-${satirNo}`}>
                {satirNo === 0 && (
                  <th className="gun-hucre" rowSpan={gun.satirlar.length}>
                    {GUN_ADLARI[gun.gun - 1]}
                  </th>
                )}
                {satir.map((hucre, i) =>
                  hucre.tur === 'bos' ? (
                    <td key={i} />
                  ) : (
                    <td
                      key={i}
                      colSpan={hucre.genislik}
                      className={hucre.kutu.renk ? 'ders-hucre' : 'ders-hucre hocasiz'}
                      style={{ '--k': hucre.kutu.renk ?? '' } as CSSProperties}
                    >
                      <div className="k-kod">{hucreBasligi(hucre.kutu)}</div>
                      {hucreSatirlari(hucre.kutu, gorunum).map((metin, j) => (
                        <div key={j} className={j === 0 ? 'k-ad' : 'k-satir'}>
                          {metin}
                        </div>
                      ))}
                    </td>
                  ),
                )}
              </tr>
            )),
          )}
        </tbody>
      </table>

      <div className="cikti-ayak">
        <span>Ders Planı Oluşturuldu: {tarih}</span>
        <span>{projeAdi}</span>
      </div>
    </section>
  )
}

export default function ProjeCikti() {
  const { projeId } = useParams()
  const kimlik = projeId ? encodeURIComponent(projeId) : null
  const proje = useYukle<ProgramProjesi>(kimlik ? `/api/program-projeleri/${kimlik}` : null)
  const p = proje.veri
  const dersler = useYukle<Ders[]>(p ? `/api/dersler?projeId=${p.id}` : null)
  const yerlesimler = useYukle<Yerlesim[]>(p ? `/api/yerlesimler?projeId=${p.id}` : null)
  const hocalar = useYukle<OgretimElemani[]>(p ? `/api/ogretim-elemanlari?bolumId=${p.bolumId}` : null)
  const derslikler = useYukle<Derslik[]>(p ? `/api/derslikler?kurumId=${p.kurumId}` : null)
  const programlar = useYukle<AkademikProgram[]>(p ? `/api/programlar?bolumId=${p.bolumId}` : null)
  const siniflar = useYukle<Sinif[]>(p ? '/api/siniflar' : null)
  const dersSiniflari = useYukle<DersSinif[]>(p ? '/api/ders-sinif' : null)
  const subeler = useYukle<Sube[]>(p ? '/api/subeler' : null)
  const izgara = useYukle<ZamanIzgarasi>(p ? '/api/zaman-izgarasi' : null)
  const bolumler = useYukle<Bolum[]>(p ? '/api/bolumler' : null)
  const fakulteler = useYukle<Fakulte[]>(p ? '/api/fakulteler' : null)
  const [gorunum, setGorunum] = useState<CiktiGorunumu>('SINIF')
  const [secim, setSecim] = useState<number | null>(null)
  const [bosDahil, setBosDahil] = useState(false)

  const hata = [
    proje,
    dersler,
    yerlesimler,
    hocalar,
    derslikler,
    programlar,
    siniflar,
    dersSiniflari,
    subeler,
    izgara,
    bolumler,
    fakulteler,
  ]
    .map((y) => y.hata)
    .find((h) => h !== undefined)
  if (hata) {
    return (
      <>
        <p className="hata" role="alert">
          {hata}
        </p>
        <Link to="/projeler">← Projelere dön</Link>
      </>
    )
  }

  const { veri: ds } = dersler
  const { veri: yl } = yerlesimler
  const { veri: hc } = hocalar
  const { veri: dl } = derslikler
  const { veri: pr } = programlar
  const { veri: sn } = siniflar
  const { veri: dsn } = dersSiniflari
  const { veri: sb } = subeler
  const { veri: ig } = izgara
  const { veri: bl } = bolumler
  const { veri: fk } = fakulteler
  if (!p || !ds || !yl || !hc || !dl || !pr || !sn || !dsn || !sb || !ig || !bl || !fk) {
    return <p className="soluk">Yükleniyor…</p>
  }

  const veri: CiktiVerisi = {
    dersler: ds,
    yerlesimler: yl,
    subeler: sb,
    hocalar: hc,
    derslikler: dl,
    dersSiniflari: dsn,
    gunSayisi: ig.gunSayisi,
    slotSayisi: ig.gunlukSlotSayisi,
  }

  const varliklar =
    gorunum === 'SINIF' ? sinifVarliklari(pr, sn) : gorunum === 'HOCA' ? hocaVarliklari(hc) : derslikVarliklari(dl)
  const kutular = new Map(varliklar.map((v) => [v.id, varlikKutulari(gorunum, v.id, veri)] as const))
  const etkinSecim = secim !== null && varliklar.some((v) => v.id === secim) ? secim : null
  const yazdirilacaklar = varliklar.filter((v) =>
    etkinSecim !== null ? v.id === etkinSecim : bosDahil || (kutular.get(v.id)?.length ?? 0) > 0,
  )

  const bolum = bl.find((b) => b.id === p.bolumId)
  const fakulte = fk.find((f) => f.id === bolum?.fakulteId)
  const kurumSatiri = [fakulte?.ad, bolum?.ad].filter((s): s is string => s !== undefined && s !== '').join(' · ')
  const tarih = new Date().toLocaleDateString('tr-TR')

  return (
    <div className="cikti-kap">
      <div className="cikti-araclari">
        <Link to={`/projeler/${p.id}`}>← Projeye dön</Link>
        <label>
          Görünüm
          <select
            value={gorunum}
            onChange={(olay) => {
              setGorunum(olay.target.value as CiktiGorunumu)
              setSecim(null)
            }}
          >
            {(Object.keys(GORUNUM_ADLARI) as CiktiGorunumu[]).map((g) => (
              <option key={g} value={g}>
                {GORUNUM_ADLARI[g]}
              </option>
            ))}
          </select>
        </label>
        <label>
          Kapsam
          <select
            value={etkinSecim ?? ''}
            onChange={(olay) => setSecim(olay.target.value === '' ? null : Number(olay.target.value))}
          >
            <option value="">Tümü ({yazdirilacaklar.length} sayfa)</option>
            {varliklar.map((v) => (
              <option key={v.id} value={v.id}>
                {v.baslik} ({kutular.get(v.id)?.length ?? 0} blok)
              </option>
            ))}
          </select>
        </label>
        <label className="onay-kutusu">
          <input type="checkbox" checked={bosDahil} onChange={(olay) => setBosDahil(olay.target.checked)} />
          Programı olmayanları da ekle
        </label>
        <button type="button" disabled={yazdirilacaklar.length === 0} onClick={() => window.print()}>
          Yazdır / PDF olarak kaydet
        </button>
      </div>
      <p className="soluk cikti-ipucu">
        Açılan yazdırma penceresinde <strong>Hedef: PDF olarak kaydet</strong>, <strong>Kağıt boyutu: A4</strong>,{' '}
        <strong>Yönlendirme: Yatay</strong> seçin ve <strong>Arka plan grafikleri</strong> kutusunu işaretleyin. Logo için{' '}
        <code>frontend/public/alku-logo.png</code> dosyası eklenebilir.
      </p>

      {yazdirilacaklar.length === 0 ? (
        <p className="soluk">
          Bu görünümde yazdırılacak program yok. Önce blokları yerleştirin ya da &quot;Programı olmayanları da
          ekle&quot; kutusunu işaretleyin.
        </p>
      ) : (
        <div className="cikti-sayfalar">
          {yazdirilacaklar.map((v) => (
            <CiktiSayfasi
              key={v.id}
              varlik={v}
              gorunum={gorunum}
              tablo={tabloOlustur(kutular.get(v.id) ?? [], ig.gunSayisi, ig.gunlukSlotSayisi)}
              izgara={ig}
              kurumSatiri={kurumSatiri}
              donem={p.donem}
              projeAdi={p.ad}
              tarih={tarih}
            />
          ))}
        </div>
      )}
    </div>
  )
}
