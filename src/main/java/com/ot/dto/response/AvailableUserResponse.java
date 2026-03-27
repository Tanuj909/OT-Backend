package com.ot.dto.response;

import com.ot.enums.RoleType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvailableUserResponse {
    private Long id;
    private String userName;
    private String email;
    private RoleType role;
}
