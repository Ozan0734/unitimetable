package com.unitimetable.demo.ortak;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class GuvenlikAyarlari {

    private static final String[] KURUM_YAPISI = {
            "/api/fakulteler/**",
            "/api/bolumler/**",
            "/api/programlar/**",
            "/api/siniflar/**",
            "/api/derslikler/**",
            "/api/derslik-bolum/**",
            "/api/lab-gun-dagilimi/**"
    };

    private static final String[] ACIK_ADRESLER = {
            "/api/giris",
            "/actuator/health",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filtreZinciri(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .authorizeHttpRequests(istekler -> istekler
                        .requestMatchers(ACIK_ADRESLER).permitAll()
                        .requestMatchers("/api/kullanicilar/**").hasRole("SUPERADMIN")
                        .requestMatchers(HttpMethod.GET, KURUM_YAPISI).authenticated()
                        .requestMatchers(KURUM_YAPISI).hasAnyRole("SUPERADMIN", "FAKULTE")
                        .anyRequest().authenticated())
                .exceptionHandling(hatalar -> hatalar
                        .authenticationEntryPoint((istek, cevap, hata) -> {
                            cevap.setStatus(401);
                            cevap.setContentType("application/json;charset=UTF-8");
                            cevap.getWriter().write(
                                    "{\"mesaj\":\"Bu işlem için giriş yapmanız gerekiyor\",\"alanlar\":{}}");
                        })
                        .accessDeniedHandler((istek, cevap, hata) -> {
                            cevap.setStatus(403);
                            cevap.setContentType("application/json;charset=UTF-8");
                            cevap.getWriter().write(
                                    "{\"mesaj\":\"Bu işlem için yetkiniz yok\",\"alanlar\":{}}");
                        }));
        return http.build();
    }
}