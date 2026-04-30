package com.ot.config;

import com.ot.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .cors(withDefaults -> {})
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/**").permitAll()
                // ✅ ADD THIS LINE
                .requestMatchers(HttpMethod.POST, "/api/super-admin/register").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/ipd/ot-request").permitAll() // 👈 ONLY THIS OPEN
                // 🔥 ADD THIS
                .requestMatchers(HttpMethod.PUT, "/api/ot/mark-accepted-by-ipd").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            )
            
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
 
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
 
        config.setAllowedOrigins(List.of(
        		"http://localhost:5173",
        		"http://localhost:5174",
                "http://localhost",
                "http://localhost:*",
                "http://127.0.0.1",
                "http://127.0.0.1:*",
                "http://147.93.28.8:4003",
                "http://147.93.28.8:4004",
                "http://147.93.28.8:4005",
                "http://147.93.28.8:4006",
                "http://147.93.28.8:4007",
                "http://147.93.28.8:4010",
                "http://127.0.0.1:5500"
        ));
 
        config.setAllowedMethods(List.of("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
 
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    } 
}