package com.unitimetable.demo.ogretimelemani;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "hoca_musaitlik")
public class HocaMusaitlik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ogretim_elemani_id", nullable = false)
    private Long ogretimElemaniId;

    @Column(nullable = false)
    private int gun;

    @Column(nullable = false)
    private int slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MusaitlikDurumu durum;

    protected HocaMusaitlik() {
    }

    public HocaMusaitlik(Long ogretimElemaniId, int gun, int slot, MusaitlikDurumu durum) {
        this.ogretimElemaniId = ogretimElemaniId;
        this.gun = gun;
        this.slot = slot;
        this.durum = durum;
    }

    public Long getId() {
        return id;
    }

    public Long getOgretimElemaniId() {
        return ogretimElemaniId;
    }

    public int getGun() {
        return gun;
    }

    public int getSlot() {
        return slot;
    }

    public MusaitlikDurumu getDurum() {
        return durum;
    }
}