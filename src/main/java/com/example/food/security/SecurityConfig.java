package com.example.food.security;

import com.example.food.config.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService; // MUHIM IMPORT
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    // DIQQAT: Bu yerda CustomUserDetailsService emas, UserDetailsService bo'lishi kerak
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // 1. Ochiq URL'lar (Public APIs)
                        .requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers("/api/v1/files/**", "/api/v1/products/**", "/api/v1/categories/**").permitAll()
                        .requestMatchers("/api/customer/banners/**", "/api/users/**").permitAll()

                        // 2. DIQQAT: Xatoliklar uchun ochiq yo'l (404 xatosi uchun juda muhim)
                        .requestMatchers("/error/**").permitAll()

                        // 3. Rollarga asoslangan ruxsatlar
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/superadmin/**").hasAuthority("ROLE_SUPERADMIN")

                        // 4. Qolgan barcha so'rovlar autentifikatsiyadan o'tishi shart
                        .anyRequest().authenticated()
                )
                // 5. Biz yozgan Custom Exception handling
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. "*" o'rniga aniq pattern ishlatamiz.
        // Bu barcha originlarga ruxsat beradi, lekin "Credentials" bilan konfliktga kirmaydi.
        configuration.setAllowedOriginPatterns(List.of("http://*", "https://*", "app://*", "capacitor://*"));

        // 2. Metodlar: PATCH va OPTIONS judayam muhim (ayniqsa Flutter/Web uchun)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 3. Hamma headerlarga ruxsat beramiz
        configuration.setAllowedHeaders(List.of("*"));

        // 4. ExposeHeaders: Ba'zida Flutter'da headerlarni o'qish uchun kerak bo'ladi
        configuration.setExposedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));

        // 5. Credential-larga ruxsat (JWT va WebSocket ulanishi uchun shart)
        configuration.setAllowCredentials(true);

        // 6. MaxAge: Brauzer har safar OPTIONS so'rovini yubormasligi uchun (Performance)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // Hamma domenga ruxsat (pattern orqali xavfsizroq)
//        configuration.setAllowedOriginPatterns(List.of("*"));
//
//        // Metodlar ro'yxatiga PATCH ni ham qo'shib qo'ying (Status yangilash uchun kerak bo'ladi)
//        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//
//        // ENG MUHIMI: Hamma headerlarga ruxsat bering
//        configuration.setAllowedHeaders(List.of("*"));
//
//        // Cookie va Authorization headerlar o'tishi uchun
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
