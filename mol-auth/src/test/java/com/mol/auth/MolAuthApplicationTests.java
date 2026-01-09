package com.mol.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("auth") // 2. 【核心】指定加载 application-auth.yml
class MolAuthApplicationTests {
    
    @Test
    void contextLoads() {
    }
    
}
