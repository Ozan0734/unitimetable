package com.unitimetable.demo.yerlesim;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.blok.DersBlogu;
import com.unitimetable.demo.blok.DersBloguRepository;
import com.unitimetable.demo.ders.Ders;
import com.unitimetable.demo.ders.DersRepository;
import com.unitimetable.demo.sube.Sube;
import com.unitimetable.demo.sube.SubeRepository;

@Service
public class DerslikKullanimService {

    public record Kullanim(Long blokId, String dersKodu, String dersAdi, int subeNo, int gun,
            int baslangicSlot, int bitisSlot) {
    }

    private final YerlesimDerslikRepository yerlesimDerslikRepository;
    private final YerlesimRepository yerlesimRepository;
    private final DersBloguRepository blokRepository;
    private final SubeRepository subeRepository;
    private final DersRepository dersRepository;

    public DerslikKullanimService(YerlesimDerslikRepository yerlesimDerslikRepository,
            YerlesimRepository yerlesimRepository, DersBloguRepository blokRepository,
            SubeRepository subeRepository, DersRepository dersRepository) {
        this.yerlesimDerslikRepository = yerlesimDerslikRepository;
        this.yerlesimRepository = yerlesimRepository;
        this.blokRepository = blokRepository;
        this.subeRepository = subeRepository;
        this.dersRepository = dersRepository;
    }

    @Transactional(readOnly = true)
    public List<Kullanim> kullanimlar(Long derslikId) {
        List<YerlesimDerslik> eslesmeler = yerlesimDerslikRepository.findByDerslikId(derslikId);
        if (eslesmeler.isEmpty()) {
            return List.of();
        }
        List<Yerlesim> yerlesimler = yerlesimRepository.findAllById(
                eslesmeler.stream().map(YerlesimDerslik::getYerlesimId).collect(Collectors.toSet()));
        Map<Long, DersBlogu> bloklar = blokRepository.findAllById(
                yerlesimler.stream().map(Yerlesim::getBlokId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(DersBlogu::getId, Function.identity()));
        Map<Long, Sube> subeler = subeRepository.findAllById(
                bloklar.values().stream().map(DersBlogu::getSubeId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Sube::getId, Function.identity()));
        Map<Long, Ders> dersler = dersRepository.findAllById(
                subeler.values().stream().map(Sube::getDersId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Ders::getId, Function.identity()));

        return yerlesimler.stream()
                .map(y -> {
                    DersBlogu blok = bloklar.get(y.getBlokId());
                    Sube sube = subeler.get(blok.getSubeId());
                    Ders ders = dersler.get(sube.getDersId());
                    return new Kullanim(blok.getId(), ders.getKod(), ders.getAd(), sube.getSubeNo(),
                            y.getGun(), y.getBaslangicSlot(), y.getBaslangicSlot() + blok.getSure() - 1);
                })
                .sorted(Comparator.comparingInt(Kullanim::gun).thenComparingInt(Kullanim::baslangicSlot))
                .toList();
    }
}