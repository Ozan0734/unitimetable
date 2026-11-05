package com.unitimetable.demo.izgara;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "zaman_izgarasi")
public class ZamanIzgarasi {

    @Id
    private Long id;

    @Column(name = "kurum_id", nullable = false)
    private Long kurumId;

    @Column(name = "gun_sayisi", nullable = false)
    private int gunSayisi;

    @Column(name = "gunluk_slot_sayisi", nullable = false)
    private int gunlukSlotSayisi;

    @Column(name = "ders_suresi_dk", nullable = false)
    private int dersSuresiDk;

    @Column(name = "ara_suresi_dk", nullable = false)
    private int araSuresiDk;

    @Column(name = "baslangic_saati", nullable = false)
    private LocalTime baslangicSaati;

    protected ZamanIzgarasi() {
    }

    public Long getId() {
        return id;
    }

    public Long getKurumId() {
        return kurumId;
    }

    public int getGunSayisi() {
        return gunSayisi;
    }

    public int getGunlukSlotSayisi() {
        return gunlukSlotSayisi;
    }

    public int getDersSuresiDk() {
        return dersSuresiDk;
    }

    public int getAraSuresiDk() {
        return araSuresiDk;
    }

    public LocalTime getBaslangicSaati() {
        return baslangicSaati;
    }
}