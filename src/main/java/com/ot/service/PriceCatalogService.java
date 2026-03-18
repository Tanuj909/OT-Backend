package com.ot.service;

import java.util.List;

import com.ot.dto.priceCatalog.PriceCatalogRequest;
import com.ot.dto.priceCatalog.PriceCatalogResponse;
import com.ot.dto.priceCatalog.PriceCatalogUpdateRequest;
import com.ot.enums.CatalogItemType;

public interface PriceCatalogService {
    PriceCatalogResponse createPrice(PriceCatalogRequest request);
    PriceCatalogResponse getPriceByCatalogItemId(Long catalogItemId);
    PriceCatalogResponse getPriceById(Long priceId);
    List<PriceCatalogResponse> getAllPrices(Boolean isActive, CatalogItemType itemType);
    PriceCatalogResponse updatePrice(Long priceId, PriceCatalogUpdateRequest request);
    void deactivatePrice(Long priceId);
    void activatePrice(Long priceId);
}