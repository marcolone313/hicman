package com.osc.CorporateSite.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Permetti accesso pubblico a queste risorse
                .requestMatchers(
                    "/",
                    "/about",
                    "/services",
                    "/contact",
                    "/blog/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/uploads/**",
                    "/error"
                ).permitAll()
                // Richiedi autenticazione per tutto sotto /admin
                .requestMatchers("/admin/**").authenticated()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin", true)
                .failureUrl("/admin/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Se aggiungi API REST
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // IMPORTANTE: Cambia username e password in produzione!
        // Per generare una password bcrypt: 
        // https://bcrypt-generator.com/
        
        UserDetails admin = User.builder()
            .username("admin")
            // Password: "admin123" (CAMBIALA!)
            .password(passwordEncoder().encode("admin123"))
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(admin);
    }
}