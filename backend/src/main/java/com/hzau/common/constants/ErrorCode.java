package com.hzau.common.constants;


public enum ErrorCode {
    SUCCESS(0, "成功"),
    ERROR100(100, "失败"),
    ERROR400(400, "参数或方法错误"),
    ERROR404(404, "资源未找到"),
    ERROR403(403, "无权限操作"),
    ERROR401(401, "请登录后重新请求"),
    ERROR500(500, "系统异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
