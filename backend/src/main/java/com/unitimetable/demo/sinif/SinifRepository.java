package com.unitimetable.demo.sinif;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SinifRepository extends JpaRepository<Sinif, Long> {

    List<Sinif> findByProgramIdOrderByDuzey(Long programId);
}