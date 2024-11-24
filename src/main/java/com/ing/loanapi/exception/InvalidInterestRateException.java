package com.ing.loanapi.exception;

import java.text.MessageFormat;

import org.springframework.http.HttpStatus;

public class InvalidInterestRateException extends BusinessException {

	private static final String MESSAGE_PATTERN = "Invalid interest rate: {0}";
	private static final String CODE = "INVALID_INTEREST_RATE";

	public InvalidInterestRateException(double interestRate, Throwable cause) {
		super(MessageFormat.format(MESSAGE_PATTERN, interestRate), cause, CODE, HttpStatus.CONFLICT);
	}

	public InvalidInterestRateException(double interestRate) {
		this(interestRate, null);
	}
}
