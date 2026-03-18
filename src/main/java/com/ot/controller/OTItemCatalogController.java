package com.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ot.dto.oTItemCatalog.OTItemCatalogRequest;
import com.ot.dto.oTItemCatalog.OTItemCatalogResponse;
import com.ot.dto.oTItemCatalog.OTItemCatalogUpdateRequest;
import com.ot.dto.response.ApiResponse;
import com.ot.enums.CatalogItemType;
import com.ot.service.OTItemCatalogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class OTItemCatalogController {

    private final OTItemCatalogService catalogService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OTItemCatalogResponse>> createItem(
            @RequestBody OTItemCatalogRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item created successfully",
                        catalogService.createItem(request)));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<OTItemCatalogResponse>> getItemById(
            @PathVariable Long itemId) {

        return ResponseEntity.ok(ApiResponse.success("Item fetched successfully",
                catalogService.getItemById(itemId)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<OTItemCatalogResponse>>> getAllItems(
            @RequestParam(required = false) CatalogItemType itemType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isActive) {

        return ResponseEntity.ok(ApiResponse.success("Items fetched successfully",
                catalogService.getAllItems(itemType, category, isActive)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<OTItemCatalogResponse>>> searchItems(
            @RequestParam String keyword) {

        return ResponseEntity.ok(ApiResponse.success("Search results fetched successfully",
                catalogService.searchItems(keyword)));
    }

    @GetMapping("/type/{itemType}")
    public ResponseEntity<ApiResponse<List<OTItemCatalogResponse>>> getItemsByType(
            @PathVariable CatalogItemType itemType) {

        return ResponseEntity.ok(ApiResponse.success("Items fetched successfully",
                catalogService.getItemsByType(itemType)));
    }

    @PutMapping("/{itemId}/update")
    public ResponseEntity<ApiResponse<OTItemCatalogResponse>> updateItem(
            @PathVariable Long itemId,
            @RequestBody OTItemCatalogUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Item updated successfully",
                catalogService.updateItem(itemId, request)));
    }

    @PatchMapping("/{itemId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateItem(
            @PathVariable Long itemId) {

        catalogService.deactivateItem(itemId);
        return ResponseEntity.ok(ApiResponse.success("Item deactivated successfully", null));
    }

    @PatchMapping("/{itemId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateItem(
            @PathVariable Long itemId) {

        catalogService.activateItem(itemId);
        return ResponseEntity.ok(ApiResponse.success("Item activated successfully", null));
    }

    @DeleteMapping("/{itemId}/delete")
    public ResponseEntity<ApiResponse<Void>> hardDeleteItem(
            @PathVariable Long itemId) {

        catalogService.hardDeleteItem(itemId);
        return ResponseEntity.ok(ApiResponse.success("Item permanently deleted", null));
    }
}