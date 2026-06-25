package com.example.ProductComparison.Security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()

                        // public
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/compare/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**").permitAll()

                        //User only
                        .requestMatchers(HttpMethod.POST, "/products/{productId}/reviews").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/products/{productId}/reviews").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/products/{productId}/reviews").hasRole("USER")
                        // admin only
                        .requestMatchers(HttpMethod.POST, "/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
        .authenticationEntryPoint((req, res, authEx) -> {
            // Fires when request is UNAUTHENTICATED (no token / invalid token)
            System.out.println("AUTHENTICATION ENTRY POINT - Unauthenticated request");
            System.out.println("   Exception: " + authEx.getMessage());
            res.setContentType("application/json");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\": \"Unauthorized: Authentication required\"}");
        })
        .accessDeniedHandler((req, res, accessEx) -> {
            // Fires when request is AUTHENTICATED but lacks required role → 403
            System.out.println(" ACCESS DENIED HANDLER - Authenticated but insufficient permissions");
            System.out.println("   Exception: " + accessEx.getMessage());
            System.out.println("   Principal: " + (req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "null"));
            res.setContentType("application/json");
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().write("{\"error\": \"Forbidden: Insufficient permissions\"}");
        })
)

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

