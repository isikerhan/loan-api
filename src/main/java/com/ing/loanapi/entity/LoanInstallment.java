package com.ing.loanapi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LoanInstallment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, unique = true)
	private Long id;

	@ManyToOne(optional = false)
	@With
	private Loan loan;

	@Column(nullable = false)
	private BigDecimal amount;

	@Column(nullable = false)
	@Builder.Default
	private BigDecimal paidAmount = BigDecimal.ZERO;

	@Column(nullable = false)
	private LocalDate dueDate;

	@Column
	private LocalDate paymentDate;

	@Column
	@Builder.Default
	private Boolean paid = Boolean.FALSE;
}
