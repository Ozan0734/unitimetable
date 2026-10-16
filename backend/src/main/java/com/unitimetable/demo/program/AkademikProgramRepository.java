package com.unitimetable.demo.program;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AkademikProgramRepository extends JpaRepository<AkademikProgram, Long> {

    List<AkademikProgram> findByBolumId(Long bolumId);
}