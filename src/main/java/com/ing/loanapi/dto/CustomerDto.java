package com.ing.loanapi.dto;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record CustomerDto(
		Long id,
		String name,
		String surname,
		BigDecimal creditLimit,
		BigDecimal usedCreditLimit) {
}
