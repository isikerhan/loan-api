package com.ing.loanapi.exception;

import org.springframework.http.HttpStatus;

public class LoanAlreadyPaidException extends BusinessException {

	private static final String MESSAGE = "The loan is already paid!";
	private static final String CODE = "LOAN_ALREADY_PAID";

	public LoanAlreadyPaidException(Throwable cause) {
		super(MESSAGE, cause, CODE, HttpStatus.CONFLICT);
	}

	public LoanAlreadyPaidException() {
		this(null);
	}
}
