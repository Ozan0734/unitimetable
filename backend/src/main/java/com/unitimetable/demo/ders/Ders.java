package com.unitimetable.demo.ders;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ders")
public class Ders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "proje_id", nullable = false)
    private Long projeId;

    @Column(name = "bolum_id", nullable = false)
    private Long bolumId;

    @Column(nullable = false, length = 30)
    private String kod;

    @Column(nullable = false, length = 200)
    private String ad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DersTuru tur;

    @Column(name = "haftalik_saat")
    private Integer haftalikSaat;

    @Column(name = "saat_ifadesi", length = 100)
    private String saatIfadesi;

    protected Ders() {
    }

    public Ders(Long projeId, Long bolumId, String kod, String ad, DersTuru tur,
            Integer haftalikSaat, String saatIfadesi) {
        this.projeId = projeId;
        this.bolumId = bolumId;
        this.kod = kod;
        this.ad = ad;
        this.tur = tur;
        this.haftalikSaat = haftalikSaat;
        this.saatIfadesi = saatIfadesi;
    }

    public Long getId() {
        return id;
    }

    public Long getProjeId() {
        return projeId;
    }

    public Long getBolumId() {
        return bolumId;
    }

    public String getKod() {
        return kod;
    }

    public String getAd() {
        return ad;
    }

    public DersTuru getTur() {
        return tur;
    }

    public Integer getHaftalikSaat() {
        return haftalikSaat;
    }

    public String getSaatIfadesi() {
        return saatIfadesi;
    }

    public void setKod(String kod) {
        this.kod = kod;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public void setTur(DersTuru tur) {
        this.tur = tur;
    }

    public void setSaat(Integer haftalikSaat, String saatIfadesi) {
        this.haftalikSaat = haftalikSaat;
        this.saatIfadesi = saatIfadesi;
    }
}