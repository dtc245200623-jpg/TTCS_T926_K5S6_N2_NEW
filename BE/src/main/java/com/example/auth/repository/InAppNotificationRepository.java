package com.example.auth.repository;

import com.example.auth.entity.InAppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {
    List<InAppNotification> findTop100ByRecipientUsernameOrderByCreatedAtDesc(String username);
}
