package com.unitimetable.demo.ogretimelemani;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ogretim_elemani")
public class OgretimElemani {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bolum_id", nullable = false)
    private Long bolumId;

    @Column(name = "ad_soyad", nullable = false, length = 200)
    private String adSoyad;

    @Column(length = 50)
    private String unvan;

    @Column(length = 7)
    private String renk;

    @Column(nullable = false)
    private int oncelik;

    protected OgretimElemani() {
    }

    public OgretimElemani(Long bolumId, String adSoyad, String unvan, String renk, int oncelik) {
        this.bolumId = bolumId;
        this.adSoyad = adSoyad;
        this.unvan = unvan;
        this.renk = renk;
        this.oncelik = oncelik;
    }

    public Long getId() {
        return id;
    }

    public Long getBolumId() {
        return bolumId;
    }

    public String getAdSoyad() {
        return adSoyad;
    }

    public String getUnvan() {
        return unvan;
    }

    public String getRenk() {
        return renk;
    }

    public int getOncelik() {
        return oncelik;
    }

    public void setAdSoyad(String adSoyad) {
        this.adSoyad = adSoyad;
    }

    public void setUnvan(String unvan) {
        this.unvan = unvan;
    }

    public void setRenk(String renk) {
        this.renk = renk;
    }

    public void setOncelik(int oncelik) {
        this.oncelik = oncelik;
    }
}