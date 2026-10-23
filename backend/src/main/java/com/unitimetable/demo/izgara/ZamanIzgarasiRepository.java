package com.unitimetable.demo.izgara;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ZamanIzgarasiRepository extends JpaRepository<ZamanIzgarasi, Long> {

    Optional<ZamanIzgarasi> findByKurumId(Long kurumId);
}