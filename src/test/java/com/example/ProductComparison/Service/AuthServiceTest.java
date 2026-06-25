package com.example.ProductComparison.Service;

import com.example.ProductComparison.DTO.AuthLoginRequestDto;
import com.example.ProductComparison.DTO.AuthAdminRegisterRequestDto;
import com.example.ProductComparison.DTO.AuthRegisterRequestDto;
import com.example.ProductComparison.DTO.AuthResponseDto;
import com.example.ProductComparison.Entity.AppUser;
import com.example.ProductComparison.Entity.Role;
import com.example.ProductComparison.Exception.DuplicateUsernameException;
import com.example.ProductComparison.Repository.UserRepo;
import com.example.ProductComparison.Security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @MockitoBean
    private UserRepo userRepo;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtService jwtService;

    private AppUser testUser;
    private AuthRegisterRequestDto registerRequest;
    private AuthLoginRequestDto loginRequest;

    @BeforeEach
    void setUp() {
        testUser = AppUser.builder()
                .id(1L)
                .username("testuser")
                .passwordHash("hashedPassword")
                .role(Role.USER)
                .build();

        registerRequest = new AuthRegisterRequestDto();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");

        loginRequest = new AuthLoginRequestDto();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegister_WithValidCredentials_Success() {
        when(userRepo.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepo.save(any(AppUser.class))).thenReturn(testUser);
        when(jwtService.generateToken("newuser", "USER")).thenReturn("testToken");

        AuthResponseDto response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("newuser", response.getUsername());
        assertEquals("USER", response.getRole());
        verify(userRepo, times(1)).save(any(AppUser.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegister_WithExistingUsername_ThrowsException() {
        when(userRepo.existsByUsernameIgnoreCase("newuser")).thenReturn(true);


        assertThrows(DuplicateUsernameException.class,
                () -> authService.register(registerRequest));

        verify(userRepo, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("Should trim whitespace from username during registration")
    void testRegister_WithWhitespaceUsername_TrimmedSuccessfully() {
        registerRequest.setUsername("  newuser  ");

        when(userRepo.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepo.save(any(AppUser.class))).thenReturn(testUser);
        when(jwtService.generateToken("newuser", "USER")).thenReturn("testToken");

        AuthResponseDto response = authService.register(registerRequest);

        assertNotNull(response);
        verify(userRepo, times(1)).save(argThat(user -> user.getUsername().equals("newuser")));
    }

    @Test
    @DisplayName("Should login user with correct credentials")
    void testLogin_WithCorrectCredentials_Success() {
        when(userRepo.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken("testuser", "USER")).thenReturn("testToken");

        AuthResponseDto response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("testuser", response.getUsername());
        assertEquals("USER", response.getRole());
    }

    @Test
    @DisplayName("Should throw exception for non-existent user login")
    void testLogin_WithNonExistentUser_ThrowsException() {
        when(userRepo.findByUsernameIgnoreCase("nonexistent")).thenReturn(Optional.empty());

        loginRequest.setUsername("nonexistent");

        assertThrows(ResponseStatusException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Should throw exception for incorrect password")
    void testLogin_WithIncorrectPassword_ThrowsException() {
        when(userRepo.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        loginRequest.setPassword("wrongpassword");

        assertThrows(ResponseStatusException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Should trim username during login")
    void testLogin_WithWhitespaceUsername_TrimmedSuccessfully() {
        loginRequest.setUsername("  testuser  ");

        when(userRepo.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken("testuser", "USER")).thenReturn("testToken");

        AuthResponseDto response = authService.login(loginRequest);

        assertNotNull(response);
    }

    @Test
    @DisplayName("Should register admin with valid admin key")
    void testRegisterAdmin_WithValidAdminKey_Success() {
        AuthAdminRegisterRequestDto adminRequest = new AuthAdminRegisterRequestDto();
        adminRequest.setUsername("admin");
        adminRequest.setPassword("password123");
        adminRequest.setAdminKey("admin-key-0910");

        AppUser adminUser = AppUser.builder()
                .id(2L)
                .username("admin")
                .passwordHash("encodedPassword")
                .role(Role.ADMIN)
                .build();

        when(userRepo.existsByUsernameIgnoreCase("admin")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepo.save(any(AppUser.class))).thenReturn(adminUser);
        when(jwtService.generateToken("admin", "ADMIN")).thenReturn("adminToken");

        AuthResponseDto response = authService.registerAdmin(adminRequest);

        assertNotNull(response);
        assertEquals("ADMIN", response.getRole());
        verify(userRepo, times(1)).save(argThat(user -> user.getRole() == Role.ADMIN));
    }

    @Test
    @DisplayName("Should throw exception for invalid admin key")
    void testRegisterAdmin_WithInvalidAdminKey_ThrowsException() {
        AuthAdminRegisterRequestDto adminRequest = new AuthAdminRegisterRequestDto();
        adminRequest.setUsername("admin");
        adminRequest.setPassword("password123");
        adminRequest.setAdminKey("wrong-key");

        assertThrows(ResponseStatusException.class,
                () -> authService.registerAdmin(adminRequest));

        verify(userRepo, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("Should throw exception when admin registration is disabled")
    void testRegisterAdmin_WhenDisabled_ThrowsException() {
        AuthAdminRegisterRequestDto adminRequest = new AuthAdminRegisterRequestDto();
        adminRequest.setUsername("admin");
        adminRequest.setPassword("password123");
        adminRequest.setAdminKey("");

        assertThrows(ResponseStatusException.class,
                () -> authService.registerAdmin(adminRequest));
    }
}
