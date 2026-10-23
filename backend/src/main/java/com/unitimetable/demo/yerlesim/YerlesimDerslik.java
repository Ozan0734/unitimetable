package com.unitimetable.demo.yerlesim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "yerlesim_derslik")
public class YerlesimDerslik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "yerlesim_id", nullable = false)
    private Long yerlesimId;

    @Column(name = "derslik_id", nullable = false)
    private Long derslikId;

    protected YerlesimDerslik() {
    }

    public YerlesimDerslik(Long yerlesimId, Long derslikId) {
        this.yerlesimId = yerlesimId;
        this.derslikId = derslikId;
    }

    public Long getId() {
        return id;
    }

    public Long getYerlesimId() {
        return yerlesimId;
    }

    public Long getDerslikId() {
        return derslikId;
    }
}