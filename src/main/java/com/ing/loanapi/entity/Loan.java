package com.ing.loanapi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Loan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, unique = true)
	private Long id;

	@ManyToOne(optional = false)
	private Customer customer;

	@Column(nullable = false)
	private BigDecimal loanAmount;

	@Column(nullable = false)
	private Short numberOfInstallments;

	@Column(nullable = false)
	@Builder.Default
	private Boolean paid = Boolean.FALSE;

	@Column(nullable = false)
	private LocalDate createDate;

	@OneToMany(mappedBy = "loan", cascade = CascadeType.PERSIST)
	private List<LoanInstallment> installments;

	public void setInstallments(List<LoanInstallment> installments) {
		this.installments = installments.stream()
				.map(installment -> installment.withLoan(this))
				.toList();
	}
}
