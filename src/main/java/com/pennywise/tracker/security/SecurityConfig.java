package com.pennywise.tracker.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.ApplicationRunner;
import com.pennywise.tracker.model.AppUser;
import com.pennywise.tracker.repository.AppUserRepository;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain applicationSecurity(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/error", "/api/health").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
                .httpBasic(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutSuccessHandler((request, response, authentication) -> response.sendRedirect("/login?logout"))
                        .permitAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(AppUserRepository users) {
        return username -> users.findByUsername(username.toLowerCase(java.util.Locale.ROOT))
                .map(user -> org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                        .password(user.getPasswordHash()).roles("USER").build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    ApplicationRunner defaultAccountInitializer(Environment environment, AppUserRepository users, PasswordEncoder passwordEncoder) {
        return args -> {
            String username = environment.getProperty("APP_USERNAME", "admin");
            String password = environment.getProperty("APP_PASSWORD", "pennywise");
            String normalizedUsername = username.trim().toLowerCase(java.util.Locale.ROOT);
            if (users.findByUsername(normalizedUsername).isEmpty()) {
                users.save(new AppUser(normalizedUsername, passwordEncoder.encode(password)));
            }
        };
    }
}
