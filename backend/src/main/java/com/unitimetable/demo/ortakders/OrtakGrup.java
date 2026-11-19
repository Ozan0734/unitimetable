package com.unitimetable.demo.ortakders;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ortak_grup")
public class OrtakGrup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kurum_id", nullable = false)
    private Long kurumId;

    @Column(nullable = false, length = 100)
    private String donem;

    @Column(name = "olusturan_bolum_id", nullable = false)
    private Long olusturanBolumId;

    @Column(name = "olusturma_tarihi", nullable = false, updatable = false)
    private LocalDateTime olusturmaTarihi;

    protected OrtakGrup() {
    }

    public OrtakGrup(Long kurumId, String donem, Long olusturanBolumId) {
        this.kurumId = kurumId;
        this.donem = donem;
        this.olusturanBolumId = olusturanBolumId;
        this.olusturmaTarihi = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getKurumId() {
        return kurumId;
    }

    public String getDonem() {
        return donem;
    }

    public Long getOlusturanBolumId() {
        return olusturanBolumId;
    }

    public LocalDateTime getOlusturmaTarihi() {
        return olusturmaTarihi;
    }
}
