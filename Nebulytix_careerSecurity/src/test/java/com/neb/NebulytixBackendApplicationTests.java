package com.neb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
//added for testing purpose for the H2 data without mysql just for testing the application
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test") 
@SpringBootTest
class NebulytixBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
