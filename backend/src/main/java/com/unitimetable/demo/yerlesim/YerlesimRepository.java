package com.unitimetable.demo.yerlesim;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.unitimetable.demo.blok.DersBlogu;

public interface YerlesimRepository extends JpaRepository<Yerlesim, Long> {

    Optional<Yerlesim> findByBlokId(Long blokId);

    @Query("select y from Yerlesim y, DersBlogu b, Sube s, Ders d "
            + "where y.blokId = b.id and b.subeId = s.id and s.dersId = d.id and d.projeId = :projeId "
            + "order by y.gun, y.baslangicSlot")
    List<Yerlesim> findByProjeId(@Param("projeId") Long projeId);

    @Query("select y from Yerlesim y, DersBlogu b where y.blokId = b.id and b.subeId = :subeId "
            + "order by y.gun, y.baslangicSlot")
    List<Yerlesim> findBySubeId(@Param("subeId") Long subeId);

    @Query("select b from DersBlogu b, Sube s, Ders d "
            + "where b.subeId = s.id and s.dersId = d.id and d.projeId = :projeId "
            + "and not exists (select y.id from Yerlesim y where y.blokId = b.id) "
            + "order by d.kod, s.subeNo, b.siraNo")
    List<DersBlogu> yerlesmeyenBloklar(@Param("projeId") Long projeId);
}