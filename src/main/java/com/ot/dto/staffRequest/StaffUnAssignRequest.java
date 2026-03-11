package com.ot.dto.staffRequest;

import java.util.Set;

import lombok.Data;

@Data
public class StaffUnAssignRequest {
    private Set<Long> staffIds;
}