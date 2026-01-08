package com.mol.dorm.biz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "宿舍测试接口")
@RestController
@RequestMapping("/test")
public class DormTestController {
    
    @Operation(summary = "打招呼测试")
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Dormitory Service!";
    }
}