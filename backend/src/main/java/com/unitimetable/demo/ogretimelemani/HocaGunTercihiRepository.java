package com.unitimetable.demo.ogretimelemani;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HocaGunTercihiRepository extends JpaRepository<HocaGunTercihi, Long> {

    List<HocaGunTercihi> findByOgretimElemaniIdOrderByIdAsc(Long ogretimElemaniId);

    void deleteByOgretimElemaniId(Long ogretimElemaniId);
}