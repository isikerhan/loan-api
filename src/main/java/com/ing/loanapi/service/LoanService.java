package com.ing.loanapi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.ing.loanapi.dto.LoanDto;
import com.ing.loanapi.dto.LoanInstallmentDto;
import com.ing.loanapi.dto.LoanPaymentResultDto;
import com.ing.loanapi.dto.command.CreateLoanCommand;
import com.ing.loanapi.dto.command.PayLoanCommand;
import com.ing.loanapi.entity.Loan;
import com.ing.loanapi.entity.LoanInstallment;
import com.ing.loanapi.exception.BusinessException;
import com.ing.loanapi.exception.CustomerNotFoundException;
import com.ing.loanapi.exception.InsufficientCreditLimitException;
import com.ing.loanapi.exception.InvalidInterestRateException;
import com.ing.loanapi.exception.InvalidNumberOfInstallmentsException;
import com.ing.loanapi.exception.LoanAlreadyPaidException;
import com.ing.loanapi.exception.LoanAmountTooLowException;
import com.ing.loanapi.exception.LoanNotFoundException;
import com.ing.loanapi.mapper.CustomerMapper;
import com.ing.loanapi.mapper.LoanInstallmentMapper;
import com.ing.loanapi.mapper.LoanMapper;
import com.ing.loanapi.properties.LoanConfigurationProperties;
import com.ing.loanapi.repository.LoanRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@EnableConfigurationProperties(LoanConfigurationProperties.class)
@RequiredArgsConstructor
@Transactional(rollbackFor = BusinessException.class)
public class LoanService {

	private final CustomerService customerService;
	private final LoanRepository loanRepository;
	private final LoanMapper loanMapper;
	private final CustomerMapper customerMapper;
	private final LoanInstallmentMapper loanInstallmentMapper;
	private final LoanConfigurationProperties loanConfigurationProperties;

