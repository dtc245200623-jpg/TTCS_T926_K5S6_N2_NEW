
package com.example.auth.service;

import com.example.auth.dto.ResetPasswordRequest;
import com.example.auth.entity.PasswordResetToken;
import com.example.auth.entity.User;
import com.example.auth.exception.AppException;
import com.example.auth.repository.PasswordResetTokenRepository;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final JavaMailSender mailSender;
    @Value("${app.password-reset-url:http://localhost:8080/forgot-password.html}")
    private String resetPageUrl;
    @Value("${spring.mail.username:no-reply@example.com}")
    private String fromAddress;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void requestReset(String email) {
        User user = users.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (user == null || !user.isActive()) return;
        tokens.deleteByUser_Id(user.getId());
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiry = Instant.now().plus(Duration.ofMinutes(30));
        tokens.save(new PasswordResetToken(user, hash(raw), expiry));
        String link = UriComponentsBuilder.fromUriString(resetPageUrl).queryParam("token", raw).build().encode().toUriString();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("Đặt lại mật khẩu tài khoản tuyển dụng nội bộ");
        message.setText("Xin chào " + (user.getFullName() == null ? user.getUsername() : user.getFullName())
                + ",\n\nChúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Mở liên kết sau trong vòng 30 phút để tạo mật khẩu mới:\n\n"
                + link + "\n\nNếu bạn không yêu cầu thao tác này, hãy bỏ qua email. Liên kết chỉ dùng được một lần.");
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            tokens.deleteByUser_Id(user.getId());
            log.error("Không gửi được email đặt lại mật khẩu cho user id {}", user.getId(), ex);
        }
    }

    @Transactional
    public void confirmReset(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword()))
            throw new AppException("Mật khẩu xác nhận không khớp.");
        PasswordResetToken token = tokens.findByTokenHash(hash(request.token()))
                .orElseThrow(() -> new AppException("Mã đặt lại không hợp lệ hoặc đã hết hạn."));
        if (!token.getExpiresAt().isAfter(Instant.now())) {
            tokens.delete(token);
            throw new AppException("Mã đặt lại không hợp lệ hoặc đã hết hạn.");
        }
        User user = token.getUser();
        user.setPassword(encoder.encode(request.newPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        users.save(user);
        tokens.delete(token);
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tạo mã bảo mật.", ex);
        }
    }
}
