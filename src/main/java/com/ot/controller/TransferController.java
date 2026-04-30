package com.ot.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ot.service.TransferService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ot")
@RequiredArgsConstructor
public class TransferController {
	
	private final TransferService transferService;
	
	
//-----------------------------------This API Send the Request to the IPD(That Patient is Ready for Transfer back to IPD)
	@PostMapping("/{operationId}/ready-for-ipd-transfer")
	public ResponseEntity<String> readyForIpdTransfer(@PathVariable Long operationId) {

		transferService.markReadyForIpdTransfer(operationId);

	    return ResponseEntity.ok("Patient ready for transfer to IPD");
	}
	
	
//-----------------------------------This API's is hit buy IPD API(A hand shake API to Inform OT that Patient is Received)
	@PutMapping("/mark-accepted-by-ipd")
	public ResponseEntity<String> markAcceptedByIpd(@RequestBody Map<String, Long> request) {

		transferService.markAcceptedByIpd(request.get("admissionId"));

	    return ResponseEntity.ok("Transfer marked as accepted by IPD");
	}
	
}
