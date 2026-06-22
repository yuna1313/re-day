package com.reday.global.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.reday.global.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 서비스 계층에서 명시적으로 던진 비즈니스 예외를 공통 오류 응답으로 변환합니다.
	 *
	 * @param exception 비즈니스 규칙 위반 또는 도메인 처리 실패를 나타내는 예외
	 * @return ErrorCode에 정의된 HTTP 상태와 공통 오류 응답
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
		ErrorResponseCode errorCode = exception.getErrorCode();

		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.error(errorCode));
	}

	/**
	 * Bean Validation 검증 실패 예외를 필드별 오류 목록이 포함된 공통 오류 응답으로 변환합니다.
	 *
	 * @param exception @Valid 또는 @Validated 요청 객체 검증 실패 예외
	 * @return 입력값 오류 코드와 필드별 검증 실패 정보
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<List<FieldErrorResponse>>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException exception
	) {
		List<FieldErrorResponse> errors = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
			.toList();

		return ResponseEntity
			.status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
			.body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE, errors));
	}

	/**
	 * JSON 형식 오류처럼 요청 본문을 읽거나 변환할 수 없는 예외를 공통 오류 응답으로 변환합니다.
	 *
	 * @param exception 요청 본문 파싱 또는 HTTP 메시지 변환 실패 예외
	 * @return 요청 본문 오류 코드가 포함된 공통 오류 응답
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException exception
	) {
		return ResponseEntity
			.status(ErrorCode.INVALID_REQUEST_BODY.getHttpStatus())
			.body(ApiResponse.error(ErrorCode.INVALID_REQUEST_BODY));
	}

	/**
	 * 별도로 처리하지 않은 예외를 서버 내부 오류 응답으로 변환합니다.
	 *
	 * @param exception 예상하지 못한 서버 예외
	 * @return 서버 내부 오류 코드가 포함된 공통 오류 응답
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
		return ResponseEntity
			.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
			.body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
	}

	public record FieldErrorResponse(
		String field,
		String message
	) {
	}
}
