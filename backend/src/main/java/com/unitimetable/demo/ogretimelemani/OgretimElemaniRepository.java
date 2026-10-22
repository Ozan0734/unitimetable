package com.unitimetable.demo.ogretimelemani;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OgretimElemaniRepository extends JpaRepository<OgretimElemani, Long> {

    List<OgretimElemani> findByBolumIdOrderByAdSoyad(Long bolumId);

    List<OgretimElemani> findByBolumIdIn(Collection<Long> bolumIdler, Sort sort);
}