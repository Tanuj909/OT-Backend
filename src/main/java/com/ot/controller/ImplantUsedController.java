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

import com.ot.dto.implantUsed.ImplantUsedRequest;
import com.ot.dto.implantUsed.ImplantUsedResponse;
import com.ot.dto.implantUsed.ImplantUsedUpdateRequest;
import com.ot.dto.response.ApiResponse;
import com.ot.service.ImplantUsedService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class ImplantUsedController {

    private final ImplantUsedService implantUsedService;

    @PostMapping("/{operationId}/implants/add")
    public ResponseEntity<ApiResponse<ImplantUsedResponse>> addImplant(
            @PathVariable Long operationId,
            @RequestBody ImplantUsedRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Implant added successfully",
                        implantUsedService.addImplant(operationId, request)));
    }

    @GetMapping("/{operationId}/implants")
    public ResponseEntity<ApiResponse<List<ImplantUsedResponse>>> getImplants(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Implants fetched successfully",
                implantUsedService.getImplants(operationId)));
    }

    @PatchMapping("/{operationId}/implants/{implantId}/update")
    public ResponseEntity<ApiResponse<ImplantUsedResponse>> updateImplant(
            @PathVariable Long operationId,
            @PathVariable Long implantId,
            @RequestBody ImplantUsedUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Implant updated successfully",
                implantUsedService.updateImplant(operationId, implantId, request)));
    }

    @DeleteMapping("/{operationId}/implants/{implantId}/remove")
    public ResponseEntity<ApiResponse<Void>> removeImplant(
            @PathVariable Long operationId,
            @PathVariable Long implantId) {

        implantUsedService.removeImplant(operationId, implantId);
        return ResponseEntity.ok(ApiResponse.success("Implant removed successfully", null));
    }
}