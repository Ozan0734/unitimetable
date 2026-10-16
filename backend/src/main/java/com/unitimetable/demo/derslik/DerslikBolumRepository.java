package com.unitimetable.demo.derslik;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DerslikBolumRepository extends JpaRepository<DerslikBolum, Long> {

    List<DerslikBolum> findByDerslikId(Long derslikId);

    List<DerslikBolum> findByBolumId(Long bolumId);
}