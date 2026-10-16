package com.unitimetable.demo.derslik;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DerslikRepository extends JpaRepository<Derslik, Long> {

    List<Derslik> findByKurumIdOrderByAd(Long kurumId);
}