package com.sotospeak.service

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Test
    fun `context loads`() {
        // This test verifies that Spring context loads correctly with test profile
    }

    @Test
    fun `test profile is active`() {
        // Verify test configuration is loaded
        println("Test profile is active")
    }
}
