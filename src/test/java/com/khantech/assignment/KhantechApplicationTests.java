package com.khantech.assignment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestContainerConfiguration.class)
@SpringBootTest
class KhantechApplicationTests {

    @Test
    void contextLoads() {
    }

}
