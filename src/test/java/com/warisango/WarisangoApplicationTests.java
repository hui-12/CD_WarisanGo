package com.warisango;

import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = "firebase.enabled=false")
class WarisangoApplicationTests {

	@MockitoBean
	private Firestore firestore;

	@Test
	void contextLoads() {
	}
}
