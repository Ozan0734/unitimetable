package com.unitimetable.demo.ortakders;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ortak_grup_uye")
public class OrtakGrupUye {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grup_id", nullable = false)
    private Long grupId;

    @Column(name = "sube_id", nullable = false)
    private Long subeId;

    @Column(nullable = false)
    private boolean onayli;

    protected OrtakGrupUye() {
    }

    public OrtakGrupUye(Long grupId, Long subeId, boolean onayli) {
        this.grupId = grupId;
        this.subeId = subeId;
        this.onayli = onayli;
    }

    public Long getId() {
        return id;
    }

    public Long getGrupId() {
        return grupId;
    }

    public Long getSubeId() {
        return subeId;
    }

    public boolean isOnayli() {
        return onayli;
    }

    public void setOnayli(boolean onayli) {
        this.onayli = onayli;
    }
}
