package com.unitimetable.demo.kullanici;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitimetable.demo.ortak.YetkisizException;

@Service
public class GirisService {

    private static final String HATALI_GIRIS = "E-posta veya şifre hatalı";

    private final KullaniciRepository repository;
    private final PasswordEncoder passwordEncoder;

    public GirisService(KullaniciRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public KullaniciDto.Yanit giris(GirisDto.Istek istek) {
        Kullanici kullanici = repository.findByEposta(istek.eposta().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new YetkisizException(HATALI_GIRIS));
        if (!passwordEncoder.matches(istek.sifre(), kullanici.getSifreHash())) {
            throw new YetkisizException(HATALI_GIRIS);
        }
        return new KullaniciDto.Yanit(kullanici.getId(), kullanici.getKurumId(), kullanici.getFakulteId(),
                kullanici.getBolumId(), kullanici.getAdSoyad(), kullanici.getEposta(), kullanici.getRol());
    }
}