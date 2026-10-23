package com.unitimetable.demo.yerlesim;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface YerlesimDerslikRepository extends JpaRepository<YerlesimDerslik, Long> {

    List<YerlesimDerslik> findByYerlesimIdIn(Collection<Long> yerlesimIdler);

    List<YerlesimDerslik> findByDerslikId(Long derslikId);

    void deleteByYerlesimId(Long yerlesimId);
}