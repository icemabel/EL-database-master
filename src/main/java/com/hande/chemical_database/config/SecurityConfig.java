package com.hande.chemical_database.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ALLOW EVERYTHING - NO SECURITY FOR NOW
                        .anyRequest().permitAll()
                )
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .build();
    }

//    @Autowired
//    private UserDetailsService userDetailsService;
//
//    @Autowired
//    private JwtFilter jwtFilter;
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(10);
//    }
//
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
//        authenticationProvider.setUserDetailsService(userDetailsService);
//        authenticationProvider.setPasswordEncoder(passwordEncoder());
//        return authenticationProvider;
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        return http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        // ============================================
//                        // PUBLIC ENDPOINTS (No authentication needed)
//                        // ============================================
//                        .requestMatchers("/", "/home", "/login", "/register").permitAll()
//                        .requestMatchers("/login-test", "/auth-test").permitAll() // Debug pages
//                        .requestMatchers("/api/login", "/api/register").permitAll() // Auth APIs
//
//                        // Static resources
//                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()
//
//                        // QR code public access (for scanning)
//                        .requestMatchers("/qr/**", "/scanner").permitAll()
//
//                        // H2 Console (development only)
//                        .requestMatchers("/h2-console/**").permitAll()
//
//                        // Debug endpoints (some public for troubleshooting)
//                        .requestMatchers("/api/debug/users").permitAll()
//
//                        // ============================================
//                        // PAGES - NOW PUBLIC (auth handled by JavaScript)
//                        // ============================================
//                        .requestMatchers("/chemicals-with-qr", "/csv-import-export").permitAll()
//
//                        // ============================================
//                        // API ENDPOINTS - SECURED BY ROLE
//                        // ============================================
//                        .requestMatchers("/api/chemicals/**").hasAnyRole("USER", "ADMIN")
//                        .requestMatchers("/api/profile").hasAnyRole("USER", "ADMIN")
//                        .requestMatchers("/api/debug/**").hasAnyRole("USER", "ADMIN")
//
//                        // ============================================
//                        // ADMIN ONLY ENDPOINTS
//                        // ============================================
//                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
//
//                        // ============================================
//                        // ALL OTHER ENDPOINTS REQUIRE AUTH
//                        // ============================================
//                        .anyRequest().authenticated()
//                )
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .headers(headers ->
//                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
//                )
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
//                .build();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
}