	@Transactional(readOnly = true)
	public List<LoanDto> findLoansOfCustomer(Long customerId) throws CustomerNotFoundException {
		final var customer = customerService.findCustomerById(customerId);
		final var loans = loanRepository.findByCustomerId(customer.id());
		return loans.stream()
				.map(loanMapper::mapToLoanDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<LoanInstallmentDto> findInstallmentsOfLoan(Long loanId) throws LoanNotFoundException {
		final var loan = loanRepository.findById(loanId)
				.orElseThrow(() -> new LoanNotFoundException(loanId));

		final var installments = loan.getInstallments();
		return installments.stream()
				.map(loanInstallmentMapper::mapToLoanInstallmentDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public LoanDto findLoanById(Long loanId) throws LoanNotFoundException {
		final var loan = loanRepository.findById(loanId)
				.orElseThrow(() -> new LoanNotFoundException(loanId));
		return loanMapper.mapToLoanDto(loan);
	}

	@Validated
	public LoanDto createLoan(@Valid CreateLoanCommand command) throws CustomerNotFoundException,
			InvalidNumberOfInstallmentsException, InsufficientCreditLimitException, InvalidInterestRateException, LoanAmountTooLowException {
		if (command.amount().compareTo(loanConfigurationProperties.minLoanAmount()) < 0) {
			throw new LoanAmountTooLowException(loanConfigurationProperties.minLoanAmount());
		}

		final var loanDate = LocalDate.now();

		final var customerId = command.customerId();
		final var amount = command.amount();
		final var interestRate = command.interestRate();
		final var numberOfInstallments = command.numberOfInstallments();

		final var allowedNumberOfInstallments = loanConfigurationProperties.installment().values();
		final var minInterestRate = loanConfigurationProperties.interestRate().min();
		final var maxInterestRate = loanConfigurationProperties.interestRate().max();

		if (!allowedNumberOfInstallments.contains(numberOfInstallments)) {
			throw new InvalidNumberOfInstallmentsException(numberOfInstallments);
		}

		if (interestRate < minInterestRate || interestRate > maxInterestRate) {
			throw new InvalidInterestRateException(interestRate);
		}

		final var totalPaymentAmount = amount.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(interestRate)));
		final var customer = customerService.useCreditLimit(customerId, totalPaymentAmount);

		final var paymentAmountPerInstallment = totalPaymentAmount.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.HALF_UP);

		final var loan = Loan.builder()
				.customer(customerMapper.mapToCustomer(customer))
				.loanAmount(amount)
				.numberOfInstallments(numberOfInstallments)
				.createDate(loanDate)
				.build();

		final var installments = this.buildInstallments(loanDate, paymentAmountPerInstallment, totalPaymentAmount, numberOfInstallments);
		loan.setInstallments(installments);

		final var savedLoan = loanRepository.save(loan);
		return loanMapper.mapToLoanDto(savedLoan);
	}

	private List<LoanInstallment> buildInstallments(LocalDate loanDate, BigDecimal paymentAmountPerInstallment, BigDecimal totalPaymentAmount, Short numberOfInstallments) {
		final var installments = new ArrayList<LoanInstallment>();

		var previousPaymentDate = loanDate;

		for (int i = 0; i < numberOfInstallments; i++) {
			final var paymentAmount = i == numberOfInstallments - 1
					? totalPaymentAmount.subtract(paymentAmountPerInstallment.multiply(BigDecimal.valueOf(numberOfInstallments - 1)))
					: paymentAmountPerInstallment; // avoid under or over payment due to rounding
			final var paymentDate = previousPaymentDate.with(TemporalAdjusters.firstDayOfNextMonth());

			final var installment = LoanInstallment.builder()
					.amount(paymentAmount)
					.dueDate(paymentDate)
					.build();

			installments.add(installment);
			previousPaymentDate = paymentDate;
		}

		return Collections.unmodifiableList(installments);
	}

	@Validated
	public LoanPaymentResultDto payLoan(Long loanId, @Valid PayLoanCommand command) throws LoanNotFoundException, LoanAlreadyPaidException {
		final var paymentAmount = command.paymentAmount();
		final var paymentDate = LocalDate.now();

		final var loan = loanRepository.findById(loanId)
				.orElseThrow(() -> new LoanNotFoundException(loanId));

		if (Boolean.TRUE.equals(loan.getPaid())) {
			throw new LoanAlreadyPaidException();
		}

		final var unpaidInstallments = loan.getInstallments().stream()
				.filter(installment -> Boolean.FALSE.equals(installment.getPaid()))
				.toList();

		final var installmentsToPay = findInstallmentsToPay(paymentDate, unpaidInstallments, paymentAmount);
		installmentsToPay.forEach(installment -> {
			installment.setPaid(Boolean.TRUE);
			installment.setPaymentDate(paymentDate);
		});

		final var allInstallmentsOfLoanPaid = installmentsToPay.size() == unpaidInstallments.size();
		if (allInstallmentsOfLoanPaid) {
			loan.setPaid(Boolean.TRUE);
		}

		final var savedLoan = loanRepository.save(loan);
		final var paidAmount = installmentsToPay.stream()
				.map(LoanInstallment::getPaidAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
			// actual debt amount without rewards / penalties
			final var paidDebtAmount = installmentsToPay.stream()
					.map(LoanInstallment::getAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			try {
				customerService.returnCreditLimit(savedLoan.getCustomer().getId(), paidDebtAmount);
			} catch (CustomerNotFoundException e) {
				// this should not happen
				throw new RuntimeException("Customer not found! Possibly a data inconsistency!", e);
			}
		}

		return new LoanPaymentResultDto((short) installmentsToPay.size(), paidAmount, allInstallmentsOfLoanPaid);
	}

	private List<LoanInstallment> findInstallmentsToPay(LocalDate paymentDate, List<LoanInstallment> unpaidInstallments, BigDecimal paymentAmount) {
		final var firstDayOfCurrentMonth = paymentDate.withDayOfMonth(1);
		final var installmentsToPay = new ArrayList<LoanInstallment>();
		// make sure earliest installments come first
		final var sortedInstallments = unpaidInstallments.stream()
				.sorted(Comparator.comparing(LoanInstallment::getDueDate))
				.toList();
		var remainingAmount = paymentAmount;

		for (var installment : sortedInstallments) {
			final var dayDifference = ChronoUnit.DAYS.between(paymentDate, installment.getDueDate());
			final var installmentAmount = installment.getAmount();
			final BigDecimal installmentPaymentAmount;

			if (dayDifference > 0) {
				final var reward = BigDecimal.valueOf(loanConfigurationProperties.rewardPerDay())
						.multiply(BigDecimal.valueOf(dayDifference));
				installmentPaymentAmount = installmentAmount.subtract(reward).setScale(2, RoundingMode.UP);
			}
			else if (dayDifference < 0) {
				final var penalty = BigDecimal.valueOf(loanConfigurationProperties.penaltyPerDay())
						.multiply(BigDecimal.valueOf(dayDifference).abs());
				installmentPaymentAmount = installmentAmount.add(penalty).setScale(2, RoundingMode.DOWN);
			}
			else {
				installmentPaymentAmount = installmentAmount;
			}

			final var monthDiff = ChronoUnit.MONTHS.between(firstDayOfCurrentMonth, installment.getDueDate());
			if (remainingAmount.compareTo(installmentPaymentAmount) < 0
					|| monthDiff > loanConfigurationProperties.paymentInAdvanceMaxMonths()) {
				break;
			}

			remainingAmount = remainingAmount.subtract(installmentPaymentAmount);
			installment.setPaidAmount(installmentPaymentAmount);
			installmentsToPay.add(installment);
		}

		return Collections.unmodifiableList(installmentsToPay);
	}
}
