package com.unitimetable.demo.derslik;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LabGunDagilimiRepository extends JpaRepository<LabGunDagilimi, Long> {

    List<LabGunDagilimi> findByDerslikIdOrderByGun(Long derslikId);

    List<LabGunDagilimi> findByBolumId(Long bolumId);
}