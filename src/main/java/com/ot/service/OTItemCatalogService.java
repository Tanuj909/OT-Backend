package com.ot.service;

import java.util.List;

import com.ot.dto.oTItemCatalog.OTItemCatalogRequest;
import com.ot.dto.oTItemCatalog.OTItemCatalogResponse;
import com.ot.dto.oTItemCatalog.OTItemCatalogUpdateRequest;
import com.ot.enums.CatalogItemType;

public interface OTItemCatalogService {
    OTItemCatalogResponse createItem(OTItemCatalogRequest request);
    OTItemCatalogResponse getItemById(Long itemId);
    List<OTItemCatalogResponse> getAllItems(CatalogItemType itemType, String category, Boolean isActive);
    List<OTItemCatalogResponse> searchItems(String keyword);
    List<OTItemCatalogResponse> getItemsByType(CatalogItemType itemType);
    OTItemCatalogResponse updateItem(Long itemId, OTItemCatalogUpdateRequest request);
    void deactivateItem(Long itemId);    // Soft delete
    void activateItem(Long itemId);      // Reactivate
    void hardDeleteItem(Long itemId);    // SUPER_ADMIN only
}