package com.unitimetable.demo.kullanici;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class GirisController {

    private final GirisService service;
    private final HttpSessionSecurityContextRepository baglamDeposu = new HttpSessionSecurityContextRepository();

    public GirisController(GirisService service) {
        this.service = service;
    }

    @PostMapping("/giris")
    public KullaniciDto.Yanit giris(@Valid @RequestBody GirisDto.Istek istek,
            HttpServletRequest request, HttpServletResponse response) {
        KullaniciDto.Yanit kullanici = service.giris(istek);

        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        Authentication kimlik = UsernamePasswordAuthenticationToken.authenticated(
                kullanici, null, List.of(new SimpleGrantedAuthority("ROLE_" + kullanici.rol().name())));
        SecurityContext baglam = SecurityContextHolder.createEmptyContext();
        baglam.setAuthentication(kimlik);
        SecurityContextHolder.setContext(baglam);
        baglamDeposu.saveContext(baglam, request, response);

        return kullanici;
    }

    @GetMapping("/oturum")
    public KullaniciDto.Yanit oturum(@AuthenticationPrincipal KullaniciDto.Yanit kullanici) {
        return kullanici;
    }

    @PostMapping("/cikis")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cikis(HttpServletRequest request) {
        HttpSession oturum = request.getSession(false);
        if (oturum != null) {
            oturum.invalidate();
        }
        SecurityContextHolder.clearContext();
    }
}