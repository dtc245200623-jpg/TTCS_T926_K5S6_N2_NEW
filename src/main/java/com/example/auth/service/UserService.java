package com.example.auth.service;

import com.example.auth.dto.UserRequest;
import com.example.auth.dto.UserResponse;

import java.util.List;
import java.util.Set;

/**
 * Interface UserService định nghĩa các thao tác nghiệp vụ liên quan đến quản lý người dùng (User).
 */
public interface UserService {
    
    /**
     * Lấy danh sách tất cả người dùng, hỗ trợ tìm kiếm, lọc theo vai trò (role), trạng thái khóa (isLocked) và phân trang.
     * 
     * @param search Từ khóa tìm kiếm (ví dụ: username, họ tên...)
     * @param role Tên vai trò dùng để lọc danh sách (ví dụ: ROLE_ADMIN, ROLE_USER...)
     * @param isLocked Lọc theo trạng thái tài khoản (true: bị khóa, false: đang hoạt động)
     * @param pageable Đối tượng chứa thông tin phân trang (trang số mấy, số lượng phần tử trên 1 trang)
     * @return Trả về một đối tượng Page chứa danh sách người dùng (dưới dạng UserResponse)
     */
    org.springframework.data.domain.Page<UserResponse> getAllUsers(String search, String role, Boolean isLocked, org.springframework.data.domain.Pageable pageable);
    
    /**
     * Tạo mới một người dùng vào hệ thống.
     * 
     * @param request Đối tượng chứa các thông tin đăng ký của người dùng mới (username, password, v.v.)
     * @return Trả về thông tin người dùng vừa được tạo thành công (dưới dạng UserResponse)
     */
    UserResponse createUser(UserRequest request);
    
    /**
     * Cập nhật danh sách các vai trò (phân quyền) cho một người dùng.
     * 
     * @param id Mã định danh (ID) của người dùng cần thay đổi quyền
     * @param roles Tập hợp các quyền mới sẽ được gán cho người dùng này
     * @return Trả về thông tin người dùng sau khi đã cập nhật quyền thành công
     */
    UserResponse updateUserRoles(Long id, Set<String> roles);
    
    /**
     * Chuyển đổi trạng thái khóa của người dùng (Đang hoạt động -> Bị khóa, hoặc Bị khóa -> Đang hoạt động).
     * 
     * @param id Mã định danh (ID) của người dùng cần khóa/mở khóa
     * @return Trả về thông tin người dùng sau khi đã thay đổi trạng thái khóa
     */
    UserResponse toggleLockUser(Long id);
    
    /**
     * Dành cho Quản trị viên (Admin) thay đổi mật khẩu của một người dùng bất kỳ.
     * 
     * @param id Mã định danh (ID) của người dùng bị đổi mật khẩu
     * @param newPassword Mật khẩu mới được thiết lập
     * @return Trả về thông tin người dùng sau khi đã đổi mật khẩu thành công
     */
    UserResponse adminChangePassword(Long id, String newPassword);
    
    /**
     * Xóa hoàn toàn một người dùng khỏi hệ thống.
     * 
     * @param id Mã định danh (ID) của người dùng cần xóa
     */
    void deleteUser(Long id);
}
