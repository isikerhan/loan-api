package com.ing.loanapi.exception;

import java.text.MessageFormat;

import org.springframework.http.HttpStatus;

public class InvalidNumberOfInstallmentsException extends BusinessException {

	private static final String MESSAGE_PATTERN = "Invalid number of installments: {0}";
	private static final String CODE = "INVALID_NUM_OF_INSTALLMENTS";

	public InvalidNumberOfInstallmentsException(int numberOfInstallments, Throwable cause) {
		super(MessageFormat.format(MESSAGE_PATTERN, numberOfInstallments), cause, CODE, HttpStatus.UNPROCESSABLE_ENTITY);
	}

	public InvalidNumberOfInstallmentsException(int numberOfInstallments) {
		this(numberOfInstallments, null);
	}
}
