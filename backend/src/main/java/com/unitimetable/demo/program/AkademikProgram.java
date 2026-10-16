package com.unitimetable.demo.program;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "akademik_program")
public class AkademikProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bolum_id", nullable = false)
    private Long bolumId;

    @Column(nullable = false, length = 200)
    private String ad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Seviye seviye;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private Dil dil;

    protected AkademikProgram() {
    }

    public AkademikProgram(Long bolumId, String ad, Seviye seviye, Dil dil) {
        this.bolumId = bolumId;
        this.ad = ad;
        this.seviye = seviye;
        this.dil = dil;
    }

    public Long getId() {
        return id;
    }

    public Long getBolumId() {
        return bolumId;
    }

    public String getAd() {
        return ad;
    }

    public Seviye getSeviye() {
        return seviye;
    }

    public Dil getDil() {
        return dil;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public void setSeviye(Seviye seviye) {
        this.seviye = seviye;
    }

    public void setDil(Dil dil) {
        this.dil = dil;
    }
}