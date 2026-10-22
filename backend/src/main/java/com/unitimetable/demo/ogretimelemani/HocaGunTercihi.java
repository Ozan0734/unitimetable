package com.unitimetable.demo.ogretimelemani;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "hoca_gun_tercihi")
public class HocaGunTercihi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ogretim_elemani_id", nullable = false)
    private Long ogretimElemaniId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GunTercihTuru tur;

    private Integer deger;

    private Integer gun;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TercihKatiligi katilik;

    @Column(name = "asistana_birakildi", nullable = false)
    private boolean asistanaBirakildi;

    protected HocaGunTercihi() {
    }

    public HocaGunTercihi(Long ogretimElemaniId, GunTercihTuru tur, Integer deger, Integer gun,
            TercihKatiligi katilik, boolean asistanaBirakildi) {
        this.ogretimElemaniId = ogretimElemaniId;
        this.tur = tur;
        this.deger = deger;
        this.gun = gun;
        this.katilik = katilik;
        this.asistanaBirakildi = asistanaBirakildi;
    }

    public Long getId() {
        return id;
    }

    public Long getOgretimElemaniId() {
        return ogretimElemaniId;
    }

    public GunTercihTuru getTur() {
        return tur;
    }

    public Integer getDeger() {
        return deger;
    }

    public Integer getGun() {
        return gun;
    }

    public TercihKatiligi getKatilik() {
        return katilik;
    }

    public boolean isAsistanaBirakildi() {
        return asistanaBirakildi;
    }
}