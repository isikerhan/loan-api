package com.ing.loanapi.exception;

import java.text.MessageFormat;

import org.springframework.http.HttpStatus;

public class CustomerNotFoundException extends BusinessException {

	private static final String MESSAGE_PATTERN = "Customer with id {0} is not found!";
	private static final String CODE = "CUSTOMER_NOT_FOUND";

	public CustomerNotFoundException(Long customerId, Throwable cause) {
		super(MessageFormat.format(MESSAGE_PATTERN, String.valueOf(customerId)), cause, CODE, HttpStatus.NOT_FOUND);
	}

	public CustomerNotFoundException(Long customerId) {
		this(customerId, null);
	}
}
