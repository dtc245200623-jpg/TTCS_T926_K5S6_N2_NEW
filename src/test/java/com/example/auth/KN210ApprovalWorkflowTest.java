package com.example.auth;

import com.example.auth.dto.ApprovalRuleDto;
import com.example.auth.dto.ApprovalRuleStepDto;
import com.example.auth.dto.JobRequestDto;
import com.example.auth.entity.ApprovalRule;
import com.example.auth.entity.Department;
import com.example.auth.entity.User;
import com.example.auth.exception.AppException;
import com.example.auth.repository.ApprovalRuleRepository;
import com.example.auth.repository.DepartmentRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ApprovalRuleService;
import com.example.auth.service.JobRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class KN210ApprovalWorkflowTest {

    @Autowired
    private ApprovalRuleService approvalRuleService;

    @Autowired
    private ApprovalRuleRepository approvalRuleRepository;

    @Autowired
    private JobRequestService jobRequestService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    private Department deptIT;
    private User hrManager;
    private User approver;
    private User hiringManager;

    @BeforeEach
    void setUp() {
        // Tạo phòng ban kiểm thử
        deptIT = departmentRepository.save(Department.builder()
                .name("Phòng Công nghệ Thông tin")
                .description("Phòng IT Tech")
                .status("ACTIVE")
                .build());

        // Tạo người dùng cho các vai trò
        hrManager = userRepository.findByUsername("hr_test").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("hr_test")
                        .email("hr_test@example.com")
                        .password("123456")
                        .roles(Set.of("ROLE_HR_MANAGER"))
                        .build()));

        approver = userRepository.findByUsername("approver_test").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("approver_test")
                        .email("approver_test@example.com")
                        .password("123456")
                        .roles(Set.of("ROLE_APPROVER"))
                        .build()));

        hiringManager = userRepository.findByUsername("hm_test").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("hm_test")
                        .email("hm_test@example.com")
                        .password("123456")
                        .roles(Set.of("ROLE_HIRING_MANAGER"))
                        .build()));
    }

    /**
     * KN-220: Khai báo chuỗi cấp duyệt theo phòng ban và theo mức lương đề xuất
     */
    @Test
    void testKN220_DeclareApprovalChain() {
        ApprovalRuleDto dto = ApprovalRuleDto.builder()
                .name("Chuỗi duyệt phòng IT - Dưới 20 triệu")
                .departmentId(deptIT.getId())
                .minSalary(0.0)
                .maxSalary(20000000.0)
                .overThresholdRole("ROLE_APPROVER")
                .steps(List.of(
                        ApprovalRuleStepDto.builder()
                                .stepOrder(1)
                                .stepName("Cấp 1: Trưởng bộ phận duyệt")
                                .approverRole("ROLE_HIRING_MANAGER")
                                .build(),
                        ApprovalRuleStepDto.builder()
                                .stepOrder(2)
                                .stepName("Cấp 2: Trưởng phòng Nhân sự duyệt")
                                .approverRole("ROLE_HR_MANAGER")
                                .build()
                ))
                .build();

        ApprovalRuleDto created = approvalRuleService.createRule(dto);
        assertNotNull(created.getId());
        assertEquals("Chuỗi duyệt phòng IT - Dưới 20 triệu", created.getName());
        assertEquals(deptIT.getId(), created.getDepartmentId());
        assertEquals(2, created.getSteps().size());
        assertEquals("ROLE_HIRING_MANAGER", created.getSteps().get(0).getApproverRole());
        assertEquals("ROLE_HR_MANAGER", created.getSteps().get(1).getApproverRole());
    }

    /**
     * KN-221: Yêu cầu vượt hạn mức tự động thêm cấp duyệt cao hơn
     */
    @Test
    void testKN221_OverBudgetAutoAddsHigherApprovalStep() {
        // Cấu hình chuỗi duyệt với hạn mức 20 triệu, nếu vượt tự động thêm ROLE_APPROVER
        ApprovalRuleDto ruleDto = ApprovalRuleDto.builder()
                .name("Quy trình chuẩn IT - Hạn mức 20M")
                .departmentId(deptIT.getId())
                .minSalary(0.0)
                .maxSalary(20000000.0)
                .overThresholdRole("ROLE_APPROVER")
                .steps(List.of(
                        ApprovalRuleStepDto.builder()
                                .stepOrder(1)
                                .stepName("Cấp 1: Trưởng phòng Nhân sự")
                                .approverRole("ROLE_HR_MANAGER")
                                .build()
                ))
                .build();
        approvalRuleService.createRule(ruleDto);

        // Trường hợp 1: Lương đề xuất trong hạn mức (18M <= 20M) -> Đúng 1 cấp duyệt
        JobRequestDto underBudget = JobRequestDto.builder()
                .title("Tuyển Junior Java")
                .departmentId(deptIT.getId())
                .headcount(1)
                .minSalary(10000000.0)
                .maxSalary(18000000.0)
                .build();
        JobRequestDto createdUnder = jobRequestService.createJobRequest(underBudget, hiringManager.getUsername());
        assertFalse(createdUnder.getIsOverBudget());
        assertEquals(1, createdUnder.getApprovalSteps().size());

        // Trường hợp 2: Lương đề xuất VƯỢT hạn mức (35M > 20M) -> Tự động thêm cấp duyệt cao hơn (ROLE_APPROVER)
        JobRequestDto overBudget = JobRequestDto.builder()
                .title("Tuyển Lead Architect")
                .departmentId(deptIT.getId())
                .headcount(1)
                .minSalary(25000000.0)
                .maxSalary(35000000.0)
                .build();
        JobRequestDto createdOver = jobRequestService.createJobRequest(overBudget, hiringManager.getUsername());

        assertTrue(createdOver.getIsOverBudget(), "JobRequest phải được đánh dấu vượt hạn mức");
        assertEquals(2, createdOver.getApprovalSteps().size(), "Phải tự động thêm 1 cấp duyệt cao hơn (tổng 2 cấp)");

        // Cấp 2 phải là cấp duyệt cao hơn được tự động thêm
        var extraStep = createdOver.getApprovalSteps().get(1);
        assertEquals(2, extraStep.getStepOrder());
        assertEquals("ROLE_APPROVER", extraStep.getApproverRole());
        assertTrue(extraStep.getIsOverThreshold(), "Cấp bổ sung phải được đánh dấu isOverThreshold");
        assertTrue(extraStep.getStepName().contains("vượt hạn mức"));
    }

    /**
     * KN-222: Cấu hình sai (không có cấp duyệt nào khớp) bị chặn lưu
     */
    @Test
    void testKN222_InvalidConfigBlocked() {
        // Trường hợp 1: Lưu cấu hình chuỗi duyệt mà không có cấp duyệt nào (steps rỗng) -> Bị chặn lưu
        ApprovalRuleDto emptyStepRule = ApprovalRuleDto.builder()
                .name("Chuỗi duyệt lỗi - Không có cấp duyệt nào")
                .departmentId(deptIT.getId())
                .minSalary(0.0)
                .maxSalary(20000000.0)
                .steps(new ArrayList<>())
                .build();

        AppException ex1 = assertThrows(AppException.class, () -> approvalRuleService.createRule(emptyStepRule));
        assertTrue(ex1.getMessage().contains("bị chặn lưu") || ex1.getMessage().contains("ít nhất 1 cấp duyệt"));

        // Trường hợp 2: Lưu chuỗi duyệt có cấp duyệt nhưng thiếu vai trò người duyệt -> Bị chặn lưu
        ApprovalRuleDto missingRoleRule = ApprovalRuleDto.builder()
                .name("Chuỗi duyệt lỗi - Thiếu vai trò")
                .departmentId(deptIT.getId())
                .steps(List.of(ApprovalRuleStepDto.builder().stepName("Bước 1").approverRole("").build()))
                .build();

        AppException ex2 = assertThrows(AppException.class, () -> approvalRuleService.createRule(missingRoleRule));
        assertTrue(ex2.getMessage().contains("chưa được chọn vai trò người duyệt") || ex2.getMessage().contains("bị chặn lưu"));

        // Trường hợp 3: Khi tạo yêu cầu tuyển dụng với phòng ban không có chuỗi duyệt nào khớp -> Bị chặn lưu
        Department isolatedDept = departmentRepository.save(Department.builder().name("Phòng Cô Đơn").build());
        // Xóa tạm các rule chung để test
        approvalRuleRepository.deleteAll();

        JobRequestDto request = JobRequestDto.builder()
                .title("Tuyển nhân sự phòng cô đơn")
                .departmentId(isolatedDept.getId())
                .headcount(1)
                .maxSalary(10000000.0)
                .build();

        AppException ex3 = assertThrows(AppException.class, () -> jobRequestService.createJobRequest(request, hiringManager.getUsername()));
        assertTrue(ex3.getMessage().contains("bị chặn lưu") || ex3.getMessage().contains("không có cấp duyệt nào khớp"));
    }

    /**
     * KN-235: Luồng đang chạy không bị ảnh hưởng khi cấu hình thay đổi (Snapshot pattern)
     */
    @Test
    void testKN235_InFlightWorkflowUnaffectedByConfigChange() {
        // 1. Tạo cấu hình chuỗi duyệt ban đầu: Gồm 2 cấp (HR_MANAGER -> APPROVER)
        ApprovalRuleDto initialRule = ApprovalRuleDto.builder()
                .name("Cấu hình ban đầu")
                .departmentId(deptIT.getId())
                .minSalary(0.0)
                .maxSalary(30000000.0)
                .steps(List.of(
                        ApprovalRuleStepDto.builder().stepOrder(1).stepName("Bước ban đầu 1").approverRole("ROLE_HR_MANAGER").build(),
                        ApprovalRuleStepDto.builder().stepOrder(2).stepName("Bước ban đầu 2").approverRole("ROLE_APPROVER").build()
                ))
                .build();
        ApprovalRuleDto createdRule = approvalRuleService.createRule(initialRule);

        // 2. Tạo JobRequest đang chạy theo cấu hình ban đầu
        JobRequestDto jobDto = JobRequestDto.builder()
                .title("Lập trình viên React")
                .departmentId(deptIT.getId())
                .headcount(1)
                .maxSalary(25000000.0)
                .build();
        JobRequestDto inFlightJob = jobRequestService.createJobRequest(jobDto, hiringManager.getUsername());

        assertEquals(2, inFlightJob.getApprovalSteps().size());
        assertEquals("Bước ban đầu 1", inFlightJob.getApprovalSteps().get(0).getStepName());

        // 3. HR Manager sau đó thay đổi cấu hình chuỗi duyệt (ví dụ: chỉ còn 1 cấp HIRING_MANAGER, hoặc đổi hạn mức)
        ApprovalRuleDto modifiedRule = ApprovalRuleDto.builder()
                .name("Cấu hình đã sửa đổi sau này")
                .departmentId(deptIT.getId())
                .minSalary(0.0)
                .maxSalary(10000000.0)
                .steps(List.of(
                        ApprovalRuleStepDto.builder().stepOrder(1).stepName("Bước đã thay đổi").approverRole("ROLE_HIRING_MANAGER").build()
                ))
                .build();
        approvalRuleService.updateRule(createdRule.getId(), modifiedRule);

        // 4. Kiểm tra lại JobRequest đang chạy: Chuỗi duyệt của nó PHẢI GIỮ NGUYÊN cấu hình snapshot ban đầu!
        JobRequestDto reloadedJob = jobRequestService.getJobRequestById(inFlightJob.getId());
        assertEquals(2, reloadedJob.getApprovalSteps().size(), "Số lượng cấp duyệt của luồng đang chạy không được thay đổi");
        assertEquals("Bước ban đầu 1", reloadedJob.getApprovalSteps().get(0).getStepName(), "Tên bước ban đầu phải giữ nguyên");
        assertEquals("ROLE_HR_MANAGER", reloadedJob.getApprovalSteps().get(0).getApproverRole(), "Vai trò bước ban đầu phải giữ nguyên");
        assertEquals("ROLE_APPROVER", reloadedJob.getApprovalSteps().get(1).getApproverRole());

        // 5. Tiến hành duyệt qua các bước ban đầu bình thường
        jobRequestService.approveOrRejectStep(reloadedJob.getId(), true, "HR đồng ý", hrManager.getUsername());
        JobRequestDto afterStep1 = jobRequestService.getJobRequestById(inFlightJob.getId());
        assertEquals("APPROVED", afterStep1.getApprovalSteps().get(0).getStatus());
        assertEquals("PENDING", afterStep1.getApprovalSteps().get(1).getStatus());

        jobRequestService.approveOrRejectStep(reloadedJob.getId(), true, "Ban Giám đốc đồng ý", approver.getUsername());
        JobRequestDto afterStep2 = jobRequestService.getJobRequestById(inFlightJob.getId());
        assertEquals("APPROVED", afterStep2.getStatus(), "Yêu cầu phải được duyệt hoàn toàn sau khi xong các bước snapshot");
    }
}
