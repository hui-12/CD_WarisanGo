package com.warisango;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "firebase.enabled=false")
class WarisangoApplicationTests {

	@Test
	void contextLoads() {
	}
}
