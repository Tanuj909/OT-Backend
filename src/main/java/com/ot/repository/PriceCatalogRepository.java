package com.ot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.Hospital;
import com.ot.entity.OTItemCatalog;
import com.ot.entity.PriceCatalog;
import com.ot.enums.CatalogItemType;

public interface PriceCatalogRepository extends JpaRepository<PriceCatalog, Long> {
    Optional<PriceCatalog> findByCatalogItem(OTItemCatalog catalogItem);
    Optional<PriceCatalog> findByCatalogItemId(Long catalogItemId);
    boolean existsByCatalogItem(OTItemCatalog catalogItem);
    List<PriceCatalog> findByHospitalAndIsActive(Hospital hospital, Boolean isActive);
    List<PriceCatalog> findByHospitalAndCatalogItemItemType(Hospital hospital, CatalogItemType itemType);
}