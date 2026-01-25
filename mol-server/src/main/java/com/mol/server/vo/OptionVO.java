package com.mol.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptionVO {
    private Long value;   // 存 ID
    private String label; // 展示名称
}