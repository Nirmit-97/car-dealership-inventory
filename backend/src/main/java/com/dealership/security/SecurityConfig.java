package com.dealership.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security Configuration (Spring Security 6.x style — no WebSecurityConfigurerAdapter).
 *
 * Key decisions:
 * - CSRF disabled  : REST API uses JWT, not sessions/cookies → CSRF not needed
 * - STATELESS      : no server-side session, every request carries its own JWT
 * - CORS           : configured to allow the React frontend origin
 * - Role-based     : DELETE and restock are ADMIN only
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize on controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Main security filter chain — defines which endpoints are public vs protected.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Disable CSRF — not needed for stateless JWT APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS — allows the React frontend to call this API
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Define endpoint access rules
                .authorizeHttpRequests(auth -> auth
                        // AUTH endpoints — public (no token needed)
                        .requestMatchers("/api/auth/**").permitAll()

                        // VEHICLE DELETE — Admin only
                        .requestMatchers(HttpMethod.DELETE, "/api/vehicles/**").hasRole("ADMIN")

                        // VEHICLE RESTOCK — Admin only
                        .requestMatchers(HttpMethod.POST, "/api/vehicles/*/restock").hasRole("ADMIN")

                        // ALL other /api/** endpoints — require valid JWT (any role)
                        .requestMatchers("/api/**").authenticated()

                        // Everything else — deny by default
                        .anyRequest().denyAll()
                )

                // Stateless session — no HttpSession, every request uses JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Use our custom UserDetailsService + BCrypt
                .authenticationProvider(authenticationProvider())

                // Add JWT filter BEFORE Spring's default username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    /**
     * CORS configuration — allows the React frontend to call the API.
     * Reads allowed origin from application.yml (overridden per env).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow the React dev server and deployed Vercel URL
        config.setAllowedOriginPatterns(List.of(allowedOrigins, "https://*.vercel.app"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * BCrypt password encoder — used for hashing passwords at registration.
     * Strength 10 is the default and industry-recommended level.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DaoAuthenticationProvider — wires Spring Security to use our
     * CustomUserDetailsService and BCrypt for authentication.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager bean — needed for programmatic authentication
     * (used internally by Spring Security infrastructure).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
