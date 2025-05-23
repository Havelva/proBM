package cz.uhk.kpro2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/login", "/css/**", "/js/**", "/home", "/").permitAll() // Added /home and / to permitAll
                // Permit all access to player-related paths for now, can be restricted later
                .requestMatchers("/players/**", "/teams/**", "/coaches/**", "/users/**").permitAll() // Changed from /fuel-cells/** and added others
                .anyRequest().authenticated()
        ).csrf(csrf -> csrf
                .ignoringRequestMatchers("/players/**", "/teams/**", "/coaches/**", "/users/**") // Disable CSRF for new paths
        ).formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
                .permitAll()
        ).logout(logout -> logout
                .logoutSuccessUrl("/login?logout") // Added ?logout for a potential logout message
                .permitAll()
        );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
