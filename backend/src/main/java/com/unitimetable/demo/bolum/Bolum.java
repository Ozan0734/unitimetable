package com.unitimetable.demo.bolum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bolum")
public class Bolum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fakulte_id", nullable = false)
    private Long fakulteId;

    @Column(nullable = false, length = 200)
    private String ad;

    protected Bolum() {
    }

    public Bolum(Long fakulteId, String ad) {
        this.fakulteId = fakulteId;
        this.ad = ad;
    }

    public Long getId() {
        return id;
    }

    public Long getFakulteId() {
        return fakulteId;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }
}