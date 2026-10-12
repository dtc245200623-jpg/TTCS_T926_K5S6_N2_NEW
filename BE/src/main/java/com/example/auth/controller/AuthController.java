package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.ChangePasswordRequest;
import com.example.auth.dto.ChangePasswordResponse;
import com.example.auth.service.AuthService;
import com.example.auth.service.PasswordResetService;
import com.example.auth.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.auth.entity.User;
import com.example.auth.dto.ProfileUpdateRequest;
import com.example.auth.dto.CreateAccountRequest;
import com.example.auth.dto.UpdateAccountRequest;
import com.example.auth.security.AppRole;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Cho phép frontend (như Live Server) gọi API
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> currentUser(Authentication authentication,
            @RequestParam(name = "includeAvatar", defaultValue = "false") boolean includeAvatar) {
        return profileResponse(authentication, includeAvatar);
    }

    private ResponseEntity<ApiResponse<java.util.Map<String, Object>>> profileResponse(Authentication authentication, boolean includeAvatar) {
        var user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new com.example.auth.exception.AppException("Người dùng không tồn tại"));
        var profile = new java.util.LinkedHashMap<String, Object>();
        profile.put("username", user.getUsername());
        profile.put("id", user.getId());
        profile.put("fullName", user.getFullName());
        profile.put("phone", user.getPhone());
        profile.put("email", user.getEmail());
        if (includeAvatar) profile.put("avatarData", user.getAvatarData());
        profile.put("role", user.getRole());
        profile.put("active", user.isActive());
        profile.put("createdAt", user.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tài khoản thành công", profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> updateProfile(Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        userRepository.findByEmailIgnoreCase(request.email().trim()).filter(other -> !other.getId().equals(user.getId()))
                .ifPresent(other -> { throw new com.example.auth.exception.AppException("Email đã được sử dụng."); });
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim());
        user.setPhone(request.phone() == null ? null : request.phone().trim());
        validateAvatar(request.avatarData());
        user.setAvatarData(request.avatarData() == null || request.avatarData().isBlank() ? null : request.avatarData());
        userRepository.save(user);
        return profileResponse(authentication, true);
    }

    private static void validateAvatar(String avatarData) {
        if (avatarData == null || avatarData.isBlank()) return;
        var matcher = java.util.regex.Pattern.compile("^data:image/(png|jpeg);base64,([A-Za-z0-9+/]+={0,2})$").matcher(avatarData);
        if (!matcher.matches()) throw new com.example.auth.exception.AppException("Ảnh đại diện chỉ hỗ trợ PNG hoặc JPEG hợp lệ.");
        final byte[] bytes;
        try { bytes = Base64.getDecoder().decode(matcher.group(2)); }
        catch (IllegalArgumentException ex) { throw new com.example.auth.exception.AppException("Dữ liệu ảnh đại diện không hợp lệ."); }
        if (bytes.length > 1_048_576) throw new com.example.auth.exception.AppException("Ảnh đại diện không được vượt quá 1 MB.");
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new com.example.auth.exception.AppException("Tệp tải lên không phải ảnh PNG/JPEG hợp lệ.");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                String format = reader.getFormatName().toLowerCase(java.util.Locale.ROOT);
                String expected = matcher.group(1).equals("jpeg") ? "jpeg" : "png";
                if ((!format.equals(expected) && !(expected.equals("jpeg") && format.equals("jpg")))
                        || width < 1 || height < 1 || width > 2048 || height > 2048 || (long) width * height > 4_000_000L)
                    throw new com.example.auth.exception.AppException("Ảnh đại diện phải là PNG/JPEG và không vượt quá 2048 × 2048 pixel.");
            } finally { reader.dispose(); }
        } catch (com.example.auth.exception.AppException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new com.example.auth.exception.AppException("Không thể đọc ảnh đại diện.");
        }
    }

    @GetMapping("/admin/accounts")
    @PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_ACCOUNTS')")
    public ResponseEntity<ApiResponse<java.util.List<java.util.Map<String, Object>>>> accounts(
            @RequestParam(name = "q", required = false) String q) {
        String term = q == null ? "" : q.trim().toLowerCase(java.util.Locale.ROOT);
        var rows = userRepository.findAll().stream()
                .filter(user -> term.isEmpty()
                        || contains(user.getUsername(), term)
                        || contains(user.getFullName(), term)
                        || contains(user.getEmail(), term))
                .map(AuthController::accountView).toList();
        return ResponseEntity.ok(ApiResponse.success("Danh sách tài khoản", rows));
    }

    @PostMapping("/admin/accounts")
    @PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_ACCOUNTS')")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        if (!AppRole.isInternal(request.role())) throw new com.example.auth.exception.AppException("Vai trò không hợp lệ.");
        if (userRepository.existsByUsername(request.username().trim()) || userRepository.findByEmailIgnoreCase(request.email().trim()).isPresent())
            throw new com.example.auth.exception.AppException("Tên đăng nhập hoặc email đã tồn tại.");
        User user = User.builder().username(request.username().trim()).email(request.email().trim())
                .fullName(request.fullName().trim()).phone(request.phone()).role(request.role())
                .password(passwordEncoder.encode(request.password())).active(true).tokenVersion(1L).build();
        return ResponseEntity.ok(ApiResponse.success("Tạo tài khoản thành công", accountView(userRepository.save(user))));
    }

    @PutMapping("/admin/accounts/{id}")
    @PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_ACCOUNTS')")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> updateAccount(@PathVariable Long id,
            @Valid @RequestBody UpdateAccountRequest request, Authentication authentication) {
        User user = userRepository.findById(id).orElseThrow(() -> new com.example.auth.exception.AppException("Tài khoản không tồn tại."));
        if (!AppRole.isInternal(request.role())) throw new com.example.auth.exception.AppException("Vai trò không hợp lệ.");
        userRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(other -> !other.getId().equals(user.getId()))
                .ifPresent(other -> { throw new com.example.auth.exception.AppException("Email đã được sử dụng bởi tài khoản khác."); });
        if (user.getUsername().equals(authentication.getName()) && (!request.active() || !"ROLE_ADMIN".equals(request.role())))
            throw new com.example.auth.exception.AppException("Không thể vô hiệu hóa hoặc hạ quyền tài khoản quản trị đang sử dụng.");
        boolean authChanged = !user.getRole().equals(request.role()) || user.isActive() != request.active();
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim());
        user.setPhone(request.phone() == null ? null : request.phone().trim());
        user.setRole(request.role());
        user.setActive(request.active());
        if (authChanged) user.setTokenVersion(user.getTokenVersion() + 1);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tài khoản thành công", accountView(userRepository.save(user))));
    }

    private static boolean contains(String value, String term) {
        return value != null && value.toLowerCase(java.util.Locale.ROOT).contains(term);
    }

    private static java.util.Map<String, Object> accountView(User user) {
        var row = new java.util.LinkedHashMap<String, Object>();
        row.put("id", user.getId()); row.put("username", user.getUsername()); row.put("fullName", user.getFullName());
        row.put("email", user.getEmail()); row.put("phone", user.getPhone()); row.put("role", user.getRole());
        row.put("active", user.isActive()); row.put("createdAt", user.getCreatedAt());
        return row;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody com.example.auth.dto.ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.ok(ApiResponse.success(
                "Nếu email đã đăng ký, hướng dẫn đặt lại mật khẩu sẽ được gửi."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody com.example.auth.dto.ResetPasswordRequest request) {
        passwordResetService.confirmReset(request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới."));
    }

    /**
     * Endpoint đăng nhập để lấy JWT token thử nghiệm
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<com.example.auth.dto.LoginResponse>> login(
            @Valid @RequestBody com.example.auth.dto.LoginRequest request) {
        com.example.auth.dto.LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    /**
     * Endpoint đổi mật khẩu và thu hồi các phiên đăng nhập khác.
     * Yêu cầu người dùng phải gửi kèm JWT token hợp lệ trong header Authorization: Bearer <token>.
     *
     * @param authentication Đối tượng chứa thông tin người dùng đã xác thực từ Security Context
     * @param request        DTO chứa mật khẩu cũ, mật khẩu mới và xác nhận mật khẩu
     * @return ApiResponse chứa ChangePasswordResponse kèm token mới cho phiên hiện tại
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<ChangePasswordResponse>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        String currentUsername = authentication.getName();
        ChangePasswordResponse response = authService.changePassword(currentUsername, request);

        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", response));
    }

    /**
     * Endpoint chủ động thu hồi tất cả các phiên đăng nhập (Đăng xuất khỏi tất cả thiết bị).
     *
     * @param authentication Thông tin người dùng hiện tại
     * @return ApiResponse thông báo thành công
     */
    @PostMapping("/revoke-sessions")
    public ResponseEntity<ApiResponse<Void>> revokeAllSessions(Authentication authentication) {
        String currentUsername = authentication.getName();
        authService.revokeAllSessions(currentUsername);

        return ResponseEntity.ok(ApiResponse.success("Đã thu hồi tất cả các phiên đăng nhập thành công"));
    }
}
