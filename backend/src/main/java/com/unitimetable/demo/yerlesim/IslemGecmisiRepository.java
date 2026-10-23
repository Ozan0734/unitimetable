package com.unitimetable.demo.yerlesim;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IslemGecmisiRepository extends JpaRepository<IslemGecmisi, Long> {

    List<IslemGecmisi> findByProjeIdOrderByIdDesc(Long projeId);

    Optional<IslemGecmisi> findFirstByProjeIdAndGeriAlindiFalseOrderByIdDesc(Long projeId);

    Optional<IslemGecmisi> findFirstByProjeIdAndGeriAlindiTrueOrderByIdAsc(Long projeId);

    void deleteByProjeIdAndGeriAlindiTrue(Long projeId);
}