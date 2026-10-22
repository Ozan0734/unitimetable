package com.unitimetable.demo.blok;

public final class BlokDto {

    private BlokDto() {
    }

    public record Yanit(Long id, Long subeId, int siraNo, int sure) {
    }
}