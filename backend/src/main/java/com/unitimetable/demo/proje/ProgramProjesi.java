package com.unitimetable.demo.proje;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "program_projesi")
public class ProgramProjesi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kurum_id", nullable = false)
    private Long kurumId;

    @Column(name = "bolum_id", nullable = false)
    private Long bolumId;

    @Column(nullable = false, length = 200)
    private String ad;

    @Column(nullable = false, length = 50)
    private String donem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjeDurumu durum;

    @Column(name = "olusturma_tarihi", nullable = false, updatable = false)
    private LocalDateTime olusturmaTarihi;

    protected ProgramProjesi() {
    }

    public ProgramProjesi(Long kurumId, Long bolumId, String ad, String donem) {
        this.kurumId = kurumId;
        this.bolumId = bolumId;
        this.ad = ad;
        this.donem = donem;
        this.durum = ProjeDurumu.TASLAK;
        this.olusturmaTarihi = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getKurumId() {
        return kurumId;
    }

    public Long getBolumId() {
        return bolumId;
    }

    public String getAd() {
        return ad;
    }

    public String getDonem() {
        return donem;
    }

    public ProjeDurumu getDurum() {
        return durum;
    }

    public LocalDateTime getOlusturmaTarihi() {
        return olusturmaTarihi;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public void setDonem(String donem) {
        this.donem = donem;
    }

    public void setDurum(ProjeDurumu durum) {
        this.durum = durum;
    }
}