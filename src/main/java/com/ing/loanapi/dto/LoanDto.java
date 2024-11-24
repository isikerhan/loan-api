package com.ing.loanapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanDto(
		Long id,
		Long customerId,
		BigDecimal loanAmount,
		Short numberOfInstallments,
		Boolean paid,
		LocalDate createDate) {
}
