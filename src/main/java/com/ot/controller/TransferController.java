package com.ot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ot.service.TransferService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ot")
@RequiredArgsConstructor
public class TransferController {
	
	private final TransferService transferService;
	
	@PostMapping("/{operationId}/ready-for-ipd-transfer")
	public ResponseEntity<String> readyForIpdTransfer(@PathVariable Long operationId) {

		transferService.markReadyForIpdTransfer(operationId);

	    return ResponseEntity.ok("Patient ready for transfer to IPD");
	}
	
}
