package com.example.auth.service.impl;

import com.example.auth.dto.UserRequest;
import com.example.auth.dto.UserResponse;
import com.example.auth.entity.User;
import com.example.auth.exception.AppException;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.example.auth.service.EmailService emailService;

    @Override
    public org.springframework.data.domain.Page<UserResponse> getAllUsers(String search, String role, Boolean isLocked, org.springframework.data.domain.Pageable pageable) {
        return userRepository.searchUsers(search, role, isLocked, pageable).map(this::mapToResponse);
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException("Email đã tồn tại");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException("Username đã tồn tại");
        }
        
        // Tạo mật khẩu ngẫu nhiên
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 8);
        
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .fullName(request.getFullName())
                .department(request.getDepartment())
                .password(passwordEncoder.encode(tempPassword))
                .roles(request.getRoles() != null && !request.getRoles().isEmpty() ? request.getRoles() : java.util.Set.of("ROLE_USER"))
                .tokenVersion(1L)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Gửi email kích hoạt với mật khẩu tạm
        emailService.sendActivationEmail(savedUser.getEmail(), tempPassword);
        
        return mapToResponse(savedUser);
    }

    @Override
    public UserResponse updateUserRoles(Long id, Set<String> roles) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng"));
        
        // Prevent admin from revoking their own admin privileges
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && user.getUsername().equals(authentication.getName())) {
            if (!roles.contains("ROLE_ADMIN") && !roles.contains("ADMIN")) {
                throw new AppException("Quản trị viên không thể tự thu hồi quyền quản trị của chính mình");
            }
        }
        
        user.setRoles(roles);
        // Force token invalidation by incrementing token version
        user.setTokenVersion(user.getTokenVersion() + 1); 
        
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public UserResponse toggleLockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng"));
                
        // Không cho phép khóa tài khoản root admin (bảo vệ an toàn)
        if (user.getRoles().contains("ROLE_ADMIN") && user.getUsername().equals("quantrihethong")) {
            throw new AppException("Không thể khóa tài khoản quản trị viên gốc");
        }
        
        // Ngăn quản trị viên tự khóa tài khoản của chính mình
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && user.getUsername().equals(authentication.getName())) {
            throw new AppException("Bạn không thể tự khóa tài khoản của chính mình");
        }
        
        user.setIsLocked(!Boolean.TRUE.equals(user.getIsLocked()));
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            user.setLockReason("Khóa bởi Quản trị viên");
            user.setLockTime(LocalDateTime.now());
            user.setTokenVersion(user.getTokenVersion() + 1); // Đăng xuất người dùng bị khóa ngay lập tức
        } else {
            user.setLockReason(null);
            user.setLockTime(null);
        }
        
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public UserResponse adminChangePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng"));
                
        // Không cho phép xóa tài khoản root admin
        if (user.getRoles().contains("ROLE_ADMIN") && user.getUsername().equals("quantrihethong")) {
            throw new AppException("Không thể xóa tài khoản quản trị viên gốc");
        }
        
        // Ngăn quản trị viên tự xóa chính mình
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && user.getUsername().equals(authentication.getName())) {
            throw new AppException("Bạn không thể tự xóa tài khoản của chính mình");
        }
        
        try {
            userRepository.delete(user);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new AppException("Không thể xóa tài khoản này vì đang có dữ liệu liên quan (công tác, hồ sơ...)");
        }
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .department(user.getDepartment())
                .roles(user.getRoles())
                .isLocked(Boolean.TRUE.equals(user.getIsLocked()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
