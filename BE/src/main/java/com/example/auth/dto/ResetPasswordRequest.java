package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Pattern(regexp = "(?=.*[A-Za-z])(?=.*\\d).{8,64}", message = "Mật khẩu phải dài 8–64 ký tự và gồm chữ, số") String newPassword,
        @NotBlank String confirmPassword) {}
