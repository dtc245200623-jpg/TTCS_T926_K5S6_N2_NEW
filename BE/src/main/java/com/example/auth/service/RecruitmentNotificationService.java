package com.example.auth.service;

import com.example.auth.entity.Application;
import com.example.auth.entity.EmailTemplate;
import com.example.auth.entity.InAppNotification;
import com.example.auth.repository.EmailTemplateRepository;
import com.example.auth.repository.InAppNotificationRepository;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecruitmentNotificationService {
    private static final Set<String> STAFF_ROLES = Set.of("ROLE_ADMIN", "ROLE_HR_MANAGER", "ROLE_RECRUITER");
    private final InAppNotificationRepository notifications;
    private final EmailTemplateRepository templates;
    private final UserRepository users;
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username:no-reply@example.com}")
    private String fromAddress;

    public void notifyStaff(String title, String message) {
        users.findAll().stream().filter(user -> user.isActive() && STAFF_ROLES.contains(user.getRole())).forEach(user -> notifyUser(user.getUsername(), title, message));
    }

    public void notifyUser(String username, String title, String message) {
        if (username == null || username.isBlank()) return;
        InAppNotification notification = new InAppNotification();
        notification.setRecipientUsername(username);
        notification.setTitle(title);
        notification.setMessage(message);
        notifications.save(notification);
    }

    public void notifyCandidate(Application application, String templateKey) {
        String recipient = application.getCandidate().getEmail();
        if (recipient == null || recipient.isBlank()) return;
        String candidateName = application.getCandidate().getFullName();
        String jobTitle = application.getJob().getTitle();
        String stage = application.getStage();
        EmailTemplate template = templates.findByTemplateKeyAndEnabledTrue(templateKey).orElse(null);
        String subject = template == null ? "Cập nhật hồ sơ ứng tuyển: " + jobTitle : template.getSubject();
        String body = template == null
                ? "Xin chào " + candidateName + ",\n\nHồ sơ của bạn cho vị trí " + jobTitle + " đã chuyển sang trạng thái: " + stage + "."
                : template.getBody();
        body = body.replace("{{candidateName}}", candidateName == null ? "bạn" : candidateName)
                .replace("{{jobTitle}}", jobTitle == null ? "vị trí tuyển dụng" : jobTitle)
                .replace("{{stage}}", stage == null ? "đang được xem xét" : stage);
        subject = subject.replace("{{candidateName}}", candidateName == null ? "bạn" : candidateName)
                .replace("{{jobTitle}}", jobTitle == null ? "vị trí tuyển dụng" : jobTitle)
                .replace("{{stage}}", stage == null ? "đang được xem xét" : stage);
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromAddress); mail.setTo(recipient); mail.setSubject(subject); mail.setText(body);
        try { mailSender.send(mail); }
        catch (MailException ex) { log.warn("Email template {} not delivered to {}: {}", templateKey, recipient, ex.getMessage()); }
    }

    public void seedTemplates() {
        seed("APPLICATION_RECEIVED", "Đã nhận hồ sơ ứng tuyển – {{jobTitle}}",
                "Xin chào {{candidateName}},\n\nChúng tôi đã nhận hồ sơ ứng tuyển vị trí {{jobTitle}}. Mã trạng thái được gửi trên trang ứng tuyển.");
        seed("STAGE_CHANGED", "Cập nhật hồ sơ – {{jobTitle}}",
                "Xin chào {{candidateName}},\n\nHồ sơ ứng tuyển {{jobTitle}} của bạn hiện ở giai đoạn {{stage}}.");
        seed("INTERVIEW_SCHEDULED", "Lịch phỏng vấn – {{jobTitle}}",
                "Xin chào {{candidateName}},\n\nNhà tuyển dụng đã cập nhật lịch phỏng vấn cho vị trí {{jobTitle}}. Vui lòng tra cứu hồ sơ để xem chi tiết.");
    }

    private void seed(String key, String subject, String body) {
        if (templates.findByTemplateKey(key).isPresent()) return;
        EmailTemplate template = new EmailTemplate();
        template.setTemplateKey(key); template.setSubject(subject); template.setBody(body);
        template.setUpdatedBy("system"); template.setUpdatedAt(LocalDateTime.now());
        templates.save(template);
    }
}
