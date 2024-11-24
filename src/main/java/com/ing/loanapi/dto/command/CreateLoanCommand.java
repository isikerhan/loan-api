package com.ing.loanapi.dto.command;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateLoanCommand(
		@NotNull(message = "customerId is required")
		Long customerId,

		@NotNull(message = "amount is required")
		@Positive(message = "amount must be a positive number")
		BigDecimal amount,

		@NotNull(message = "interestRate is required")
		@Positive(message = "interestRate must be a positive decimal")
		Double interestRate,

		@NotNull(message = "numberOfInstallments is required")
		@Positive(message = "numberOfInstallments must be a positive integer")
		Short numberOfInstallments
) {
}
