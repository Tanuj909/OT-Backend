package com.ot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OtManagementSystemApplication 
{	
	public static void main(String[] args) 
	{
		SpringApplication.run(OtManagementSystemApplication.class, args);
	}

}
			