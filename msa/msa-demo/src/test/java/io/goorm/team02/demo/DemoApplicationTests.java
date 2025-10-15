package io.goorm.team02.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
	"spring.cloud.openfeign.enabled=false"
})
@ActiveProfiles("test")
class DemoApplicationTests {

	@Test
	void contextLoads() {
		// Test with Feign disabled
	}

}
