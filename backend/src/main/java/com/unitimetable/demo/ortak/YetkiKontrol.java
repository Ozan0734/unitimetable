package com.unitimetable.demo.ortak;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.unitimetable.demo.bolum.Bolum;
import com.unitimetable.demo.bolum.BolumRepository;
import com.unitimetable.demo.kullanici.KullaniciDto;

@Component
public class YetkiKontrol {

    private static final String YASAK_MESAJI = "Bu bölümün verilerine erişim yetkiniz yok";

    private final BolumRepository bolumRepository;

    public YetkiKontrol(BolumRepository bolumRepository) {
        this.bolumRepository = bolumRepository;
    }

    public KullaniciDto.Yanit kullanici() {
        Authentication kimlik = SecurityContextHolder.getContext().getAuthentication();
        if (kimlik == null || !(kimlik.getPrincipal() instanceof KullaniciDto.Yanit k)) {
            throw new YetkisizException("Bu işlem için giriş yapmanız gerekiyor");
        }
        return k;
    }

    /**
     * Giriş yapan kullanıcının verilen bölüme erişimi yoksa hata fırlatır.
     */
    public void bolumeErisim(Long bolumId) {
        KullaniciDto.Yanit k = kullanici();
        switch (k.rol()) {
            case SUPERADMIN -> {
            }
            case ASISTAN -> {
                if (!bolumId.equals(k.bolumId())) {
                    throw new YasakException(YASAK_MESAJI);
                }
            }
            case FAKULTE -> {
                Bolum bolum = bolumRepository.findById(bolumId)
                        .orElseThrow(() -> new KayitBulunamadiException(
                                "Bölüm bulunamadı (id: " + bolumId + ")"));
                if (!bolum.getFakulteId().equals(k.fakulteId())) {
                    throw new YasakException(YASAK_MESAJI);
                }
            }
        }
    }

    /**
     * Kullanıcının erişebildiği bölüm numaraları. Süperadmin için null döner (hepsi).
     */
    public List<Long> erisilebilirBolumler() {
        KullaniciDto.Yanit k = kullanici();
        return switch (k.rol()) {
            case SUPERADMIN -> null;
            case ASISTAN -> List.of(k.bolumId());
            case FAKULTE -> bolumRepository.findByFakulteId(k.fakulteId()).stream()
                    .map(Bolum::getId)
                    .toList();
        };
    }
}