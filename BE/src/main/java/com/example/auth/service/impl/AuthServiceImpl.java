package com.example.auth.service.impl;

import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.dto.ChangePasswordResponse;
import com.example.auth.entity.User;
import com.example.auth.exception.AppException;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtTokenProvider;
import com.example.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public com.example.auth.dto.LoginResponse login(com.example.auth.dto.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmailIgnoreCase(request.getUsername()))
                .orElseThrow(() -> new AppException("Tên đăng nhập hoặc mật khẩu không chính xác"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }
        if (!user.isActive()) throw new AppException("Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.");

        String token = jwtTokenProvider.generateToken(user);
        return com.example.auth.dto.LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .tokenVersion(user.getTokenVersion())
                .role(user.getRole())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    @Override
    @Transactional
    public ChangePasswordResponse changePassword(String username, ChangePasswordRequest request) {
        // 1. Tìm thông tin người dùng
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Người dùng không tồn tại"));

        // 2. Xác thực mật khẩu hiện tại
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Đổi mật khẩu thất bại: Mật khẩu hiện tại không đúng cho user [{}]", username);
            throw new AppException("Mật khẩu hiện tại không chính xác");
        }

        // 3. Kiểm tra xác nhận mật khẩu mới
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException("Mật khẩu xác nhận không khớp với mật khẩu mới");
        }

        // 4. Kiểm tra mật khẩu mới không được trùng với mật khẩu hiện tại
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AppException("Mật khẩu mới không được trùng với mật khẩu hiện tại");
        }

        // 5. Cập nhật mật khẩu đã được mã hóa BCrypt
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // 6. TĂNG TOKEN_VERSION ĐỂ THU HỒI TẤT CẢ CÁC PHIÊN ĐĂNG NHẬP / TOKEN JWT CŨ
        long oldVersion = user.getTokenVersion();
        long newVersion = oldVersion + 1;
        user.setTokenVersion(newVersion);

        User savedUser = userRepository.save(user);

        log.info("Đổi mật khẩu thành công cho user [{}]. token_version được nâng từ {} lên {}",
                username, oldVersion, newVersion);

        // 7. Tạo JWT mới với token_version mới cho phiên hiện tại của người dùng
        String newAccessToken = jwtTokenProvider.generateToken(savedUser);

        return ChangePasswordResponse.builder()
                .message("Đổi mật khẩu thành công. Tất cả các phiên đăng nhập khác đã bị vô hiệu hóa.")
                .newTokenVersion(newVersion)
                .newAccessToken(newAccessToken)
                .build();
    }

    @Override
    @Transactional
    public void revokeAllSessions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Người dùng không tồn tại"));

        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        log.info("Đã thu hồi tất cả phiên đăng nhập của user [{}]. token_version mới: {}",
                username, user.getTokenVersion());
    }
}
