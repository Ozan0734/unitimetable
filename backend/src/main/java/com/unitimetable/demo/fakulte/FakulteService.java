package com.unitimetable.demo.fakulte;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FakulteService {

    private final FakulteRepository repository;

    public FakulteService(FakulteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<FakulteDto.Yanit> listele() {
        return repository.findAll().stream()
                .map(f -> new FakulteDto.Yanit(f.getId(), f.getKurumId(), f.getAd()))
                .toList();
    }

    @Transactional
    public FakulteDto.Yanit olustur(FakulteDto.Istek istek) {
        Fakulte kaydedilen = repository.save(new Fakulte(istek.kurumId(), istek.ad().trim()));
        return new FakulteDto.Yanit(kaydedilen.getId(), kaydedilen.getKurumId(), kaydedilen.getAd());
    }
}