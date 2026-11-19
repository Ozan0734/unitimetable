package com.unitimetable.demo.ortakders;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrtakGrupUyeRepository extends JpaRepository<OrtakGrupUye, Long> {

    List<OrtakGrupUye> findByGrupId(Long grupId);

    List<OrtakGrupUye> findByGrupIdIn(Collection<Long> grupIdler);

    Optional<OrtakGrupUye> findBySubeId(Long subeId);

    List<OrtakGrupUye> findBySubeIdIn(Collection<Long> subeIdler);
}
