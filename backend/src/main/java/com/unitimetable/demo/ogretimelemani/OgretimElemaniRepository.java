package com.unitimetable.demo.ogretimelemani;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OgretimElemaniRepository extends JpaRepository<OgretimElemani, Long> {

    List<OgretimElemani> findByBolumIdOrderByAdSoyad(Long bolumId);
}