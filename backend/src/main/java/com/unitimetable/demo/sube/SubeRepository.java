package com.unitimetable.demo.sube;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubeRepository extends JpaRepository<Sube, Long> {

    List<Sube> findByDersIdOrderBySubeNo(Long dersId);

    List<Sube> findByOgretimElemaniIdOrderByDersIdAscSubeNoAsc(Long ogretimElemaniId);
}