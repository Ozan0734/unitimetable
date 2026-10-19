package com.unitimetable.demo.kullanici;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "kullanici")
public class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kurum_id", nullable = false)
    private Long kurumId;

    @Column(name = "fakulte_id")
    private Long fakulteId;

    @Column(name = "bolum_id")
    private Long bolumId;

    @Column(name = "ad_soyad", nullable = false, length = 200)
    private String adSoyad;

    @Column(nullable = false, length = 200)
    private String eposta;

    @Column(name = "sifre_hash", nullable = false, length = 100)
    private String sifreHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KullaniciRolu rol;

    protected Kullanici() {
    }

    public Kullanici(Long kurumId, Long fakulteId, Long bolumId, String adSoyad, String eposta,
            String sifreHash, KullaniciRolu rol) {
        this.kurumId = kurumId;
        this.fakulteId = fakulteId;
        this.bolumId = bolumId;
        this.adSoyad = adSoyad;
        this.eposta = eposta;
        this.sifreHash = sifreHash;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public Long getKurumId() {
        return kurumId;
    }

    public Long getFakulteId() {
        return fakulteId;
    }

    public Long getBolumId() {
        return bolumId;
    }

    public String getAdSoyad() {
        return adSoyad;
    }

    public String getEposta() {
        return eposta;
    }

    public String getSifreHash() {
        return sifreHash;
    }

    public KullaniciRolu getRol() {
        return rol;
    }

    public void guncelle(String adSoyad, KullaniciRolu rol, Long fakulteId, Long bolumId) {
        this.adSoyad = adSoyad;
        this.rol = rol;
        this.fakulteId = fakulteId;
        this.bolumId = bolumId;
    }

    public void setSifreHash(String sifreHash) {
        this.sifreHash = sifreHash;
    }
}