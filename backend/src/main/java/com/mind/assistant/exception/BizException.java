package com.mind.assistant.exception;
import com.mind.assistant.common.Result;
import com.mind.assistant.common.ResultCode;


import lombok.Getter;

/**
 * 业务异常：携带业务 code，由 GlobalExceptionHandler 统一转换为 Result
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(ResultCode.FAIL.getCode(), message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }
}
