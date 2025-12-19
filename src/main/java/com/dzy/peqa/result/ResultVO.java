package com.dzy.peqa.result;

import com.dzy.peqa.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResultVO<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(200, "success", data);
    }

    public static ResultVO<?> success() {
        return new ResultVO<>(200, "success", null);
    }

    public static ResultVO<?> fail(int code, String message) {
        return new ResultVO<>(code, message, null);
    }

    public static ResultVO<?> fail(ErrorCode errorCode) {
        return new ResultVO<>(errorCode.getCode(), errorCode.getMessage(), null);
    }
}