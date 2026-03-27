package com.ot.mapper;

import com.ot.dto.response.AvailableUserResponse;
import com.ot.entity.User;

public class AvailableUserMapper {
	
	public static AvailableUserResponse mapToResponse(User user) {
	    return AvailableUserResponse.builder()
	            .id(user.getId())
	            .userName(user.getUserName())
	            .email(user.getEmail())
	            .role(user.getRole())
	            .build();
	}

}
