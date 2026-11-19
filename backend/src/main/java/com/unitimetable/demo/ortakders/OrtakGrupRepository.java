package com.unitimetable.demo.ortakders;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrtakGrupRepository extends JpaRepository<OrtakGrup, Long> {

    List<OrtakGrup> findByKurumId(Long kurumId);
}
