package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization_catalog_items", uniqueConstraints =
        @UniqueConstraint(columnNames = {"catalog_type", "name"}))
@Getter @Setter @NoArgsConstructor
public class OrganizationCatalogItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "catalog_type", nullable = false, length = 32)
    private String catalogType;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(length = 1000)
    private String description;
    @Column(precision = 14, scale = 2)
    private BigDecimal salaryMin;
    @Column(precision = 14, scale = 2)
    private BigDecimal salaryMax;
    @Column(nullable = false)
    private boolean active = true;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
