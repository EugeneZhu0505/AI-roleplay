package com.hzau.common;

import com.hzau.common.constants.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.common
 * @className: Result
 * @author: zhuyuchen
 * @description: 统一的响应返回结果类
 * @date: 2025/9/22 下午4:05
 */

@Schema(description = "统一响应结果")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {

    @Schema(description = "响应码", example = "200")
    private Integer code;
    
    @Schema(description = "响应消息", example = "操作成功")
    private String message;
    
    @Schema(description = "响应数据")
    private T data;

    public static <T> Result<T> success(T t, String message) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), message, t);
    }

    public static Result success() {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> success(T t) {
        return success(t, ErrorCode.SUCCESS.getMessage());
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }

}

