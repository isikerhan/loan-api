package com.ing.loanapi.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public abstract class BusinessException extends Exception {

	private final String code;
	private final HttpStatus status;

	public BusinessException(String message, Throwable cause, String code, HttpStatus status) {
		super(message, cause);
		this.code = code;
		this.status = status;
	}

	public BusinessException(String message, String code, HttpStatus status) {
		super(message);
		this.code = code;
		this.status = status;
	}

	public BusinessException(Throwable cause, String code, HttpStatus status) {
		super(cause);
		this.code = code;
		this.status = status;
	}
}
