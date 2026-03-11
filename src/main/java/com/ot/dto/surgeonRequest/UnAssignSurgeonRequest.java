package com.ot.dto.surgeonRequest;

import java.util.Set;

import lombok.Data;

@Data
public class UnAssignSurgeonRequest {
 private Set<Long> surgeonIds;
}
