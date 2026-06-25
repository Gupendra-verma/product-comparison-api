package com.example.ProductComparison.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // No token → pass through (permit public endpoints)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No Bearer token found - allowing unauthenticated access for path: {}", 
                        request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        logger.debug("Processing JWT token authentication for path: {}", request.getRequestURI());

        // Validate FIRST before extracting anything
        boolean isValid = jwtService.isTokenValid(token);
        logger.debug("JWT token validation result: {}", isValid);
        
        if (!isValid) {
            logger.warn("Invalid or expired JWT token provided for path: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        try {
            String username = null;
            String role = null;
            
            try {
                username = jwtService.extractUsername(token);
                logger.debug("Successfully extracted username from JWT token");
            } catch (Exception e) {
                logger.error("Failed to extract username from JWT token", e);
            }
            
            try {
                role = jwtService.extractRole(token);
                logger.debug("Successfully extracted role from JWT token");
            } catch (Exception e) {
                logger.error("Failed to extract role from JWT token", e);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                var authorities = role == null
                        ? List.<SimpleGrantedAuthority>of()
                        : List.of(new SimpleGrantedAuthority("ROLE_" + role));

                logger.debug("Setting up authentication for user: {} with role: {}", username, role);

                var authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Authentication established for user: {} with {} authorities", 
                           username, authorities.size());
            } else {
                if (username == null) {
                    logger.warn("Cannot set authentication - username extraction failed");
                } else {
                    logger.debug("Authentication already exists in SecurityContext, skipping setup");
                }
            }

        } catch (Exception e) {
            logger.error("Error in JWT filter processing", e);
            // Don't clear context or return error here
            // Let the request proceed to authorization layer which will handle it appropriately
        }

        filterChain.doFilter(request, response);
    }
}