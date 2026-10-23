package com.unitimetable.demo.yerlesim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "yerlesim")
public class Yerlesim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blok_id", nullable = false)
    private Long blokId;

    @Column(nullable = false)
    private int gun;

    @Column(name = "baslangic_slot", nullable = false)
    private int baslangicSlot;

    @Column(nullable = false)
    private boolean kilitli;

    protected Yerlesim() {
    }

    public Yerlesim(Long blokId, int gun, int baslangicSlot) {
        this.blokId = blokId;
        this.gun = gun;
        this.baslangicSlot = baslangicSlot;
        this.kilitli = false;
    }

    public Long getId() {
        return id;
    }

    public Long getBlokId() {
        return blokId;
    }

    public int getGun() {
        return gun;
    }

    public int getBaslangicSlot() {
        return baslangicSlot;
    }

    public boolean isKilitli() {
        return kilitli;
    }

    public void tasi(int gun, int baslangicSlot) {
        this.gun = gun;
        this.baslangicSlot = baslangicSlot;
    }

    public void setKilitli(boolean kilitli) {
        this.kilitli = kilitli;
    }
}