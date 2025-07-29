package com.hande.chemical_database.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize annotations
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ============================================
                        // PUBLIC ENDPOINTS
                        // ============================================
                        .requestMatchers("/", "/home", "/login", "/register").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // ============================================
                        // AUTHENTICATION ENDPOINTS
                        // ============================================
                        .requestMatchers("/api/register", "/api/login").permitAll()

                        // ============================================
                        // READ ACCESS - ALL AUTHENTICATED USERS
                        // ============================================
                        .requestMatchers("GET", "/api/chemicals/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("GET", "/api/studies/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("GET", "/api/profile").hasAnyRole("USER", "ADMIN")

                        // ============================================
                        // CREATE/UPDATE ACCESS - ALL AUTHENTICATED USERS
                        // ============================================
                        .requestMatchers("POST", "/api/chemicals/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("PUT", "/api/chemicals/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("POST", "/api/studies/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("PUT", "/api/studies/**").hasAnyRole("USER", "ADMIN")

                        // ============================================
                        // DELETE ACCESS - ADMIN ONLY
                        // ============================================
                        .requestMatchers("DELETE", "/api/chemicals/**").hasRole("ADMIN")
                        .requestMatchers("DELETE", "/api/studies/**").hasRole("ADMIN")

                        // ============================================
                        // ADMIN ONLY ENDPOINTS
                        // ============================================
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")

                        // ============================================
                        // CSV IMPORT/EXPORT - ALL AUTHENTICATED USERS
                        // ============================================
                        .requestMatchers("/api/*/import-csv", "/api/*/export-csv").hasAnyRole("USER", "ADMIN")

                        // ============================================
                        // ALL OTHER ENDPOINTS REQUIRE AUTH
                        // ============================================
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}