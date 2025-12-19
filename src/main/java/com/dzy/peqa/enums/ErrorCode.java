package com.dzy.peqa.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 常用错误类型
    PARAM_ERROR(4001, "参数错误"),
    BUSINESS_ERROR(5001, "业务处理失败"),
    SYSTEM_ERROR(9999, "系统繁忙，请稍后再试");

    private final int code;
    private final String message;
}