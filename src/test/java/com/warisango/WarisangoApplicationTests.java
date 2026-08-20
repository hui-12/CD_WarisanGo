package com.warisango;

import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;

@SpringBootTest(properties = "firebase.enabled=false")
@Import(WarisangoApplicationTests.FirebaseTestConfiguration.class)
class WarisangoApplicationTests {

	@Test
	void contextLoads() {
	}

    @TestConfiguration
    static class FirebaseTestConfiguration {

        @Bean
        Firestore firestore() {
            return mock(Firestore.class);
        }
    }

}
