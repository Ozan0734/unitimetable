package com.unitimetable.demo.ders;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ders_sinif")
public class DersSinif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ders_id", nullable = false)
    private Long dersId;

    @Column(name = "sinif_id", nullable = false)
    private Long sinifId;

    protected DersSinif() {
    }

    public DersSinif(Long dersId, Long sinifId) {
        this.dersId = dersId;
        this.sinifId = sinifId;
    }

    public Long getId() {
        return id;
    }

    public Long getDersId() {
        return dersId;
    }

    public Long getSinifId() {
        return sinifId;
    }
}