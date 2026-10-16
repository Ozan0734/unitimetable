package com.unitimetable.demo.derslik;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "derslik_bolum")
public class DerslikBolum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "derslik_id", nullable = false)
    private Long derslikId;

    @Column(name = "bolum_id", nullable = false)
    private Long bolumId;

    @Column(name = "ortak_mi", nullable = false)
    private boolean ortakMi;

    protected DerslikBolum() {
    }

    public DerslikBolum(Long derslikId, Long bolumId, boolean ortakMi) {
        this.derslikId = derslikId;
        this.bolumId = bolumId;
        this.ortakMi = ortakMi;
    }

    public Long getId() {
        return id;
    }

    public Long getDerslikId() {
        return derslikId;
    }

    public Long getBolumId() {
        return bolumId;
    }

    public boolean isOrtakMi() {
        return ortakMi;
    }

    public void setOrtakMi(boolean ortakMi) {
        this.ortakMi = ortakMi;
    }
}