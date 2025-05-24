package cz.uhk.kpro2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/login", "/css/**", "/js/**", "/home", "/", "/h2-console/**").permitAll()
                // Admin can manage users
                .requestMatchers("/users/**").hasRole("ADMIN")
                // Admin can manage (POST, PUT, DELETE) coaches
                .requestMatchers(HttpMethod.POST, "/coaches/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/coaches/**").hasRole("ADMIN") // Assuming PUT for updates
                .requestMatchers(HttpMethod.DELETE, "/coaches/**").hasRole("ADMIN") // Assuming DELETE for deletions
                // Users and Admins can view coaches
                .requestMatchers(HttpMethod.GET, "/coaches/**").hasAnyRole("ADMIN", "USER")
                // Users and Admins can view teams and players
                .requestMatchers(HttpMethod.GET, "/teams/**", "/players/**").hasAnyRole("ADMIN", "USER")
                // Users and Admins can create/edit/delete their own data or managed data for teams and players
                .requestMatchers(HttpMethod.POST, "/teams/**", "/players/**").hasAnyRole("ADMIN", "USER")
                .anyRequest().authenticated()
        ).csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
        ).headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions.sameOrigin())
        ).formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
                .permitAll()
        ).logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
        ).rememberMe(remember -> remember
                .key("aVerySecretKeyForTheRememberMeCookie") // Change this key for production
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}