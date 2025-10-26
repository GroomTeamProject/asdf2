package io.goorm.team02.core.delivery.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        HttpStatus status
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, HttpStatus.OK);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, data, "생성이 완료되었습니다.", HttpStatus.CREATED);
    }

    public static <T> ApiResponse<T> fail(String msg) {
        return new ApiResponse<>(false, null, msg, HttpStatus.BAD_REQUEST);
    }
}