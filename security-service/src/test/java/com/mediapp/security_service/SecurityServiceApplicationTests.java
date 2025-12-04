package com.mediapp.security_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "eureka.client.enabled=false")
@ActiveProfiles("test")
class SecurityServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
