package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "in_app_notifications")
@Getter @Setter @NoArgsConstructor
public class InAppNotification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String recipientUsername;
    @Column(nullable = false, length = 160)
    private String title;
    @Column(nullable = false, length = 1000)
    private String message;
    @Column(nullable = false)
    private boolean read = false;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
