package com.ot.billing.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
	    name = "ipd-service",
	    url = "${ipd.service.url}"
	)
	public interface IPDFeignClient {

	    @PostMapping("/api/ipd/return-request")
	    void notifyReturnFromOT(@RequestBody Map<String, Object> request);
	}