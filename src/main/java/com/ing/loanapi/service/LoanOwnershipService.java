package com.ing.loanapi.service;

import org.springframework.stereotype.Service;

import com.ing.loanapi.dto.LoanDto;
import com.ing.loanapi.exception.LoanNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanOwnershipService {

	private final LoanService loanService;

	public boolean isLoanOwnedBy(Long loanId, String username) {
		final long customerId;
		try {
			customerId = Long.parseLong(username);
		} catch (NumberFormatException e) {
			return false;
		}

		final LoanDto loan;
		try {
			loan = loanService.findLoanById(loanId);
		} catch (LoanNotFoundException e) {
			return false;
		}

		return loan.customerId().equals(customerId);
	}
}
