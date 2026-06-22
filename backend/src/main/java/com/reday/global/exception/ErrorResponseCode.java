package com.reday.global.exception;

import org.springframework.http.HttpStatus;

import com.reday.global.response.ResponseCode;

public interface ErrorResponseCode extends ResponseCode {

	HttpStatus getHttpStatus();
}
