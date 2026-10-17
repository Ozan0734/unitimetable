package com.unitimetable.demo.ders;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DersRepository extends JpaRepository<Ders, Long> {

    List<Ders> findByProjeIdOrderByKod(Long projeId);
}