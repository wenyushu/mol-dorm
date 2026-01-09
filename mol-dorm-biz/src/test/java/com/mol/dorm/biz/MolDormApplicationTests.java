package com.mol.dorm.biz;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dorm") // 【核心】指定加载 application-dorm.yml
class MolDormApplicationTests {
    
    @Test
    void contextLoads() {
    }
    
}
