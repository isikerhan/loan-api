package com.ing.loanapi.dto;

import java.math.BigDecimal;

public record LoanPaymentResultDto(
		int numberOfInstallmentsPaid,
		BigDecimal totalPaidAmount,
		boolean allInstallmentsOfLoanPaid
) {
}
