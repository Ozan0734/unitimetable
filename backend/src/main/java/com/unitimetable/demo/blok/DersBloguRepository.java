package com.unitimetable.demo.blok;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DersBloguRepository extends JpaRepository<DersBlogu, Long> {

    List<DersBlogu> findBySubeIdOrderBySiraNo(Long subeId);

    void deleteBySubeId(Long subeId);

    @Query("select b from DersBlogu b, Sube s where b.subeId = s.id and s.dersId = :dersId "
            + "order by s.subeNo, b.siraNo")
    List<DersBlogu> findByDersId(@Param("dersId") Long dersId);
}