package com.ing.loanapi.dto.command;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PayLoanCommand(
		@NotNull(message = "amount is required")
		@Positive(message = "amount must be a positive number")
		BigDecimal paymentAmount
) {
}
