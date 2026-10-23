package com.unitimetable.demo.yerlesim;

import java.util.List;

public class SilinemezException extends RuntimeException {

    private final transient List<DerslikKullanimService.Kullanim> bloklar;

    public SilinemezException(String mesaj, List<DerslikKullanimService.Kullanim> bloklar) {
        super(mesaj);
        this.bloklar = bloklar;
    }

    public List<DerslikKullanimService.Kullanim> getBloklar() {
        return bloklar;
    }
}