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
                // User management - Admin only for all operations
                .requestMatchers("/users/**").hasRole("ADMIN")
                // Coach viewing - Admin and User
                .requestMatchers(HttpMethod.GET, "/coaches", "/coaches/", "/coaches/{id}").hasAnyRole("ADMIN", "USER")
                // Coach management (create, edit, delete) - Admin only
                .requestMatchers(HttpMethod.GET, "/coaches/new", "/coaches/{id}/edit").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/coaches/save", "/coaches/{id}/delete").hasRole("ADMIN")
                // Team and Player viewing - Admin and User
                .requestMatchers(HttpMethod.GET, "/teams", "/teams/", "/teams/{id}", "/players", "/players/", "/players/{id}", "/players/list", "/players/team/{teamId}", "/players/overview").hasAnyRole("ADMIN", "USER") // Added /players/list to match controller
                // Team and Player management (create, edit, delete) - Admin and User
                .requestMatchers(HttpMethod.GET, "/teams/new", "/teams/{id}/edit", "/players/new", "/players/new_general", "/players/{id}/edit").hasAnyRole("ADMIN", "USER")
                .requestMatchers(HttpMethod.POST, "/teams/save", "/teams/{id}/delete", "/players/save", "/players/{id}/delete").hasAnyRole("ADMIN", "USER")
                // Game viewing (list, details) - Admin and User
                .requestMatchers(HttpMethod.GET, "/games", "/games/", "/games/{id}").hasAnyRole("ADMIN", "USER")
                // Game management (create, edit, delete, update score) - Admin and User
                .requestMatchers(HttpMethod.GET, "/games/new", "/games/{id}/edit").hasAnyRole("ADMIN", "USER")
                .requestMatchers(HttpMethod.POST, "/games/save", "/games/{id}/delete", "/games/{id}/updateScore").hasAnyRole("ADMIN", "USER")
                // Team Standings viewing - Admin and User
                .requestMatchers(HttpMethod.GET, "/teams/standings").hasAnyRole("ADMIN", "USER")
                .anyRequest().authenticated()
        )
        .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**") // Keep H2 console CSRF disabled for development
        )
        .headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Necessary for H2 console
        ).formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
                .permitAll()
        ).logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST")) // Ensure POST for logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
        ).rememberMe(remember -> remember
                .key("yourVerySecretKeyShouldBeStrongAndLong") // Use a strong, unique key
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}