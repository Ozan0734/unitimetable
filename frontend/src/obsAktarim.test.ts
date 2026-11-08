import { describe, expect, it } from 'vitest'
import { aramaEslesir, formAlanlari, grupDurumu, kodListesi, sadelestir, sinifTahmini } from './obsAktarim'
import type { ObsDers, ObsSecmeliGrup } from './tipler'

function ders(kod: string, ad = 'DERS', haftalikSaat = 3, hocaAd = ''): ObsDers {
  return {
    satir: 2,
    kod,
    ad,
    zorunlu: true,
    sinif: 1,
    donem: 'G',
    teo: haftalikSaat,
    uyg: 0,
    lab: 0,
    haftalikSaat,
    onerilenSaatIfadesi: String(haftalikSaat),
    kredi: '3',
    akts: '5',
    hocaUnvan: '',
    hocaAd,
    intibak: '',
    onKosul: '',
    etiket: '',
  }
}

describe('sadelestir', () => {
  it('Türkçe karakterleri ve büyük küçük harf farkını eşitler', () => {
    expect(sadelestir('İNGİLİZCE')).toBe('ingilizce')
    expect(sadelestir('ISG 401E')).toBe('isg 401e')
    expect(sadelestir('Öğretim Üyesi ŞÇĞ')).toBe('ogretim uyesi scg')
  })
})

describe('aramaEslesir', () => {
  it('boş aramada hepsini, doluda kod, ad ve hoca ile eşleşir', () => {
    const d = ders('SEC 101.11', 'ORGANIZATIONAL BEHAVIOUR', 3, 'MEHMET ÖZER DEMİR')
    expect(aramaEslesir(d, '')).toBe(true)
    expect(aramaEslesir(d, '   ')).toBe(true)
    expect(aramaEslesir(d, 'sec 101.11')).toBe(true)
    expect(aramaEslesir(d, 'behaviour')).toBe(true)
    expect(aramaEslesir(d, 'ozer demir')).toBe(true)
    expect(aramaEslesir(d, 'ekonomi')).toBe(false)
  })
})

describe('kodListesi', () => {
  it('virgül, noktalı virgül ve satır sonuyla ayırır, tekrarları atar', () => {
    expect(kodListesi('ata, tdb ,\n isg;ATA')).toEqual(['ATA', 'TDB', 'ISG'])
    expect(kodListesi('com   401')).toEqual(['COM 401'])
    expect(kodListesi('')).toEqual([])
  })
})

describe('formAlanlari', () => {
  const secim = {
    donem: 'GUZ' as const,
    projeId: 4,
    programId: 7,
    secmeliDersler: ['SEC 101.11', 'SEC 401.1'],
    sanalOnekleri: ['ATA', 'TDB'],
    seminerKodlari: ['COM 401'],
  }

  it('seçmeli dersleri ve kodları virgülle birleştirir', () => {
    expect(formAlanlari(secim)).toEqual([
      ['donem', 'GUZ'],
      ['projeId', '4'],
      ['programId', '7'],
      ['sanalOnekleri', 'ATA,TDB'],
      ['secmeliDersler', 'SEC 101.11,SEC 401.1'],
      ['seminerKodlari', 'COM 401'],
    ])
  })

  it('boş seçmeliyi ve seminer kodunu göndermez ama sanal önekleri her zaman gider', () => {
    const alanlar = formAlanlari({ ...secim, secmeliDersler: [], seminerKodlari: [], sanalOnekleri: [] })
    expect(alanlar.map(([ad]) => ad)).toEqual(['donem', 'projeId', 'programId', 'sanalOnekleri'])
    expect(alanlar[3]).toEqual(['sanalOnekleri', ''])
  })
})

describe('sinifTahmini', () => {
  const sanal = ['ATA', 'TDB', 'ISG']

  it('sanal önekleri tanır ama benzer kodları sanal saymaz', () => {
    expect(sinifTahmini(ders('ATA 101'), sanal, [])).toEqual({ tur: 'NORMAL', sanal: true })
    expect(sinifTahmini(ders('ISG 401E'), sanal, [])).toEqual({ tur: 'NORMAL', sanal: true })
    expect(sinifTahmini(ders('ATAMER 1'), sanal, [])).toEqual({ tur: 'NORMAL', sanal: false })
    expect(sinifTahmini(ders('COM 101'), sanal, [])).toEqual({ tur: 'NORMAL', sanal: false })
  })

  it('seminer kodunu seminer ve sanal yapar', () => {
    const seminer = ders('COM 401', 'COMPUTER ENGINEERING SEMINAR', 4)
    expect(sinifTahmini(seminer, sanal, ['COM 401'])).toEqual({ tur: 'SEMINER', sanal: true })
    expect(sinifTahmini(seminer, sanal, [])).toEqual({ tur: 'NORMAL', sanal: false })
  })

  it('sıfır saatli ve bitirme derslerini yerleşimsiz sayar', () => {
    expect(sinifTahmini(ders('COM 301', 'INTERNSHIP I', 0), sanal, [])).toEqual({
      tur: 'YERLESIMSIZ',
      sanal: false,
    })
    for (const ad of ['GRADUATION DESIGN PROJECT', 'BİTİRME PROJESİ', 'Graduation Thesis', 'bitirme tezi']) {
      expect(sinifTahmini(ders('COM 402', ad, 6), sanal, []).tur).toBe('YERLESIMSIZ')
    }
  })
})

describe('grupDurumu', () => {
  it('gruptaki seçili ders sayısını verir', () => {
    const grup: ObsSecmeliGrup = {
      satir: 2,
      kod: 'SEC 101',
      ad: 'NON-TECH',
      sinif: 1,
      dersler: [ders('SEC 101.1'), ders('SEC 101.2'), ders('SEC 101.3')],
    }
    expect(grupDurumu(grup, new Set(['SEC 101.2', 'SEC 999']))).toEqual({ secili: 1, toplam: 3 })
    expect(grupDurumu(grup, new Set())).toEqual({ secili: 0, toplam: 3 })
  })
})