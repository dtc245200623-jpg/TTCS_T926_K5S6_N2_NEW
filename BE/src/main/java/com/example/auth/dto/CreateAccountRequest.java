package com.example.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank @Size(min = 3, max = 60) String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 10, max = 100) String password,
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 30) String phone,
        @NotBlank String role) {}
