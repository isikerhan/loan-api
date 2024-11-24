package com.ing.loanapi.properties;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loan")
public record LoanConfigurationProperties(
		InterestRate interestRate,
		Installment installment,
		short paymentInAdvanceMaxMonths,
		double rewardPerDay,
		double penaltyPerDay,
		BigDecimal minLoanAmount) {

	public record InterestRate(
			Double min,
			Double max) {
	}

	public record Installment(
			Set<Short> values) {
	}
}
