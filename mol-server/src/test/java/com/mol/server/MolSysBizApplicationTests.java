package com.mol.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("server") // 【关键修改】这会自动加载 application-server.yml
class MolSysBizApplicationTests {
    
    @Test
    void contextLoads() {
    }
    
}
