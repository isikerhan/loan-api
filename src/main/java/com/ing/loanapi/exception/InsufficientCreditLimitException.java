package com.ing.loanapi.exception;

import org.springframework.http.HttpStatus;

public class InsufficientCreditLimitException extends BusinessException {

	private static final String MESSAGE = "Credit limit of the customer is not sufficient to perform this transaction!";
	private static final String CODE = "INSUFFICIENT_CREDIT_LIMIT";

	public InsufficientCreditLimitException(Throwable cause) {
		super(MESSAGE, cause, CODE, HttpStatus.CONFLICT);
	}

	public InsufficientCreditLimitException() {
		this(null);
	}
}
