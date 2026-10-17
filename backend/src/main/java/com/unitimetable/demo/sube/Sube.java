package com.unitimetable.demo.sube;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sube")
public class Sube {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ders_id", nullable = false)
    private Long dersId;

    @Column(name = "ogretim_elemani_id")
    private Long ogretimElemaniId;

    @Column(name = "sube_no", nullable = false)
    private int subeNo;

    @Column(name = "tahmini_ogrenci")
    private Integer tahminiOgrenci;

    @Column(name = "sinif_cakisma_izni", nullable = false)
    private boolean sinifCakismaIzni;

    @Column(name = "derslik_cakisma_izni", nullable = false)
    private boolean derslikCakismaIzni;

    @Column(name = "coklu_derslik_izni", nullable = false)
    private boolean cokluDerslikIzni;

    protected Sube() {
    }

    public Sube(Long dersId, Long ogretimElemaniId, int subeNo, Integer tahminiOgrenci,
            boolean sinifCakismaIzni, boolean derslikCakismaIzni, boolean cokluDerslikIzni) {
        this.dersId = dersId;
        this.ogretimElemaniId = ogretimElemaniId;
        this.subeNo = subeNo;
        this.tahminiOgrenci = tahminiOgrenci;
        this.sinifCakismaIzni = sinifCakismaIzni;
        this.derslikCakismaIzni = derslikCakismaIzni;
        this.cokluDerslikIzni = cokluDerslikIzni;
    }

    public Long getId() {
        return id;
    }

    public Long getDersId() {
        return dersId;
    }

    public Long getOgretimElemaniId() {
        return ogretimElemaniId;
    }

    public int getSubeNo() {
        return subeNo;
    }

    public Integer getTahminiOgrenci() {
        return tahminiOgrenci;
    }

    public boolean isSinifCakismaIzni() {
        return sinifCakismaIzni;
    }

    public boolean isDerslikCakismaIzni() {
        return derslikCakismaIzni;
    }

    public boolean isCokluDerslikIzni() {
        return cokluDerslikIzni;
    }

    public void guncelle(Long ogretimElemaniId, Integer tahminiOgrenci,
            boolean sinifCakismaIzni, boolean derslikCakismaIzni, boolean cokluDerslikIzni) {
        this.ogretimElemaniId = ogretimElemaniId;
        this.tahminiOgrenci = tahminiOgrenci;
        this.sinifCakismaIzni = sinifCakismaIzni;
        this.derslikCakismaIzni = derslikCakismaIzni;
        this.cokluDerslikIzni = cokluDerslikIzni;
    }
}