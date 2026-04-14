package com.ot.exception;

import org.springframework.http.HttpStatus;

public class BillingException extends ApiException {
	
	public BillingException(String message) {
		super(message, HttpStatus.CONFLICT);
	}

}
