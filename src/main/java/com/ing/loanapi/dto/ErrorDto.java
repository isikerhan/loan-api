package com.ing.loanapi.dto;

public record ErrorDto(
		int status,
		String code,
		String message) {
}
