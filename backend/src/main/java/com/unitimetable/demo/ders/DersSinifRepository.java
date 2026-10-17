package com.unitimetable.demo.ders;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DersSinifRepository extends JpaRepository<DersSinif, Long> {

    List<DersSinif> findByDersId(Long dersId);

    List<DersSinif> findBySinifId(Long sinifId);
}