package com.unitimetable.demo.proje;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramProjesiRepository extends JpaRepository<ProgramProjesi, Long> {

    List<ProgramProjesi> findByBolumIdOrderByOlusturmaTarihiDesc(Long bolumId);

    List<ProgramProjesi> findByBolumIdIn(Collection<Long> bolumIdler, Sort sort);
}