package com.unitimetable.demo.sube;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubeRepository extends JpaRepository<Sube, Long> {

    List<Sube> findByDersIdOrderBySubeNo(Long dersId);

    List<Sube> findByOgretimElemaniIdOrderByDersIdAscSubeNoAsc(Long ogretimElemaniId);

    @Query("select s from Sube s where s.dersId in "
            + "(select d.id from Ders d where d.bolumId in :bolumler) "
            + "order by s.dersId, s.subeNo")
    List<Sube> findByDersBolumIn(@Param("bolumler") Collection<Long> bolumler);
}