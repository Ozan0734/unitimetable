package com.unitimetable.demo.derslik;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lab_gun_dagilimi")
public class LabGunDagilimi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "derslik_id", nullable = false)
    private Long derslikId;

    @Column(name = "bolum_id", nullable = false)
    private Long bolumId;

    @Column(nullable = false)
    private int gun;

    protected LabGunDagilimi() {
    }

    public LabGunDagilimi(Long derslikId, Long bolumId, int gun) {
        this.derslikId = derslikId;
        this.bolumId = bolumId;
        this.gun = gun;
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

    public int getGun() {
        return gun;
    }

    public void setBolumId(Long bolumId) {
        this.bolumId = bolumId;
    }
}