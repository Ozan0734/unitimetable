package com.unitimetable.demo.sinif;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sinif")
public class Sinif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "program_id", nullable = false)
    private Long programId;

    @Column(nullable = false, length = 100)
    private String ad;

    @Column(nullable = false)
    private int duzey;

    protected Sinif() {
    }

    public Sinif(Long programId, String ad, int duzey) {
        this.programId = programId;
        this.ad = ad;
        this.duzey = duzey;
    }

    public Long getId() {
        return id;
    }

    public Long getProgramId() {
        return programId;
    }

    public String getAd() {
        return ad;
    }

    public int getDuzey() {
        return duzey;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public void setDuzey(int duzey) {
        this.duzey = duzey;
    }
}