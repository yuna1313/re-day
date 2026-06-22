package com.reday.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

	private final boolean success;
	private final String code;
	private final String message;

	@JsonInclude(JsonInclude.Include.ALWAYS)
	private final T data;

	public static <T> ApiResponse<T> success(ResponseCode responseCode, T data) {
		return new ApiResponse<>(true, responseCode.getCode(), responseCode.getMessage(), data);
	}

	public static ApiResponse<Void> success(ResponseCode responseCode) {
		return new ApiResponse<>(true, responseCode.getCode(), responseCode.getMessage(), null);
	}

	public static ApiResponse<Void> error(ResponseCode responseCode) {
		return new ApiResponse<>(false, responseCode.getCode(), responseCode.getMessage(), null);
	}

	public static <T> ApiResponse<T> error(ResponseCode responseCode, T data) {
		return new ApiResponse<>(false, responseCode.getCode(), responseCode.getMessage(), data);
	}
}
