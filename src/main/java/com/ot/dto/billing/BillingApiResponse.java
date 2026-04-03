package com.ot.dto.billing;

import java.time.LocalDateTime;

import lombok.Data;

//package com.ot.billing.dto

@Data
public class BillingApiResponse<T> {
 private boolean success;
 private String message;
 private T data;
 private LocalDateTime timestamp;
}