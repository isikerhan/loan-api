package com.ing.loanapi.exception;

import java.text.MessageFormat;

import org.springframework.http.HttpStatus;

public class LoanNotFoundException extends BusinessException {

	private static final String MESSAGE_PATTERN = "Loan with id {0} is not found!";
	private static final String CODE = "LOAN_NOT_FOUND";

	public LoanNotFoundException(Long loanId, Throwable cause) {
		super(MessageFormat.format(MESSAGE_PATTERN, String.valueOf(loanId)), cause, CODE, HttpStatus.NOT_FOUND);
	}

	public LoanNotFoundException(Long loanId) {
		this(loanId, null);
	}
}
