package com.chococraft.service;

import com.chococraft.dto.UserRegistrationDto;
import com.chococraft.entity.User;
import com.chococraft.exception.BadRequestException;
import com.chococraft.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserRegistrationDto validDto;

    @BeforeEach
    void setUp() {
        validDto = new UserRegistrationDto();
        validDto.setFullName("Test User");
        validDto.setEmail("testuser@example.com");
        validDto.setPassword("Secret123");
        validDto.setConfirmPassword("Secret123");
        validDto.setPhone("9876543210");
    }

    @Test
    @DisplayName("Should successfully register new user with hashed password")
    void testSuccessfulRegistration() {
        User registered = userService.registerUser(validDto);
        assertNotNull(registered.getId());
        assertEquals("Test User", registered.getFullName());
        assertEquals("testuser@example.com", registered.getEmail());
        assertNotEquals("Secret123", registered.getPassword()); // Password must be hashed
        assertTrue(registered.isActive());
    }

    @Test
    @DisplayName("Should throw exception when registering duplicate email")
    void testDuplicateEmailRegistration() {
        userService.registerUser(validDto);
        assertThrows(BadRequestException.class, () -> userService.registerUser(validDto));
    }

    @Test
    @DisplayName("Should throw exception when password and confirm password mismatch")
    void testPasswordMismatch() {
        validDto.setConfirmPassword("Different123");
        assertThrows(BadRequestException.class, () -> userService.registerUser(validDto));
    }
}
