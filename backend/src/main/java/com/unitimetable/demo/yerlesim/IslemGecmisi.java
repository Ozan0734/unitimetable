package com.unitimetable.demo.yerlesim;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "islem_gecmisi")
public class IslemGecmisi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "proje_id", nullable = false)
    private Long projeId;

    @Column(name = "blok_id", nullable = false)
    private Long blokId;

    @Column(name = "kullanici_id")
    private Long kullaniciId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IslemTuru tur;

    @Column(length = 200)
    private String onceki;

    @Column(length = 200)
    private String sonraki;

    @Column(name = "geri_alindi", nullable = false)
    private boolean geriAlindi;

    @Column(nullable = false, updatable = false)
    private LocalDateTime tarih;

    protected IslemGecmisi() {
    }

    public IslemGecmisi(Long projeId, Long blokId, Long kullaniciId, IslemTuru tur,
            String onceki, String sonraki) {
        this.projeId = projeId;
        this.blokId = blokId;
        this.kullaniciId = kullaniciId;
        this.tur = tur;
        this.onceki = onceki;
        this.sonraki = sonraki;
        this.geriAlindi = false;
        this.tarih = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getProjeId() {
        return projeId;
    }

    public Long getBlokId() {
        return blokId;
    }

    public IslemTuru getTur() {
        return tur;
    }

    public String getOnceki() {
        return onceki;
    }

    public String getSonraki() {
        return sonraki;
    }

    public boolean isGeriAlindi() {
        return geriAlindi;
    }

    public LocalDateTime getTarih() {
        return tarih;
    }

    public void setGeriAlindi(boolean geriAlindi) {
        this.geriAlindi = geriAlindi;
    }
}