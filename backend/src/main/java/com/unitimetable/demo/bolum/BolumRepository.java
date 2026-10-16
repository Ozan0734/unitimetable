package com.unitimetable.demo.bolum;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BolumRepository extends JpaRepository<Bolum, Long> {

    List<Bolum> findByFakulteId(Long fakulteId);
}