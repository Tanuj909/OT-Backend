package com.ot.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import com.ot.dto.request.LoginRequest;
import com.ot.dto.response.ApiResponse;
import com.ot.dto.response.LoginResponse;
import com.ot.entity.User;
import com.ot.exception.BadRequestException;
import com.ot.exception.ResourceNotFoundException;
import com.ot.repository.UserRepository;
import com.ot.security.CustomUserDetails;
import com.ot.security.JwtService;
import com.ot.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{
	
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
	public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No user is logged in");
        }
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal(); 
        return cud.getUser(); 
    }
    
//-------------------------------------Login(For All Users)--------------------------------//
	@Override
	public LoginResponse login(LoginRequest request) {
		
		//check if Email Exists or not
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(()-> new ResourceNotFoundException("You are not Registered!"));
		
		//Check Password
		if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new BadRequestException("Invalid email or Password");
		}
		
		//Check Account is Active
	    if (!user.getIsActive()) {
	        throw new BadRequestException("User account is inactive");
	    }
	    
	    String token = jwtService.generateToken(user.getEmail());
	    LoginResponse response = new LoginResponse();
	    response.setEmail(user.getEmail());
	    response.setRole(user.getRole().name());
	    response.setToken(token);
	    
		return response;
	}
	


}
