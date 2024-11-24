package com.ing.loanapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanInstallmentDto(
		Long id,
		Long loanId,
		BigDecimal amount,
		BigDecimal paidAmount,
		LocalDate dueDate,
		LocalDate paymentDate,
		Boolean paid) {
}
