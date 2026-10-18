package com.carddemo.config;

import com.carddemo.repository.UserRepository;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * HTTP Basic against the {@code users} table (USRSEC), replacing COSGN00C sign-on for this slice.
 * {@code SEC-USR-TYPE} 'A' maps to role ADMIN and 'U' to role USER.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/cards", "/api/cards/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /** COSGN00C upper-cases both user ID and password before comparing them. */
    @Bean
    public UserDetailsService userDetailsService(UserRepository users) {
        return username -> users.findById(username.toUpperCase(Locale.ROOT))
                .map(u -> User.withUsername(u.getUsrId())
                        .password(u.getPwd())
                        .roles(Character.valueOf('A').equals(u.getUsrType()) ? "ADMIN" : "USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new UpperCasePasswordEncoder(new BCryptPasswordEncoder());
    }

    /** Upper-cases the raw password before delegating, matching COSGN00C's case-insensitive sign-on. */
    static final class UpperCasePasswordEncoder implements PasswordEncoder {

        private final PasswordEncoder delegate;

        UpperCasePasswordEncoder(PasswordEncoder delegate) {
            this.delegate = delegate;
        }

        @Override
        public String encode(CharSequence raw) {
            return delegate.encode(raw.toString().toUpperCase(Locale.ROOT));
        }

        @Override
        public boolean matches(CharSequence raw, String encoded) {
            return delegate.matches(raw.toString().toUpperCase(Locale.ROOT), encoded);
        }
    }
}
