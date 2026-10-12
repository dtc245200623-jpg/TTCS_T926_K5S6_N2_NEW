package com.example.auth.repository;

import com.example.auth.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByTemplateKeyAndEnabledTrue(String templateKey);
    Optional<EmailTemplate> findByTemplateKey(String templateKey);
    List<EmailTemplate> findAllByOrderByTemplateKeyAsc();
}
