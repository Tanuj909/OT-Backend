package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.Hospital;
import com.ot.entity.OTItemCatalog;
import com.ot.enums.CatalogItemType;

public interface OTItemCatalogRepository extends JpaRepository<OTItemCatalog, Long> {

    // Filters
    List<OTItemCatalog> findByHospitalAndItemType(Hospital hospital, CatalogItemType itemType);
    List<OTItemCatalog> findByHospitalAndCategory(Hospital hospital, String category);
    List<OTItemCatalog> findByHospitalAndIsActive(Hospital hospital, Boolean isActive);
    List<OTItemCatalog> findByHospital(Hospital hospital);

    // Combined filters
    List<OTItemCatalog> findByHospitalAndItemTypeAndCategoryAndIsActive(
            Hospital hospital, CatalogItemType itemType, String category, Boolean isActive);

    // itemCode unique check per hospital
    boolean existsByHospitalAndItemCode(Hospital hospital, String itemCode);

    // Search by name
    List<OTItemCatalog> findByHospitalAndItemNameContainingIgnoreCase(Hospital hospital, String itemName);
}