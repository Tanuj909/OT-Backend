package com.ot.controller;

import com.ot.dto.request.LoginRequest;
import com.ot.dto.response.ApiResponse;
import com.ot.dto.response.LoginResponse;
import com.ot.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	
	
	private final AuthService authService;

//---------------------------------Login---------------------------------//
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest){
    	
    	LoginResponse response = authService.login(request);
    	
    	return ApiResponse.success("Login Successful", response);
    	
    }
    
//-------------------------------------Log Out(For All Users)--------------------------------//
  	@PostMapping("/logout")
  	public ApiResponse<Void> logout() {
  	    return ApiResponse.success("Logged out successfully", null);
  	}
}