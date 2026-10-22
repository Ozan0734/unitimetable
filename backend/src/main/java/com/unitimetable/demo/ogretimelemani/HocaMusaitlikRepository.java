package com.unitimetable.demo.ogretimelemani;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HocaMusaitlikRepository extends JpaRepository<HocaMusaitlik, Long> {

    List<HocaMusaitlik> findByOgretimElemaniIdOrderByGunAscSlotAsc(Long ogretimElemaniId);

    void deleteByOgretimElemaniId(Long ogretimElemaniId);
}