package com.ing.loanapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ing.loanapi.entity.Loan;

public interface LoanRepository extends JpaRepository<Loan, Long> {
	List<Loan> findByCustomerId(Long customerId);
}
