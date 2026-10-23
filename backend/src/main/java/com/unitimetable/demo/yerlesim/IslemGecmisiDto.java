package com.unitimetable.demo.yerlesim;

import java.time.LocalDateTime;

public final class IslemGecmisiDto {

    private IslemGecmisiDto() {
    }

    public record Yanit(Long id, Long blokId, IslemTuru tur, BlokDurumu onceki, BlokDurumu sonraki,
            boolean geriAlindi, LocalDateTime tarih) {
    }
}