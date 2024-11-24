package com.ing.loanapi.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.ing.loanapi.dto.ErrorDto;
import com.ing.loanapi.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

	private static final String GENERIC_ERROR_MESSAGE = "An error occurred while processing the request.";

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorDto> handleBusinessException(BusinessException e) {
		final var error = new ErrorDto(
				e.getStatus().value(),
				e.getCode(),
				e.getMessage());

		return ResponseEntity.status(e.getStatus()).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		final var status = HttpStatus.BAD_REQUEST;
		final var error = new ErrorDto(status.value(), status.name(), e.getBindingResult().getFieldError().getDefaultMessage());

		return ResponseEntity.status(status.value()).body(error);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<Void> handleNoResourceFoundException() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	@ExceptionHandler
	public ResponseEntity<ErrorDto> handleGenericException(Throwable throwable) {
		final var status = HttpStatus.INTERNAL_SERVER_ERROR;
		final var error = new ErrorDto(status.value(), status.name(), GENERIC_ERROR_MESSAGE);

		log.error("Unhandled error", throwable);

		return ResponseEntity.status(status.value()).body(error);
	}
}
