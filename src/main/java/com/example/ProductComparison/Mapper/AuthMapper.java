package com.example.ProductComparison.Mapper;

import com.example.ProductComparison.DTO.AuthResponseDto;
import com.example.ProductComparison.Entity.AppUser;
import org.springframework.stereotype.Component;

/**
 * Mapper for Auth-related entity to DTO conversions.
 * Handles conversion from AppUser entity to AuthResponseDto.
 */
@Component
public class AuthMapper {

    /**
     * Convert AppUser entity to AuthResponseDto with JWT token
     * 
     * @param user the AppUser entity
     * @param token the JWT token
     * @return AuthResponseDto
     */
    public AuthResponseDto toAuthResponseDto(AppUser user, String token) {
        if (user == null || token == null) {
            return null;
        }

        return AuthResponseDto.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole() != null ? user.getRole().name() : "USER")
                .build();
    }

    /**
     * Convert AppUser entity to AuthResponseDto (without token)
     * Useful for scenarios where you need user info but no token
     * 
     * @param user the AppUser entity
     * @return AuthResponseDto with null token
     */
    public AuthResponseDto toAuthResponseDtoWithoutToken(AppUser user) {
        if (user == null) {
            return null;
        }

        return AuthResponseDto.builder()
                .token(null)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole() != null ? user.getRole().name() : "USER")
                .build();
    }
}
