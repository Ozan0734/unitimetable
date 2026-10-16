package com.unitimetable.demo.derslik;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "derslik")
public class Derslik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kurum_id", nullable = false)
    private Long kurumId;

    @Column(nullable = false, length = 100)
    private String ad;

    @Column(nullable = false)
    private int kapasite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DerslikTuru tur;

    protected Derslik() {
    }

    public Derslik(Long kurumId, String ad, int kapasite, DerslikTuru tur) {
        this.kurumId = kurumId;
        this.ad = ad;
        this.kapasite = kapasite;
        this.tur = tur;
    }

    public Long getId() {
        return id;
    }

    public Long getKurumId() {
        return kurumId;
    }

    public String getAd() {
        return ad;
    }

    public int getKapasite() {
        return kapasite;
    }

    public DerslikTuru getTur() {
        return tur;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public void setKapasite(int kapasite) {
        this.kapasite = kapasite;
    }

    public void setTur(DerslikTuru tur) {
        this.tur = tur;
    }
}