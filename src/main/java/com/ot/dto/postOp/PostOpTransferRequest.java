package com.ot.dto.postOp;

import lombok.Data;

@Data
public class PostOpTransferRequest {
	private Long wardId;    // "ICU", "Ward 4B"
    private String transferredBy;
    private String receivedBy;
}