package com.example.auth;

import com.example.auth.entity.User;
import com.example.auth.entity.Application;
import com.example.auth.entity.Candidate;
import com.example.auth.entity.CandidateEvaluation;
import com.example.auth.entity.JobOpening;
import com.example.auth.entity.OrganizationCatalogItem;
import com.example.auth.repository.ApplicationRepository;
import com.example.auth.repository.CandidateEvaluationRepository;
import com.example.auth.repository.CandidateRepository;
import com.example.auth.repository.JobOpeningRepository;
import com.example.auth.repository.OrganizationCatalogRepository;
import com.example.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.auth.service.RecruitmentNotificationService;

@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    /**
     * Khởi tạo tài khoản mẫu để kiểm thử tính năng ngay khi khởi chạy
     */
    @Bean
    @SuppressWarnings("null")
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JobOpeningRepository jobs, CandidateRepository candidates,
            ApplicationRepository applications, CandidateEvaluationRepository evaluations,
            OrganizationCatalogRepository catalogs, RecruitmentNotificationService notifications) {
        return args -> {
            if (userRepository.findByUsername("testuser").isEmpty()) {
                User user = User.builder()
                        .username("testuser")
                        .email("testuser@example.com")
                        .password(passwordEncoder.encode("OldPassword123"))
                        .tokenVersion(1L)
                        .role("ROLE_RECRUITER")
                        .build();
                userRepository.save(user);
                System.out.println(">>> Đã khởi tạo người dùng mẫu: testuser / OldPassword123 (tokenVersion: 1)");
            }
            userRepository.findByUsername("testuser").ifPresent(user -> {
                if ("ROLE_USER".equals(user.getRole())) {
                    user.setRole("ROLE_RECRUITER");
                    userRepository.save(user);
                }
            });
            if (userRepository.findByUsername("admin").isEmpty()) {
                userRepository.save(User.builder().username("admin").email("admin@example.com")
                        .fullName("Quản trị hệ thống").password(passwordEncoder.encode("Admin123456!"))
                        .tokenVersion(1L).role("ROLE_ADMIN").active(true).build());
                System.out.println(">>> Tài khoản quản trị mẫu: admin / Admin123456! (hãy đổi mật khẩu sau khi đăng nhập)");
            }
            if (jobs.count() == 0) {
                JobOpening recruiter = createJob("Chuyên viên tuyển dụng", "Nhân sự",
                        "Phụ trách đăng tin, sàng lọc hồ sơ và phối hợp phỏng vấn.");
                JobOpening engineer = createJob("Kỹ sư Backend Java", "Công nghệ",
                        "Phát triển API tuyển dụng bằng Java và Spring Boot.");
                JobOpening designer = createJob("Chuyên viên thiết kế sản phẩm", "Sản phẩm",
                        "Thiết kế trải nghiệm và giao diện cho các công cụ nội bộ.");
                jobs.saveAll(java.util.List.of(recruiter, engineer, designer));

                Candidate employee = createCandidate("Nguyễn Minh Anh", "testuser@example.com", "Ứng tuyển nội bộ");
                Candidate interview = createCandidate("Trần Bảo Ngọc", "bao.ngoc@example.com", "Giới thiệu nội bộ");
                Candidate offer = createCandidate("Lê Thu Linh", "thu.linh@example.com", "Cổng tuyển dụng");
                candidates.saveAll(java.util.List.of(employee, interview, offer));

                Application employeeApplication = createApplication(employee, recruiter, "SCREENING");
                Application interviewApplication = createApplication(interview, engineer, "INTERVIEW");
                interviewApplication.setInterviewAt(java.time.LocalDateTime.now().plusDays(1)
                        .withHour(10).withMinute(0).withSecond(0).withNano(0));
                Application offerApplication = createApplication(offer, designer, "OFFER");
                applications.saveAll(java.util.List.of(employeeApplication, interviewApplication, offerApplication));

                CandidateEvaluation evaluation = new CandidateEvaluation();
                evaluation.setApplication(offerApplication);
                evaluation.setSkillScore(5);
                evaluation.setProblemSolvingScore(4);
                evaluation.setComments("Hồ sơ phù hợp; hội đồng đề xuất gửi offer.");
                evaluation.setSalaryOffer("35.000.000 VND/tháng");
                evaluation.setDecision("PASS");
                evaluations.save(evaluation);
            }
            seedCatalog(catalogs, "DEPARTMENT", "Nhân sự", "Bộ phận phụ trách nhân sự và tuyển dụng.");
            seedCatalog(catalogs, "DEPARTMENT", "Công nghệ", "Bộ phận phát triển sản phẩm công nghệ.");
            seedCatalog(catalogs, "JOB_TITLE", "Chuyên viên tuyển dụng", "Phụ trách quy trình tuyển dụng.");
            seedCatalog(catalogs, "JOB_TITLE", "Kỹ sư Backend Java", "Phát triển dịch vụ backend.");
            seedCatalog(catalogs, "COMPETENCY", "Giao tiếp", "Khả năng truyền đạt và phối hợp.");
            seedCatalog(catalogs, "COMPETENCY", "Giải quyết vấn đề", "Khả năng phân tích và xử lý tình huống.");
            seedCatalog(catalogs, "INTERVIEW_QUESTION", "Hãy kể về một vấn đề khó bạn đã giải quyết.", "Giải quyết vấn đề");
            notifications.seedTemplates();
        };
    }

    private static JobOpening createJob(String title, String department, String description) {
        JobOpening job = new JobOpening();
        job.setTitle(title);
        job.setDepartment(department);
        job.setDescription(description);
        job.setHeadcount(1);
        job.setStatus("OPEN");
        return job;
    }

    private static Candidate createCandidate(String name, String email, String source) {
        Candidate candidate = new Candidate();
        candidate.setFullName(name);
        candidate.setEmail(email);
        candidate.setSource(source);
        return candidate;
    }

    private static Application createApplication(Candidate candidate, JobOpening job, String stage) {
        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setStage(stage);
        return application;
    }

    private static void seedCatalog(OrganizationCatalogRepository catalogs, String type, String name, String description) {
        if (catalogs.findByCatalogTypeAndNameIgnoreCase(type, name).isPresent()) return;
        OrganizationCatalogItem item = new OrganizationCatalogItem();
        item.setCatalogType(type);
        item.setName(name);
        item.setDescription(description);
        catalogs.save(item);
    }
}
