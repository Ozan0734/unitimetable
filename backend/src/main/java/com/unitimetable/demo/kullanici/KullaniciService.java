package com.unitimetable.demo.kullanici;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.GecersizIslemException;
import com.unitimetable.demo.ortak.KayitBulunamadiException;

@Service
public class KullaniciService {

    private record Kapsam(Long fakulteId, Long bolumId) {
    }

    private final KullaniciRepository repository;
    private final PasswordEncoder passwordEncoder;

    public KullaniciService(KullaniciRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<KullaniciDto.Yanit> listele(Long kurumId) {
        List<Kullanici> kullanicilar = (kurumId == null)
                ? repository.findAll(Sort.by("kurumId", "adSoyad"))
                : repository.findByKurumIdOrderByAdSoyad(kurumId);
        return kullanicilar.stream().map(KullaniciService::yanita).toList();
    }

    @Transactional(readOnly = true)
    public KullaniciDto.Yanit getir(Long id) {
        return yanita(bul(id));
    }

    @Transactional
    public KullaniciDto.Yanit olustur(KullaniciDto.Istek istek) {
        Kapsam kapsam = kapsam(istek.rol(), istek.fakulteId(), istek.bolumId());
        Kullanici kaydedilen = repository.saveAndFlush(new Kullanici(
                istek.kurumId(), kapsam.fakulteId(), kapsam.bolumId(),
                istek.adSoyad().trim(),
                istek.eposta().trim().toLowerCase(Locale.ROOT),
                passwordEncoder.encode(istek.sifre()),
                istek.rol()));
        return yanita(kaydedilen);
    }

    @Transactional
    public KullaniciDto.Yanit guncelle(Long id, KullaniciDto.Guncelleme istek) {
        Kullanici kullanici = bul(id);
        Kapsam kapsam = kapsam(istek.rol(), istek.fakulteId(), istek.bolumId());
        kullanici.guncelle(istek.adSoyad().trim(), istek.rol(), kapsam.fakulteId(), kapsam.bolumId());
        if (istek.sifre() != null) {
            kullanici.setSifreHash(passwordEncoder.encode(istek.sifre()));
        }
        repository.flush();
        return yanita(kullanici);
    }

    @Transactional
    public void sil(Long id) {
        repository.delete(bul(id));
        repository.flush();
    }

    private Kullanici bul(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new KayitBulunamadiException("Kullanıcı bulunamadı (id: " + id + ")"));
    }

    private static Kapsam kapsam(KullaniciRolu rol, Long fakulteId, Long bolumId) {
        return switch (rol) {
            case ASISTAN -> {
                if (bolumId == null) {
                    throw new GecersizIslemException("Bölüm sorumlusu asistan için bölüm seçilmelidir");
                }
                yield new Kapsam(null, bolumId);
            }
            case FAKULTE -> {
                if (fakulteId == null) {
                    throw new GecersizIslemException("Fakülte sorumlusu için fakülte seçilmelidir");
                }
                yield new Kapsam(fakulteId, null);
            }
            case SUPERADMIN -> new Kapsam(null, null);
        };
    }

    private static KullaniciDto.Yanit yanita(Kullanici k) {
        return new KullaniciDto.Yanit(k.getId(), k.getKurumId(), k.getFakulteId(), k.getBolumId(),
                k.getAdSoyad(), k.getEposta(), k.getRol());
    }
}