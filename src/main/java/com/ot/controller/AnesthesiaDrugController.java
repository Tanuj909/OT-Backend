package com.ot.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ot.dto.anesthesiaDrug.AnesthesiaDrugRequest;
import com.ot.dto.anesthesiaDrug.AnesthesiaDrugResponse;
import com.ot.dto.anesthesiaDrug.AnesthesiaDrugSummaryResponse;
import com.ot.dto.anesthesiaDrug.AnesthesiaDrugUpdateRequest;
import com.ot.dto.response.ApiResponse;
import com.ot.service.AnesthesiaDrugService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/anesthesia-drugs")
@RequiredArgsConstructor
public class AnesthesiaDrugController {

    private final AnesthesiaDrugService anesthesiaDrugService;

    @PostMapping("/{operationId}/add")
    public ResponseEntity<ApiResponse<AnesthesiaDrugResponse>> addDrug(
            @PathVariable Long operationId,
            @RequestBody AnesthesiaDrugRequest request) {

        AnesthesiaDrugResponse response = anesthesiaDrugService.addDrug(operationId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Anesthesia drug added successfully", response));
    }

    @GetMapping("/{operationId}/get")
    public ResponseEntity<ApiResponse<List<AnesthesiaDrugResponse>>> getDrugs(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Anesthesia drugs fetched successfully",
                anesthesiaDrugService.getDrugs(operationId)));
    }

    @PatchMapping("/{operationId}/update/{drugId}")
    public ResponseEntity<ApiResponse<AnesthesiaDrugResponse>> updateDrug(
            @PathVariable Long operationId,
            @PathVariable Long drugId,
            @RequestBody AnesthesiaDrugUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Anesthesia drug updated successfully",
                anesthesiaDrugService.updateDrug(operationId, drugId, request)));
    }

    @DeleteMapping("/{operationId}/remove/{drugId}")
    public ResponseEntity<ApiResponse<Void>> removeDrug(
            @PathVariable Long operationId,
            @PathVariable Long drugId) {

        anesthesiaDrugService.removeDrug(operationId, drugId);
        return ResponseEntity.ok(ApiResponse.success("Anesthesia drug removed successfully", null));
    }

    @GetMapping("/{operationId}/summary")
    public ResponseEntity<ApiResponse<AnesthesiaDrugSummaryResponse>> getDrugSummary(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Anesthesia drug summary fetched successfully",
                anesthesiaDrugService.getDrugSummary(operationId)));
    }
}
