package com.example.auth.repository;

import com.example.auth.entity.OrganizationCatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrganizationCatalogRepository extends JpaRepository<OrganizationCatalogItem, Long> {
    List<OrganizationCatalogItem> findByCatalogTypeOrderByNameAsc(String catalogType);
    Optional<OrganizationCatalogItem> findByCatalogTypeAndNameIgnoreCase(String catalogType, String name);
}
