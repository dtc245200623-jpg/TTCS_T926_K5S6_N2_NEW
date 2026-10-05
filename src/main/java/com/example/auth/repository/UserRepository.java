package com.example.auth.repository;

import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE " +
           "(:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.department) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:role IS NULL OR r = :role) " +
           "AND (:isLocked IS NULL OR u.isLocked = :isLocked)")
    org.springframework.data.domain.Page<User> searchUsers(
            @Param("search") String search, 
            @Param("role") String role, 
            @Param("isLocked") Boolean isLocked, 
            org.springframework.data.domain.Pageable pageable);

    /**
     * Tăng tokenVersion trực tiếp trong DB (atomic operation)
     */
    @Modifying
    @Query("UPDATE User u SET u.tokenVersion = u.tokenVersion + 1 WHERE u.id = :userId")
    int incrementTokenVersion(@Param("userId") Long userId);
}
