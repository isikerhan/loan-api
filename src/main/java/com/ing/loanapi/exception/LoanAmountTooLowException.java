package com.ing.loanapi.exception;

import java.math.BigDecimal;
import java.text.MessageFormat;

import org.springframework.http.HttpStatus;

public class LoanAmountTooLowException extends BusinessException {

	private static final String MESSAGE_PATTERN = "Loan amount cannot be lower than {0}!";
	private static final String CODE = "LOAN_AMOUNT_TOO_LOW";

	public LoanAmountTooLowException(BigDecimal minLoanAmount, Throwable cause) {
		super(MessageFormat.format(MESSAGE_PATTERN, minLoanAmount), cause, CODE, HttpStatus.NOT_FOUND);
	}

	public LoanAmountTooLowException(BigDecimal minLoanAmount) {
		this(minLoanAmount, null);
	}
}
