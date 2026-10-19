package com.unitimetable.demo.kullanici;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {

    List<Kullanici> findByKurumIdOrderByAdSoyad(Long kurumId);

    Optional<Kullanici> findByEposta(String eposta);
}