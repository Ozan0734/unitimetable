package com.unitimetable.demo.kullanici;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {

    List<Kullanici> findByKurumIdOrderByAdSoyad(Long kurumId);
}