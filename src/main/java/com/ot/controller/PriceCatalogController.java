package com.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ot.dto.priceCatalog.PriceCatalogRequest;
import com.ot.dto.priceCatalog.PriceCatalogResponse;
import com.ot.dto.priceCatalog.PriceCatalogUpdateRequest;
import com.ot.dto.response.ApiResponse;
import com.ot.enums.CatalogItemType;
import com.ot.service.PriceCatalogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/price-catalog")
@RequiredArgsConstructor
public class PriceCatalogController {

    private final PriceCatalogService priceCatalogService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PriceCatalogResponse>> createPrice(
            @RequestBody PriceCatalogRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Price created successfully",
                        priceCatalogService.createPrice(request)));
    }

    @GetMapping("/{priceId}")
    public ResponseEntity<ApiResponse<PriceCatalogResponse>> getPriceById(
            @PathVariable Long priceId) {

        return ResponseEntity.ok(ApiResponse.success("Price fetched successfully",
                priceCatalogService.getPriceById(priceId)));
    }

    @GetMapping("/item/{catalogItemId}")
    public ResponseEntity<ApiResponse<PriceCatalogResponse>> getPriceByCatalogItemId(
            @PathVariable Long catalogItemId) {

        return ResponseEntity.ok(ApiResponse.success("Price fetched successfully",
                priceCatalogService.getPriceByCatalogItemId(catalogItemId)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PriceCatalogResponse>>> getAllPrices(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) CatalogItemType itemType) {

        return ResponseEntity.ok(ApiResponse.success("Prices fetched successfully",
                priceCatalogService.getAllPrices(isActive, itemType)));
    }

    @PutMapping("/{priceId}/update")
    public ResponseEntity<ApiResponse<PriceCatalogResponse>> updatePrice(
            @PathVariable Long priceId,
            @RequestBody PriceCatalogUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Price updated successfully",
                priceCatalogService.updatePrice(priceId, request)));
    }

    @PatchMapping("/{priceId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivatePrice(
            @PathVariable Long priceId) {

        priceCatalogService.deactivatePrice(priceId);
        return ResponseEntity.ok(ApiResponse.success("Price deactivated successfully", null));
    }

    @PatchMapping("/{priceId}/activate")
    public ResponseEntity<ApiResponse<Void>> activatePrice(
            @PathVariable Long priceId) {

        priceCatalogService.activatePrice(priceId);
        return ResponseEntity.ok(ApiResponse.success("Price activated successfully", null));
    }
}
