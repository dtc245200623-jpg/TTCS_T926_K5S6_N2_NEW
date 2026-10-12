package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_templates")
@Getter @Setter @NoArgsConstructor
public class EmailTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 48)
    private String templateKey;
    @Column(nullable = false, length = 180)
    private String subject;
    @Column(nullable = false, length = 5000)
    private String body;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(nullable = false, length = 80)
    private String updatedBy;
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
