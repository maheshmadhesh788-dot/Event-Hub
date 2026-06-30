package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;

    public SecurityConfig(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.disable()) // Defer to WebConfig CORS configuration if needed
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public Static Resources
                .requestMatchers("/", "/favicon.ico", "/error", "/index.html", "/login.html", "/events.html", "/dashboard.html", "/department.html", "/admin.html", "/superadmin.html", "/register.html", "/history.html", "/achievements.html").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                
                // Public Authentication and Initial Operations
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/notifications").permitAll()
                .requestMatchers("/api/files/upload").permitAll()
                
                // Public read-only queries for events and departments
                .requestMatchers(HttpMethod.GET, "/api/departments").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/student/events/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/student/check/**").permitAll()
                
                
                // Secure Super Admin endpoints
                .requestMatchers("/api/superadmin/**").hasRole("SUPER_ADMIN")
                
                // Secure Admin endpoints
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                
                // Secure Department endpoints
                .requestMatchers("/api/departments/**").hasRole("DEPARTMENT")
                
                // Secure Student endpoints
                .requestMatchers("/api/student/**").hasRole("STUDENT")
                
                // Secure Achievement endpoints
                .requestMatchers("/api/achievements/**").hasAnyRole("TUTOR", "HOD", "ADMIN", "SUPER_ADMIN", "DEPARTMENT")
                
                // Secure Event Participation endpoints
                .requestMatchers("/api/participation/**").hasAnyRole("TUTOR", "HOD", "ADMIN", "SUPER_ADMIN", "DEPARTMENT", "STUDENT")
                
                // Catch-all
                .anyRequest().authenticated()
            );

        // Add our JWT security filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
