package com.unitimetable.demo.fakulte;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fakulte")
public class Fakulte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kurum_id", nullable = false)
    private Long kurumId;

    @Column(nullable = false, length = 200)
    private String ad;

    protected Fakulte() {
    }

    public Fakulte(Long kurumId, String ad) {
        this.kurumId = kurumId;
        this.ad = ad;
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

    public void setAd(String ad) {
        this.ad = ad;
    }
}