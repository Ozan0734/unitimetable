package com.unitimetable.demo.program;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.KayitBulunamadiException;

@Service
public class AkademikProgramService {

    private final AkademikProgramRepository repository;

    public AkademikProgramService(AkademikProgramRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AkademikProgramDto.Yanit> listele(Long bolumId) {
        List<AkademikProgram> programlar = (bolumId == null)
                ? repository.findAll()
                : repository.findByBolumId(bolumId);
        return programlar.stream().map(AkademikProgramService::yanita).toList();
    }

    @Transactional(readOnly = true)
    public AkademikProgramDto.Yanit getir(Long id) {
        return yanita(bul(id));
    }

    @Transactional
    public AkademikProgramDto.Yanit olustur(AkademikProgramDto.Istek istek) {
        AkademikProgram kaydedilen = repository.save(new AkademikProgram(
                istek.bolumId(), istek.ad().trim(), istek.seviye(), istek.dil()));
        return yanita(kaydedilen);
    }

    @Transactional
    public AkademikProgramDto.Yanit guncelle(Long id, AkademikProgramDto.Guncelleme istek) {
        AkademikProgram program = bul(id);
        program.setAd(istek.ad().trim());
        program.setSeviye(istek.seviye());
        program.setDil(istek.dil());
        return yanita(program);
    }

    @Transactional
    public void sil(Long id) {
        AkademikProgram program = bul(id);
        repository.delete(program);
        repository.flush();
    }

    private AkademikProgram bul(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Program bulunamadı (id: " + id + ")"));
    }

    private static AkademikProgramDto.Yanit yanita(AkademikProgram p) {
        return new AkademikProgramDto.Yanit(p.getId(), p.getBolumId(), p.getAd(), p.getSeviye(), p.getDil());
    }
}