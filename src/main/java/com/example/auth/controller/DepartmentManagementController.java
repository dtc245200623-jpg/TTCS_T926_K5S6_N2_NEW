package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.entity.Department;
import com.example.auth.entity.JobRequest;
import com.example.auth.entity.User;
import com.example.auth.repository.DepartmentRepository;
import com.example.auth.repository.JobRequestRepository;
import com.example.auth.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
public class DepartmentManagementController {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRequestRepository jobRequestRepository;

    // 1. Lấy danh sách phòng ban (Cấu trúc cây nhiều cấp)
    @GetMapping("/tree")
    public ResponseEntity<?> getDepartmentTree() {
        // Lấy tất cả phòng ban
        List<Department> allDepartments = departmentRepository.findAll();
        
        // Lọc ra các phòng ban gốc (không có parent)
        List<DepartmentDto> tree = allDepartments.stream()
                .filter(d -> d.getParent() == null)
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(ApiResponse.success("Lấy cấu trúc cây phòng ban thành công", tree));
    }

    // 2. Tạo phòng ban mới (Thiết lập cấu trúc cây)
    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestBody CreateDepartmentRequest request) {
        Department dept = new Department();
        dept.setName(request.getName());
        dept.setDescription(request.getDescription());
        dept.setStatus("ACTIVE");
        
        if (request.getParentId() != null) {
            Optional<Department> parent = departmentRepository.findById(request.getParentId());
            if (parent.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy phòng ban cha"));
            }
            dept.setParent(parent.get());
        }
        
        if (request.getManagerId() != null) {
            Optional<User> manager = userRepository.findById(request.getManagerId());
            if (manager.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy người phụ trách"));
            }
            dept.setManager(manager.get());
        }
        
        Department saved = departmentRepository.save(dept);
        return ResponseEntity.ok(ApiResponse.success("Tạo phòng ban thành công", convertToDto(saved)));
    }

    // Lấy danh sách phòng ban dạng phẳng (để làm dropdown chọn phòng ban cha)
    @GetMapping("/flat")
    public ResponseEntity<?> getFlatDepartments() {
        List<DepartmentDto> list = departmentRepository.findAll().stream()
                .map(d -> {
                    DepartmentDto dto = new DepartmentDto();
                    dto.setId(d.getId());
                    dto.setName(d.getName());
                    return dto;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Success", list));
    }

    // Lấy danh sách User (để làm dropdown chọn người phụ trách)
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<UserDto> list = userRepository.findAll().stream()
                .map(u -> {
                    UserDto dto = new UserDto();
                    dto.setId(u.getId());
                    dto.setFullName(u.getFullName() != null ? u.getFullName() : u.getUsername());
                    return dto;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Success", list));
    }

    // 3. Bổ sung chức năng gán người phụ trách cho từng phòng ban (KN-175)
    @PutMapping("/{id}/manager")
    public ResponseEntity<?> assignManager(@PathVariable Long id, @RequestBody AssignManagerRequest request) {
        Optional<Department> deptOpt = departmentRepository.findById(id);
        if (deptOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy phòng ban"));
        }
        
        Department dept = deptOpt.get();
        Optional<User> managerOpt = userRepository.findById(request.getManagerId());
        
        if (managerOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy người dùng"));
        }
        
        dept.setManager(managerOpt.get());
        departmentRepository.save(dept);
        
        return ResponseEntity.ok(ApiResponse.success("Gán người phụ trách thành công", convertToDto(dept)));
    }

    // 4. Triển khai quy tắc không xoá phòng ban đang có yêu cầu tuyển dụng mở (KN-176)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        Optional<Department> deptOpt = departmentRepository.findById(id);
        if (deptOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy phòng ban"));
        }
        
        Department dept = deptOpt.get();
        
        // Kiểm tra xem phòng ban có yêu cầu tuyển dụng mở không
        List<JobRequest> jobRequests = jobRequestRepository.findByDepartmentId(id);
        boolean hasOpenRequest = jobRequests.stream()
                .anyMatch(req -> "OPEN".equalsIgnoreCase(req.getStatus()) || "APPROVED".equalsIgnoreCase(req.getStatus()));
                
        if (hasOpenRequest) {
            // Không xóa được, chỉ ngừng áp dụng
            dept.setStatus("INACTIVE");
            departmentRepository.save(dept);
            return ResponseEntity.ok(ApiResponse.success("Phòng ban có yêu cầu tuyển dụng mở. Đã chuyển trạng thái sang Ngừng áp dụng.", convertToDto(dept)));
        } else {
            // Xóa cứng
            departmentRepository.delete(dept);
            return ResponseEntity.ok(ApiResponse.success("Xóa phòng ban thành công"));
        }
    }

    // Helper method to convert Entity to DTO to prevent infinite JSON recursion on parent/children
    private DepartmentDto convertToDto(Department dept) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(dept.getId());
        dto.setName(dept.getName());
        dto.setDescription(dept.getDescription());
        dto.setStatus(dept.getStatus());
        
        if (dept.getManager() != null) {
            dto.setManagerId(dept.getManager().getId());
            dto.setManagerName(dept.getManager().getFullName() != null ? dept.getManager().getFullName() : dept.getManager().getUsername());
        }
        
        if (dept.getParent() != null) {
            dto.setParentId(dept.getParent().getId());
        }
        
        if (dept.getChildren() != null && !dept.getChildren().isEmpty()) {
            dto.setChildren(dept.getChildren().stream().map(this::convertToDto).collect(Collectors.toList()));
        }
        return dto;
    }

    // DTOs embedded for single-file approach
    @Data
    public static class CreateDepartmentRequest {
        private String name;
        private String description;
        private Long parentId;
        private Long managerId;
    }

    @Data
    public static class AssignManagerRequest {
        private Long managerId;
    }

    @Data
    public static class DepartmentDto {
        private Long id;
        private String name;
        private String description;
        private String status;
        private Long parentId;
        private Long managerId;
        private String managerName;
        private List<DepartmentDto> children;
    }

    @Data
    public static class UserDto {
        private Long id;
        private String fullName;
    }
}
