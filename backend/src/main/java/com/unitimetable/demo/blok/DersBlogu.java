package com.unitimetable.demo.blok;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ders_blogu")
public class DersBlogu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sube_id", nullable = false)
    private Long subeId;

    @Column(name = "sira_no", nullable = false)
    private int siraNo;

    @Column(nullable = false)
    private int sure;

    protected DersBlogu() {
    }

    public DersBlogu(Long subeId, int siraNo, int sure) {
        this.subeId = subeId;
        this.siraNo = siraNo;
        this.sure = sure;
    }

    public Long getId() {
        return id;
    }

    public Long getSubeId() {
        return subeId;
    }

    public int getSiraNo() {
        return siraNo;
    }

    public int getSure() {
        return sure;
    }
}