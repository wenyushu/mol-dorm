package com.mol.sys.biz;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("sys") // 【关键修改】这会自动加载 application-sys.yml
class MolSysBizApplicationTests {
    
    @Test
    void contextLoads() {
    }
    
}
