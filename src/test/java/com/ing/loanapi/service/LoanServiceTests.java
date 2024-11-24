package com.ing.loanapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ing.loanapi.dto.CustomerDto;
import com.ing.loanapi.dto.LoanDto;
import com.ing.loanapi.dto.LoanInstallmentDto;
import com.ing.loanapi.dto.LoanPaymentResultDto;
import com.ing.loanapi.dto.command.CreateLoanCommand;
import com.ing.loanapi.dto.command.PayLoanCommand;
import com.ing.loanapi.entity.Customer;
import com.ing.loanapi.entity.Loan;
import com.ing.loanapi.entity.LoanInstallment;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanService Tests")
public class LoanServiceTests {

	@InjectMocks
	private LoanService loanService;

	@Mock
	private CustomerService customerService;

	@Mock
	private LoanRepository loanRepository;

	@Mock
	private LoanMapper loanMapper;

	@Mock
	private CustomerMapper customerMapper;

	@Mock
	private LoanInstallmentMapper loanInstallmentMapper;

	@Mock
	private LoanConfigurationProperties loanConfigurationProperties;

	@Nested
	@DisplayName("findLoansOfCustomer Tests")
	class FindLoansOfCustomerTests {

		@Test
		@DisplayName("Given customer with no loans, findLoansOfCustomer should return an empty list")
		void givenCustomerWithNoLoans_findLoansOfCustomerShouldReturnEmptyList() throws CustomerNotFoundException {
			final var customerId = 100001L;
			final var name = "John";
			final var surname = "Doe";
			final var creditLimit = new BigDecimal("100000");
			final var usedCreditLimit = BigDecimal.ZERO;

			final var customerDto = new CustomerDto(
					customerId, name, surname, creditLimit, usedCreditLimit);

			when(customerService.findCustomerById(customerId))
					.thenReturn(customerDto);
			when(loanRepository.findByCustomerId(customerId))
					.thenReturn(Collections.emptyList());

			final var result = loanService.findLoansOfCustomer(customerId);

			// assertions
			verify(customerService, times(1)).findCustomerById(customerId);
			verify(loanRepository, times(1)).findByCustomerId(customerId);

			assertEquals(Collections.emptyList(), result);
		}

		@Test
		@DisplayName("Given customer with loans, findLoansOfCustomer should return the list of loans")
		void givenCustomerWithLoans_findLoansOfCustomer_shouldReturnListOfLoans() throws CustomerNotFoundException {
			final var customerId = 100002L;
			final var name = "Jane";
			final var surname = "Doe";
			final var creditLimit = new BigDecimal("100000");
			final var usedCreditLimit = BigDecimal.ZERO;

			final var customerDto = new CustomerDto(
					customerId, name, surname, creditLimit, usedCreditLimit);

			when(customerService.findCustomerById(customerId))
					.thenReturn(customerDto);

			final var customer = new Customer();
			customer.setId(customerId);

			// loan 1
			final var loan1Id = 1L;
			final var loan1Amount = new BigDecimal("10000");
			final var loan1NumberOfInstallments = (short) 12;
			final var loan1Paid = false;
			final var loan1CreationDate = LocalDate.of(2024, 1, 1);

			final var loan1 = new Loan();
			loan1.setId(1L);
			loan1.setCustomer(customer);
			loan1.setLoanAmount(loan1Amount);
			loan1.setNumberOfInstallments(loan1NumberOfInstallments);
			loan1.setPaid(loan1Paid);
			loan1.setCreateDate(loan1CreationDate);

			// loan 2
			final var loan2Id = 2L;
			final var loan2Amount = new BigDecimal("20000");
			final var loan2NumberOfInstallments = (short) 24;
			final var loan2Paid = false;
			final var loan2CreationDate = LocalDate.of(2024, 3, 1);

			final var loan2 = new Loan();
			loan2.setId(2L);
			loan2.setCustomer(customer);
			loan2.setLoanAmount(loan2Amount);
			loan2.setNumberOfInstallments(loan2NumberOfInstallments);
			loan2.setPaid(loan2Paid);
			loan2.setCreateDate(loan2CreationDate);

			when(loanRepository.findByCustomerId(customerId))
					.thenReturn(List.of(loan1, loan2));

			final var loanDto1 = new LoanDto(
					loan1Id, customerId, loan1Amount, loan1NumberOfInstallments, loan1Paid, loan1CreationDate);
			final var loanDto2 = new LoanDto(
					loan2Id, customerId, loan2Amount, loan2NumberOfInstallments, loan2Paid, loan2CreationDate);

			when(loanMapper.mapToLoanDto(same(loan1)))
					.thenReturn(loanDto1);
			when(loanMapper.mapToLoanDto(same(loan2)))
					.thenReturn(loanDto2);

			final var result = loanService.findLoansOfCustomer(customerId);

			// assertions
			verify(customerService, times(1)).findCustomerById(customerId);
			verify(loanRepository, times(1)).findByCustomerId(customerId);
			verify(loanMapper, times(1)).mapToLoanDto(same(loan1));
			verify(loanMapper, times(1)).mapToLoanDto(same(loan2));

			assertEquals(List.of(loanDto1, loanDto2), result);
		}
	}

	@Nested
	@DisplayName("findInstallmentsOfLoan Tests")
	class FindInstallmentsOfLoanTests {

		@Test
		@DisplayName("Given an existing loan, findInstallmentsOfLoan should return the list of installments")
		public void givenExistingLoan_findInstallmentsOfLoan_shouldReturnListOfInstallments() throws LoanNotFoundException {
			final var loanId = 1L;
			final var loanAmount = new BigDecimal("10000");
			final var numberOfInstallments = (short) 3;
			final var paid = false;
			final var createDate = LocalDate.of(2024, 1, 1);

			final var loan = new Loan();
			loan.setId(loanId);
			loan.setLoanAmount(loanAmount);
			loan.setNumberOfInstallments(numberOfInstallments);
			loan.setPaid(paid);
			loan.setCreateDate(createDate);

			final var installment1Id = 1L;
			final var installment1Amount = new BigDecimal("5000");
			final var installment1DueDate = LocalDate.of(2024, 2, 1);
			final var installment1Paid = true;
			final var installment1PaidAmount = new BigDecimal("5000");
			final var installment1PaymentDate = LocalDate.of(2024, 2, 1);

			final var installment2Id = 2L;
			final var installment2Amount = new BigDecimal("5000");
			final var installment2DueDate = LocalDate.of(2024, 3, 1);
			final var installment2Paid = false;

			final var installment3Id = 3L;
			final var installment3Amount = new BigDecimal("5000");
			final var installment3DueDate = LocalDate.of(2024, 4, 1);
			final var installment3Paid = false;

			final var installment1 = new LoanInstallment();
			installment1.setId(installment1Id);
			installment1.setLoan(loan);
			installment1.setAmount(installment1Amount);
			installment1.setDueDate(installment1DueDate);
			installment1.setPaid(installment1Paid);
			installment1.setPaidAmount(installment1PaidAmount);
			installment1.setPaymentDate(installment1PaymentDate);

			final var installment2 = new LoanInstallment();
			installment2.setId(installment2Id);
			installment2.setLoan(loan);
			installment2.setAmount(installment2Amount);
			installment2.setDueDate(installment2DueDate);
			installment2.setPaid(installment2Paid);

			final var installment3 = new LoanInstallment();
			installment3.setId(installment3Id);
			installment3.setLoan(loan);
			installment3.setAmount(installment3Amount);
			installment3.setDueDate(installment3DueDate);
			installment3.setPaid(installment3Paid);

			loan.setInstallments(List.of(installment1, installment2, installment3));

			when(loanRepository.findById(loanId))
					.thenReturn(Optional.of(loan));

			final var installmentDto1 = new LoanInstallmentDto(
					installment1Id, loanId, installment1Amount, installment1PaidAmount, installment1DueDate, installment1PaymentDate, installment1Paid);
			final var installmentDto2 = new LoanInstallmentDto(
					installment2Id, loanId, installment2Amount, null, installment2DueDate, null, installment2Paid);
			final var installmentDto3 = new LoanInstallmentDto(
					installment3Id, loanId, installment3Amount, null, installment3DueDate, null, installment2Paid);

			when(loanInstallmentMapper.mapToLoanInstallmentDto(same(installment1)))
					.thenReturn(installmentDto1);
			when(loanInstallmentMapper.mapToLoanInstallmentDto(same(installment2)))
					.thenReturn(installmentDto2);
			when(loanInstallmentMapper.mapToLoanInstallmentDto(same(installment3)))
					.thenReturn(installmentDto3);

			final var result = loanService.findInstallmentsOfLoan(loanId);

			// assertions
			verify(loanRepository, times(1)).findById(loanId);
			verify(loanInstallmentMapper, times(1)).mapToLoanInstallmentDto(same(installment1));
			verify(loanInstallmentMapper, times(1)).mapToLoanInstallmentDto(same(installment2));
			verify(loanInstallmentMapper, times(1)).mapToLoanInstallmentDto(same(installment3));

			assertEquals(List.of(installmentDto1, installmentDto2, installmentDto3), result);
		}

		@Test
		@DisplayName("Given non-existent loan, findInstallmentsOfLoan should throw LoanNotFoundException")
		public void givenNonExistentLoan_findInstallmentsOfLoan_shouldThrow() {
			final var loanId = 3L;

			when(loanRepository.findById(loanId))
					.thenReturn(Optional.empty());

			// assertions
			assertThrows(LoanNotFoundException.class, () -> loanService.findInstallmentsOfLoan(loanId));

			verify(loanRepository, times(1)).findById(loanId);
			verify(loanInstallmentMapper, never()).mapToLoanInstallmentDto(any());
		}
	}

	@Nested
	@DisplayName("createLoan Tests")
	class CreateLoanTests {

		@Test
		@DisplayName("Given valid create loan command, createLoan should create a loan and return it")
		void givenValidCreateLoanCommand_createLoan_shouldCreateLoanAndReturn() throws InsufficientCreditLimitException, CustomerNotFoundException, InvalidNumberOfInstallmentsException, InvalidInterestRateException, LoanAmountTooLowException {

			final var customerId = 100001L;
			final var amount = new BigDecimal("100000");
			final var interestRate = 0.2;
			final var numberOfInstallments = (short) 12;

			final var loanDate = LocalDate.now();
			final var totalPaymentAmount = new BigDecimal("120000.0");

			final var name = "Jane";
			final var surname = "Doe";
			final var creditLimit = new BigDecimal("500000");
			final var usedCreditLimit = new BigDecimal("220000");

			final var customerDto = new CustomerDto(customerId, name, surname, creditLimit, usedCreditLimit);
			final var customer = new Customer();
			customer.setId(customerId);
			customer.setName(name);
			customer.setSurname(surname);
			customer.setCreditLimit(creditLimit);
			customer.setUsedCreditLimit(usedCreditLimit);

			final var loanId = 1L;

			when(loanConfigurationProperties.minLoanAmount())
					.thenReturn(new BigDecimal("1000"));
			when(loanConfigurationProperties.interestRate())
					.thenReturn(new LoanConfigurationProperties.InterestRate(0.1, 0.5));
			when(loanConfigurationProperties.installment())
					.thenReturn(new LoanConfigurationProperties.Installment(Set.of((short) 6, (short) 9, (short) 12, (short) 24)));

			when(customerService.useCreditLimit(customerId, totalPaymentAmount))
					.thenReturn(customerDto);
			when(loanRepository.save(any(Loan.class)))
					.thenAnswer(invocation -> {
						final var loan = invocation.getArgument(0, Loan.class);
						loan.setId(loanId);
						return loan;
					});
			when(customerMapper.mapToCustomer(eq(customerDto)))
					.thenReturn(customer);
			when(loanMapper.mapToLoanDto(any(Loan.class)))
					.thenAnswer(invocation -> {
						final var loan = invocation.getArgument(0, Loan.class);
						return new LoanDto(
								loan.getId(), loan.getCustomer().getId(), loan.getLoanAmount(), loan.getNumberOfInstallments(), false, loan.getCreateDate());
					});

			final var result = loanService.createLoan(new CreateLoanCommand(customerId, amount, interestRate, numberOfInstallments));

			// assertions
			final var loanCaptor = ArgumentCaptor.forClass(Loan.class);
			verify(customerService, times(1)).useCreditLimit(eq(customerId), eq(totalPaymentAmount));
			verify(loanRepository, times(1)).save(loanCaptor.capture());
			verify(loanMapper, times(1)).mapToLoanDto(same(loanCaptor.getValue()));

			assertEquals(loanId, result.id());
			assertEquals(customerId, result.customerId());
			assertEquals(amount, result.loanAmount());
			assertEquals(numberOfInstallments, result.numberOfInstallments());
			assertEquals(false, result.paid());
			assertEquals(loanDate, result.createDate());

			final var totalInstallmentAmount = loanCaptor.getValue().getInstallments()
					.stream().map(LoanInstallment::getAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			assertEquals(numberOfInstallments, loanCaptor.getValue().getInstallments().size());
			assertEquals(0, totalPaymentAmount.compareTo(totalInstallmentAmount));
		}
	}

	@Nested
	@DisplayName("payLoan Tests")
	class PayLoanTests {

		@Test
		@DisplayName("Given enough paymentAmount and loan, payLoan should pay the earliest installments and return payment information")
		void givenEnoughPaymentAmountAndLoan_payLoan_shouldPayEarliestInstallmentsAndReturnPaymentInfo() throws LoanAlreadyPaidException, LoanNotFoundException, CustomerNotFoundException {
			final var paymentAmount = new BigDecimal("12000");
			final var today = LocalDate.of(2024, 3, 11);

			final var customerId = 100002L;
			final var name = "Jane";
			final var surname = "Doe";
			final var creditLimit = new BigDecimal("500000");
			final var usedCreditLimit = new BigDecimal("220000");

			final var customer = new Customer();
			customer.setId(customerId);
			customer.setName(name);
			customer.setSurname(surname);
			customer.setCreditLimit(creditLimit);
			customer.setUsedCreditLimit(usedCreditLimit);

			final var loanId = 1L;
			final var loanAmount = new BigDecimal("20000");
			final var numberOfInstallments = (short) 6;
			final var paid = false;
			final var createDate = LocalDate.of(2024, 1, 1);

			final var loan = new Loan();
			loan.setId(loanId);
			loan.setLoanAmount(loanAmount);
			loan.setNumberOfInstallments(numberOfInstallments);
			loan.setPaid(paid);
			loan.setCreateDate(createDate);
			loan.setCustomer(customer);

			final var installment1Id = 1L;
			final var installment1Amount = new BigDecimal("5000");
			final var installment1DueDate = LocalDate.of(2024, 2, 1);
			final var installment1Paid = true;
			final var installment1PaidAmount = new BigDecimal("5000");
			final var installment1PaymentDate = LocalDate.of(2024, 2, 1);

			final var installment2Id = 2L;
			final var installment2Amount = new BigDecimal("5000");
			final var installment2DueDate = LocalDate.of(2024, 3, 1);
			final var installment2Paid = false;

			final var installment3Id = 3L;
			final var installment3Amount = new BigDecimal("5000");
			final var installment3DueDate = LocalDate.of(2024, 4, 1);
			final var installment3Paid = false;

			final var installment4Id = 4L;
			final var installment4Amount = new BigDecimal("5000");
			final var installment4DueDate = LocalDate.of(2024, 5, 1);
			final var installment4Paid = false;

			final var installment5Id = 5L;
			final var installment5Amount = new BigDecimal("5000");
			final var installment5DueDate = LocalDate.of(2024, 6, 1);
			final var installment5Paid = false;

			final var installment6Id = 6L;
			final var installment6Amount = new BigDecimal("5000");
			final var installment6DueDate = LocalDate.of(2024, 7, 1);
			final var installment6Paid = false;

			final var installment1 = new LoanInstallment();
			installment1.setId(installment1Id);
			installment1.setLoan(loan);
			installment1.setAmount(installment1Amount);
			installment1.setDueDate(installment1DueDate);
			installment1.setPaid(installment1Paid);
			installment1.setPaidAmount(installment1PaidAmount);
			installment1.setPaymentDate(installment1PaymentDate);

			final var installment2 = new LoanInstallment();
			installment2.setId(installment2Id);
			installment2.setLoan(loan);
			installment2.setAmount(installment2Amount);
			installment2.setDueDate(installment2DueDate);
			installment2.setPaid(installment2Paid);

			final var installment3 = new LoanInstallment();
			installment3.setId(installment3Id);
			installment3.setLoan(loan);
			installment3.setAmount(installment3Amount);
			installment3.setDueDate(installment3DueDate);
			installment3.setPaid(installment3Paid);

			final var installment4 = new LoanInstallment();
			installment4.setId(installment4Id);
			installment4.setLoan(loan);
			installment4.setAmount(installment4Amount);
			installment4.setDueDate(installment4DueDate);
			installment4.setPaid(installment4Paid);

			final var installment5 = new LoanInstallment();
			installment5.setId(installment5Id);
			installment5.setLoan(loan);
			installment5.setAmount(installment5Amount);
			installment5.setDueDate(installment5DueDate);
			installment5.setPaid(installment5Paid);

			final var installment6 = new LoanInstallment();
			installment6.setId(installment6Id);
			installment6.setLoan(loan);
			installment6.setAmount(installment6Amount);
			installment6.setDueDate(installment6DueDate);
			installment6.setPaid(installment6Paid);

			loan.setInstallments(List.of(installment1, installment2, installment3, installment4, installment5, installment6));

			when(loanConfigurationProperties.paymentInAdvanceMaxMonths())
					.thenReturn((short) 3);
			when(loanConfigurationProperties.rewardPerDay())
					.thenReturn(0.001);
			when(loanConfigurationProperties.penaltyPerDay())
					.thenReturn(0.001);

			when(loanRepository.findById(loanId))
					.thenReturn(Optional.of(loan));
			when(loanRepository.save(same(loan)))
					.thenAnswer(invocation -> invocation.getArgument(0, Loan.class));

			final LoanPaymentResultDto result;

			try (final var mock = mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
				mock.when(LocalDate::now).thenReturn(today);
				result = loanService.payLoan(loanId, new PayLoanCommand(paymentAmount));
			}

			final var totalPaidDebt = new BigDecimal("10000");

			// assertions
			final var loanCaptor = ArgumentCaptor.forClass(Loan.class);
			verify(loanRepository, times(1)).findById(loanId);
			verify(loanRepository, times(1)).save(loanCaptor.capture());
			verify(customerService, times(1)).returnCreditLimit(customerId, totalPaidDebt);

			final var installments = loan.getInstallments();
			assertEquals(6, installments.size());

			assertTrue(installment1.getPaid());
			assertEquals(installment1.getPaidAmount(), installment1PaidAmount);
			assertEquals(installment1.getPaymentDate(), installment1PaymentDate);

			assertTrue(installment2.getPaid());
			assertEquals(installment2.getPaidAmount(), new BigDecimal("5000.01"));
			assertEquals(installment2.getPaymentDate(), today);

			assertTrue(installment3.getPaid());
			assertEquals(installment3.getPaidAmount(), new BigDecimal("4999.98"));
			assertEquals(installment3.getPaymentDate(), today);

			assertFalse(installment4.getPaid());
			assertEquals(installment4.getPaidAmount(), BigDecimal.ZERO);
			assertNull(installment4.getPaymentDate());

			assertFalse(installment5.getPaid());
			assertEquals(installment5.getPaidAmount(), BigDecimal.ZERO);
			assertNull(installment5.getPaymentDate());

			assertFalse(installment6.getPaid());
			assertEquals(installment6.getPaidAmount(), BigDecimal.ZERO);
			assertNull(installment6.getPaymentDate());

			assertEquals(2, result.numberOfInstallmentsPaid());
			assertEquals(new BigDecimal("9999.99"), result.totalPaidAmount());
			assertFalse(result.allInstallmentsOfLoanPaid());
		}

		@Test
		@DisplayName("Given more than enough payment amount to pay all the installments, payLoan should only pay allowed number of the earliest installments and return payment information")
		void givenMoreThanEnoughPaymentAmountToFullPayLoan_payLoan_shouldOnlyPayAllowedEarliestInstallmentsAndReturnPaymentInfo() throws LoanAlreadyPaidException, LoanNotFoundException, CustomerNotFoundException {
			final var paymentAmount = new BigDecimal("50000");
			final var today = LocalDate.of(2024, 3, 11);

			final var customerId = 100002L;
			final var name = "Jane";
			final var surname = "Doe";
			final var creditLimit = new BigDecimal("500000");
			final var usedCreditLimit = new BigDecimal("220000");

			final var customer = new Customer();
			customer.setId(customerId);
			customer.setName(name);
			customer.setSurname(surname);
			customer.setCreditLimit(creditLimit);
			customer.setUsedCreditLimit(usedCreditLimit);

			final var loanId = 1L;
			final var loanAmount = new BigDecimal("20000");
			final var numberOfInstallments = (short) 6;
			final var paid = false;
			final var createDate = LocalDate.of(2024, 1, 1);

			final var loan = new Loan();
			loan.setId(loanId);
			loan.setLoanAmount(loanAmount);
			loan.setNumberOfInstallments(numberOfInstallments);
			loan.setPaid(paid);
			loan.setCreateDate(createDate);
			loan.setCustomer(customer);

			final var installment1Id = 1L;
			final var installment1Amount = new BigDecimal("5000");
			final var installment1DueDate = LocalDate.of(2024, 2, 1);
			final var installment1Paid = true;
			final var installment1PaidAmount = new BigDecimal("5000");
			final var installment1PaymentDate = LocalDate.of(2024, 2, 1);

			final var installment2Id = 2L;
			final var installment2Amount = new BigDecimal("5000");
			final var installment2DueDate = LocalDate.of(2024, 3, 1);
			final var installment2Paid = false;

			final var installment3Id = 3L;
			final var installment3Amount = new BigDecimal("5000");
			final var installment3DueDate = LocalDate.of(2024, 4, 1);
			final var installment3Paid = false;

			final var installment4Id = 4L;
			final var installment4Amount = new BigDecimal("5000");
			final var installment4DueDate = LocalDate.of(2024, 5, 1);
			final var installment4Paid = false;

			final var installment5Id = 5L;
			final var installment5Amount = new BigDecimal("5000");
			final var installment5DueDate = LocalDate.of(2024, 6, 1);
			final var installment5Paid = false;

			final var installment6Id = 6L;
			final var installment6Amount = new BigDecimal("5000");
			final var installment6DueDate = LocalDate.of(2024, 7, 1);
			final var installment6Paid = false;

			final var installment1 = new LoanInstallment();
			installment1.setId(installment1Id);
			installment1.setLoan(loan);
			installment1.setAmount(installment1Amount);
			installment1.setDueDate(installment1DueDate);
			installment1.setPaid(installment1Paid);
			installment1.setPaidAmount(installment1PaidAmount);
			installment1.setPaymentDate(installment1PaymentDate);

			final var installment2 = new LoanInstallment();
			installment2.setId(installment2Id);
			installment2.setLoan(loan);
			installment2.setAmount(installment2Amount);
			installment2.setDueDate(installment2DueDate);
			installment2.setPaid(installment2Paid);

			final var installment3 = new LoanInstallment();
			installment3.setId(installment3Id);
			installment3.setLoan(loan);
			installment3.setAmount(installment3Amount);
			installment3.setDueDate(installment3DueDate);
			installment3.setPaid(installment3Paid);

			final var installment4 = new LoanInstallment();
			installment4.setId(installment4Id);
			installment4.setLoan(loan);
			installment4.setAmount(installment4Amount);
			installment4.setDueDate(installment4DueDate);
			installment4.setPaid(installment4Paid);

			final var installment5 = new LoanInstallment();
			installment5.setId(installment5Id);
			installment5.setLoan(loan);
			installment5.setAmount(installment5Amount);
			installment5.setDueDate(installment5DueDate);
			installment5.setPaid(installment5Paid);

			final var installment6 = new LoanInstallment();
			installment6.setId(installment6Id);
			installment6.setLoan(loan);
			installment6.setAmount(installment6Amount);
			installment6.setDueDate(installment6DueDate);
			installment6.setPaid(installment6Paid);

			loan.setInstallments(List.of(installment1, installment2, installment3, installment4, installment5, installment6));

			when(loanConfigurationProperties.paymentInAdvanceMaxMonths())
					.thenReturn((short) 3);
			when(loanConfigurationProperties.rewardPerDay())
					.thenReturn(0.001);
			when(loanConfigurationProperties.penaltyPerDay())
					.thenReturn(0.001);

			when(loanRepository.findById(loanId))
					.thenReturn(Optional.of(loan));
			when(loanRepository.save(same(loan)))
					.thenAnswer(invocation -> invocation.getArgument(0, Loan.class));

			final LoanPaymentResultDto result;

			try (final var mock = mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
				mock.when(LocalDate::now).thenReturn(today);
				result = loanService.payLoan(loanId, new PayLoanCommand(paymentAmount));
			}

			final var totalPaidDebt = new BigDecimal("20000");

			// assertions
			final var loanCaptor = ArgumentCaptor.forClass(Loan.class);
			verify(loanRepository, times(1)).findById(loanId);
			verify(loanRepository, times(1)).save(loanCaptor.capture());
			verify(customerService, times(1)).returnCreditLimit(customerId, totalPaidDebt);

			final var installments = loan.getInstallments();
			assertEquals(6, installments.size());

			assertTrue(installment1.getPaid());
			assertEquals(installment1.getPaidAmount(), installment1PaidAmount);
			assertEquals(installment1.getPaymentDate(), installment1PaymentDate);

			assertTrue(installment2.getPaid());
			assertEquals(installment2.getPaidAmount(), new BigDecimal("5000.01"));
			assertEquals(installment2.getPaymentDate(), today);

			assertTrue(installment3.getPaid());
			assertEquals(installment3.getPaidAmount(), new BigDecimal("4999.98"));
			assertEquals(installment3.getPaymentDate(), today);

			assertTrue(installment4.getPaid());
			assertEquals(installment4.getPaidAmount(), new BigDecimal("4999.95"));
			assertEquals(installment4.getPaymentDate(), today);

			assertTrue(installment5.getPaid());
			assertEquals(installment5.getPaidAmount(), new BigDecimal("4999.92"));
			assertEquals(installment5.getPaymentDate(), today);

			assertFalse(installment6.getPaid());
			assertEquals(installment6.getPaidAmount(), BigDecimal.ZERO);
			assertNull(installment6.getPaymentDate());

			assertEquals(4, result.numberOfInstallmentsPaid());
			assertEquals(new BigDecimal("19999.86"), result.totalPaidAmount());
			assertFalse(result.allInstallmentsOfLoanPaid());

			assertFalse(loanCaptor.getValue().getPaid());
		}

		@Test
		@DisplayName("Given more than enough payment amount to pay all the installments and installments within the allowed time period, payLoan should pay all the remaining installments and return payment information")
		void givenMoreThanEnoughPaymentAmountToFullPayLoanAndInstallmentsWithingTheAllowedPeriod_payLoan_shouldPayAllInstallmentsAndReturnPaymentInfo() throws LoanAlreadyPaidException, LoanNotFoundException, CustomerNotFoundException {
			final var paymentAmount = new BigDecimal("50000");
			final var today = LocalDate.of(2024, 4, 11);

			final var customerId = 100002L;
			final var name = "Jane";
			final var surname = "Doe";
			final var creditLimit = new BigDecimal("500000");
			final var usedCreditLimit = new BigDecimal("220000");

			final var customer = new Customer();
			customer.setId(customerId);
			customer.setName(name);
			customer.setSurname(surname);
			customer.setCreditLimit(creditLimit);
			customer.setUsedCreditLimit(usedCreditLimit);

			final var loanId = 1L;
			final var loanAmount = new BigDecimal("20000");
			final var numberOfInstallments = (short) 6;
			final var paid = false;
			final var createDate = LocalDate.of(2024, 1, 1);

			final var loan = new Loan();
			loan.setId(loanId);
			loan.setLoanAmount(loanAmount);
			loan.setNumberOfInstallments(numberOfInstallments);
			loan.setPaid(paid);
			loan.setCreateDate(createDate);
			loan.setCustomer(customer);

			final var installment1Id = 1L;
			final var installment1Amount = new BigDecimal("5000");
			final var installment1DueDate = LocalDate.of(2024, 2, 1);
			final var installment1Paid = true;
			final var installment1PaidAmount = new BigDecimal("5000");
			final var installment1PaymentDate = LocalDate.of(2024, 2, 1);

			final var installment2Id = 2L;
			final var installment2Amount = new BigDecimal("5000");
			final var installment2DueDate = LocalDate.of(2024, 3, 1);
			final var installment2Paid = true;
			final var installment2PaidAmount = new BigDecimal("5000");
			final var installment2PaymentDate = LocalDate.of(2024, 3, 1);

			final var installment3Id = 3L;
			final var installment3Amount = new BigDecimal("5000");
			final var installment3DueDate = LocalDate.of(2024, 4, 1);
			final var installment3Paid = false;

			final var installment4Id = 4L;
			final var installment4Amount = new BigDecimal("5000");
			final var installment4DueDate = LocalDate.of(2024, 5, 1);
			final var installment4Paid = false;

			final var installment5Id = 5L;
			final var installment5Amount = new BigDecimal("5000");
			final var installment5DueDate = LocalDate.of(2024, 6, 1);
			final var installment5Paid = false;

			final var installment6Id = 6L;
			final var installment6Amount = new BigDecimal("5000");
			final var installment6DueDate = LocalDate.of(2024, 7, 1);
			final var installment6Paid = false;

			final var installment1 = new LoanInstallment();
			installment1.setId(installment1Id);
			installment1.setLoan(loan);
			installment1.setAmount(installment1Amount);
			installment1.setDueDate(installment1DueDate);
			installment1.setPaid(installment1Paid);
			installment1.setPaidAmount(installment1PaidAmount);
			installment1.setPaymentDate(installment1PaymentDate);

			final var installment2 = new LoanInstallment();
			installment2.setId(installment2Id);
			installment2.setLoan(loan);
			installment2.setAmount(installment2Amount);
			installment2.setDueDate(installment2DueDate);
			installment2.setPaid(installment2Paid);
			installment2.setPaidAmount(installment2PaidAmount);
			installment2.setPaymentDate(installment2PaymentDate);

			final var installment3 = new LoanInstallment();
			installment3.setId(installment3Id);
			installment3.setLoan(loan);
			installment3.setAmount(installment3Amount);
			installment3.setDueDate(installment3DueDate);
			installment3.setPaid(installment3Paid);

			final var installment4 = new LoanInstallment();
			installment4.setId(installment4Id);
			installment4.setLoan(loan);
			installment4.setAmount(installment4Amount);
			installment4.setDueDate(installment4DueDate);
			installment4.setPaid(installment4Paid);

			final var installment5 = new LoanInstallment();
			installment5.setId(installment5Id);
			installment5.setLoan(loan);
			installment5.setAmount(installment5Amount);
			installment5.setDueDate(installment5DueDate);
			installment5.setPaid(installment5Paid);

			final var installment6 = new LoanInstallment();
			installment6.setId(installment6Id);
			installment6.setLoan(loan);
			installment6.setAmount(installment6Amount);
			installment6.setDueDate(installment6DueDate);
			installment6.setPaid(installment6Paid);

			loan.setInstallments(List.of(installment1, installment2, installment3, installment4, installment5, installment6));

			when(loanConfigurationProperties.paymentInAdvanceMaxMonths())
					.thenReturn((short) 3);
			when(loanConfigurationProperties.rewardPerDay())
					.thenReturn(0.001);
			when(loanConfigurationProperties.penaltyPerDay())
					.thenReturn(0.001);

			when(loanRepository.findById(loanId))
					.thenReturn(Optional.of(loan));
			when(loanRepository.save(same(loan)))
					.thenAnswer(invocation -> invocation.getArgument(0, Loan.class));

			final LoanPaymentResultDto result;

			try (final var mock = mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
				mock.when(LocalDate::now).thenReturn(today);
				result = loanService.payLoan(loanId, new PayLoanCommand(paymentAmount));
			}

			final var totalPaidDebt = new BigDecimal("20000");

			// assertions
			final var loanCaptor = ArgumentCaptor.forClass(Loan.class);
			verify(loanRepository, times(1)).findById(loanId);
			verify(loanRepository, times(1)).save(loanCaptor.capture());
			verify(customerService, times(1)).returnCreditLimit(customerId, totalPaidDebt);

			final var installments = loan.getInstallments();
			assertEquals(6, installments.size());

			assertTrue(installment1.getPaid());
			assertEquals(installment1.getPaidAmount(), installment1PaidAmount);
			assertEquals(installment1.getPaymentDate(), installment1PaymentDate);

			assertTrue(installment2.getPaid());
			assertEquals(installment2.getPaidAmount(), installment2PaidAmount);
			assertEquals(installment2.getPaymentDate(), installment2PaymentDate);

			assertTrue(installment3.getPaid());
			assertEquals(installment3.getPaidAmount(), new BigDecimal("5000.01"));
			assertEquals(installment3.getPaymentDate(), today);

			assertTrue(installment4.getPaid());
			assertEquals(installment4.getPaidAmount(), new BigDecimal("4999.98"));
			assertEquals(installment4.getPaymentDate(), today);

			assertTrue(installment5.getPaid());
			assertEquals(installment5.getPaidAmount(), new BigDecimal("4999.95"));
			assertEquals(installment5.getPaymentDate(), today);

			assertTrue(installment6.getPaid());
			assertEquals(installment6.getPaidAmount(), new BigDecimal("4999.92"));
			assertEquals(installment6.getPaymentDate(), today);

			assertEquals(4, result.numberOfInstallmentsPaid());
			assertEquals(new BigDecimal("19999.86"), result.totalPaidAmount());
			assertTrue(result.allInstallmentsOfLoanPaid());

			assertTrue(loanCaptor.getValue().getPaid());
		}

		@Test
		@DisplayName("Given insufficient payment amount to pay a single installment, payLoan should not pay any installment")
		void givenInsufficientPaymentAmount_payLoan_shouldNotPay() throws LoanAlreadyPaidException, LoanNotFoundException, CustomerNotFoundException {
			final var paymentAmount = new BigDecimal("2500");
			final var today = LocalDate.of(2024, 4, 11);

			final var customerId = 100002L;
			final var name = "Jane";
			final var surname = "Doe";
			final var creditLimit = new BigDecimal("500000");
			final var usedCreditLimit = new BigDecimal("220000");

			final var customer = new Customer();
			customer.setId(customerId);
			customer.setName(name);
			customer.setSurname(surname);
			customer.setCreditLimit(creditLimit);
			customer.setUsedCreditLimit(usedCreditLimit);

			final var loanId = 1L;
			final var loanAmount = new BigDecimal("20000");
			final var numberOfInstallments = (short) 6;
			final var paid = false;
			final var createDate = LocalDate.of(2024, 1, 1);

			final var loan = new Loan();
			loan.setId(loanId);
			loan.setLoanAmount(loanAmount);
			loan.setNumberOfInstallments(numberOfInstallments);
			loan.setPaid(paid);
			loan.setCreateDate(createDate);
			loan.setCustomer(customer);

			final var installment1Id = 1L;
			final var installment1Amount = new BigDecimal("5000");
			final var installment1DueDate = LocalDate.of(2024, 2, 1);
			final var installment1Paid = true;
			final var installment1PaidAmount = new BigDecimal("5000");
			final var installment1PaymentDate = LocalDate.of(2024, 2, 1);

			final var installment2Id = 2L;
			final var installment2Amount = new BigDecimal("5000");
			final var installment2DueDate = LocalDate.of(2024, 3, 1);
			final var installment2Paid = true;
			final var installment2PaidAmount = new BigDecimal("5000");
			final var installment2PaymentDate = LocalDate.of(2024, 3, 1);

			final var installment3Id = 3L;
			final var installment3Amount = new BigDecimal("5000");
			final var installment3DueDate = LocalDate.of(2024, 4, 1);
			final var installment3Paid = false;

			final var installment4Id = 4L;
			final var installment4Amount = new BigDecimal("5000");
			final var installment4DueDate = LocalDate.of(2024, 5, 1);
			final var installment4Paid = false;

			final var installment5Id = 5L;
			final var installment5Amount = new BigDecimal("5000");
			final var installment5DueDate = LocalDate.of(2024, 6, 1);
			final var installment5Paid = false;

			final var installment6Id = 6L;
			final var installment6Amount = new BigDecimal("5000");
			final var installment6DueDate = LocalDate.of(2024, 7, 1);
			final var installment6Paid = false;

			final var installment1 = new LoanInstallment();
			installment1.setId(installment1Id);
			installment1.setLoan(loan);
			installment1.setAmount(installment1Amount);
			installment1.setDueDate(installment1DueDate);
			installment1.setPaid(installment1Paid);
			installment1.setPaidAmount(installment1PaidAmount);
			installment1.setPaymentDate(installment1PaymentDate);

			final var installment2 = new LoanInstallment();
			installment2.setId(installment2Id);
			installment2.setLoan(loan);
			installment2.setAmount(installment2Amount);
			installment2.setDueDate(installment2DueDate);
			installment2.setPaid(installment2Paid);
			installment2.setPaidAmount(installment2PaidAmount);
			installment2.setPaymentDate(installment2PaymentDate);

			final var installment3 = new LoanInstallment();
			installment3.setId(installment3Id);
			installment3.setLoan(loan);
			installment3.setAmount(installment3Amount);
			installment3.setDueDate(installment3DueDate);
			installment3.setPaid(installment3Paid);

			final var installment4 = new LoanInstallment();
			installment4.setId(installment4Id);
			installment4.setLoan(loan);
			installment4.setAmount(installment4Amount);
			installment4.setDueDate(installment4DueDate);
			installment4.setPaid(installment4Paid);

			final var installment5 = new LoanInstallment();
			installment5.setId(installment5Id);
			installment5.setLoan(loan);
			installment5.setAmount(installment5Amount);
			installment5.setDueDate(installment5DueDate);
			installment5.setPaid(installment5Paid);

			final var installment6 = new LoanInstallment();
			installment6.setId(installment6Id);
			installment6.setLoan(loan);
			installment6.setAmount(installment6Amount);
			installment6.setDueDate(installment6DueDate);
			installment6.setPaid(installment6Paid);

			loan.setInstallments(List.of(installment1, installment2, installment3, installment4, installment5, installment6));

			when(loanRepository.findById(loanId))
					.thenReturn(Optional.of(loan));
			when(loanRepository.save(same(loan)))
					.thenAnswer(invocation -> invocation.getArgument(0, Loan.class));

			final LoanPaymentResultDto result;

			try (final var mock = mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
				mock.when(LocalDate::now).thenReturn(today);
				result = loanService.payLoan(loanId, new PayLoanCommand(paymentAmount));
			}

			// assertions
			final var loanCaptor = ArgumentCaptor.forClass(Loan.class);
			verify(loanRepository, times(1)).findById(loanId);
			verify(loanRepository, times(1)).save(loanCaptor.capture());
			verify(customerService, never()).returnCreditLimit(any(), any());

			final var installments = loan.getInstallments();
			assertEquals(6, installments.size());

			assertTrue(installment1.getPaid());
			assertEquals(installment1.getPaidAmount(), installment1PaidAmount);
			assertEquals(installment1.getPaymentDate(), installment1PaymentDate);

			assertTrue(installment2.getPaid());
			assertEquals(installment2.getPaidAmount(), installment2PaidAmount);
			assertEquals(installment2.getPaymentDate(), installment2PaymentDate);

			assertFalse(installment3.getPaid());
			assertEquals(installment3.getPaidAmount(), BigDecimal.ZERO);
			assertNull(installment3.getPaymentDate());

			assertFalse(installment4.getPaid());
			assertEquals(installment4.getPaidAmount(), BigDecimal.ZERO);
			assertNull(installment4.getPaymentDate());

			assertFalse(installment5.getPaid());
			assertEquals(installment5.getPaidAmount(), BigDecimal.ZERO);
			assertNull(installment5.getPaymentDate());

			assertFalse(installment6.getPaid());
			assertEquals(installment6.getPaidAmount(), BigDecimal.ZERO);
			assertNull(installment6.getPaymentDate());

			assertEquals(0, result.numberOfInstallmentsPaid());
			assertEquals(BigDecimal.ZERO, result.totalPaidAmount());
			assertFalse(result.allInstallmentsOfLoanPaid());

			assertFalse(loanCaptor.getValue().getPaid());
		}

		@Test
		@DisplayName("Given paid loan, payLoan should throw LoanAlreadyPaidException")
		void givenPaidLoan_payLoan_shouldThrow() throws CustomerNotFoundException {
			final var paymentAmount = new BigDecimal("2500");
			final var today = LocalDate.of(2024, 11, 11);

			final var customerId = 100002L;
			final var name = "Jane";
			final var surname = "Doe";
			final var creditLimit = new BigDecimal("500000");
			final var usedCreditLimit = new BigDecimal("220000");

			final var customer = new Customer();
			customer.setId(customerId);
			customer.setName(name);
			customer.setSurname(surname);
			customer.setCreditLimit(creditLimit);
			customer.setUsedCreditLimit(usedCreditLimit);

			final var loanId = 1L;
			final var loanAmount = new BigDecimal("20000");
			final var numberOfInstallments = (short) 6;
			final var paid = true;
			final var createDate = LocalDate.of(2024, 1, 1);

			final var loan = new Loan();
			loan.setId(loanId);
			loan.setLoanAmount(loanAmount);
			loan.setNumberOfInstallments(numberOfInstallments);
			loan.setPaid(paid);
			loan.setCreateDate(createDate);
			loan.setCustomer(customer);

			final var installment1Id = 1L;
			final var installment1Amount = new BigDecimal("5000");
			final var installment1DueDate = LocalDate.of(2024, 2, 1);
			final var installment1Paid = true;
			final var installment1PaidAmount = new BigDecimal("5000");
			final var installment1PaymentDate = LocalDate.of(2024, 2, 1);

			final var installment2Id = 2L;
			final var installment2Amount = new BigDecimal("5000");
			final var installment2DueDate = LocalDate.of(2024, 3, 1);
			final var installment2Paid = true;
			final var installment2PaidAmount = new BigDecimal("5000");
			final var installment2PaymentDate = LocalDate.of(2024, 3, 1);

			final var installment3Id = 3L;
			final var installment3Amount = new BigDecimal("5000");
			final var installment3DueDate = LocalDate.of(2024, 4, 1);
			final var installment3Paid = true;
			final var installment3PaidAmount = new BigDecimal("5000");
			final var installment3PaymentDate = LocalDate.of(2024, 4, 1);

			final var installment4Id = 4L;
			final var installment4Amount = new BigDecimal("5000");
			final var installment4DueDate = LocalDate.of(2024, 5, 1);
			final var installment4Paid = true;
			final var installment4PaidAmount = new BigDecimal("5000");
			final var installment4PaymentDate = LocalDate.of(2024, 5, 1);

			final var installment5Id = 5L;
			final var installment5Amount = new BigDecimal("5000");
			final var installment5DueDate = LocalDate.of(2024, 6, 1);
			final var installment5Paid = true;
			final var installment5PaidAmount = new BigDecimal("5000");
			final var installment5PaymentDate = LocalDate.of(2024, 6, 1);

			final var installment6Id = 6L;
			final var installment6Amount = new BigDecimal("5000");
			final var installment6DueDate = LocalDate.of(2024, 7, 1);
			final var installment6Paid = true;
			final var installment6PaidAmount = new BigDecimal("5000");
			final var installment6PaymentDate = LocalDate.of(2024, 7, 1);

			final var installment1 = new LoanInstallment();
			installment1.setId(installment1Id);
			installment1.setLoan(loan);
			installment1.setAmount(installment1Amount);
			installment1.setDueDate(installment1DueDate);
			installment1.setPaid(installment1Paid);
			installment1.setPaidAmount(installment1PaidAmount);
			installment1.setPaymentDate(installment1PaymentDate);

			final var installment2 = new LoanInstallment();
			installment2.setId(installment2Id);
			installment2.setLoan(loan);
			installment2.setAmount(installment2Amount);
			installment2.setDueDate(installment2DueDate);
			installment2.setPaid(installment2Paid);
			installment2.setPaidAmount(installment2PaidAmount);
			installment2.setPaymentDate(installment2PaymentDate);

			final var installment3 = new LoanInstallment();
			installment3.setId(installment3Id);
			installment3.setLoan(loan);
			installment3.setAmount(installment3Amount);
			installment3.setDueDate(installment3DueDate);
			installment3.setPaid(installment3Paid);
			installment3.setPaidAmount(installment3PaidAmount);
			installment3.setPaymentDate(installment3PaymentDate);

			final var installment4 = new LoanInstallment();
			installment4.setId(installment4Id);
			installment4.setLoan(loan);
			installment4.setAmount(installment4Amount);
			installment4.setDueDate(installment4DueDate);
			installment4.setPaid(installment4Paid);
			installment4.setPaidAmount(installment4PaidAmount);
			installment4.setPaymentDate(installment4PaymentDate);

			final var installment5 = new LoanInstallment();
			installment5.setId(installment5Id);
			installment5.setLoan(loan);
			installment5.setAmount(installment5Amount);
			installment5.setDueDate(installment5DueDate);
			installment5.setPaid(installment5Paid);
			installment5.setPaidAmount(installment5PaidAmount);
			installment5.setPaymentDate(installment5PaymentDate);

			final var installment6 = new LoanInstallment();
			installment6.setId(installment6Id);
			installment6.setLoan(loan);
			installment6.setAmount(installment6Amount);
			installment6.setDueDate(installment6DueDate);
			installment6.setPaid(installment6Paid);
			installment6.setPaidAmount(installment6PaidAmount);
			installment6.setPaymentDate(installment6PaymentDate);

			loan.setInstallments(List.of(installment1, installment2, installment3, installment4, installment5, installment6));

			when(loanRepository.findById(loanId))
					.thenReturn(Optional.of(loan));

			try (final var mock = mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
				mock.when(LocalDate::now).thenReturn(today);
				assertThrows(LoanAlreadyPaidException.class, () -> loanService.payLoan(loanId, new PayLoanCommand(paymentAmount)));
			}

			// assertions
			verify(loanRepository, times(1)).findById(loanId);
			verify(loanRepository, never()).save(any());
			verify(customerService, never()).returnCreditLimit(any(), any());
		}

		@Test
		@DisplayName("Given non-existent loan, payLoan should throw LoanNotFoundException")
		void givenNonExistentLoan_payLoan_shouldThrow() throws CustomerNotFoundException {
			final var paymentAmount = new BigDecimal("2500");
			final var loanId = 1L;

			when(loanRepository.findById(loanId))
					.thenReturn(Optional.empty());

			assertThrows(LoanNotFoundException.class, () -> loanService.payLoan(loanId, new PayLoanCommand(paymentAmount)));

			// assertions
			verify(loanRepository, times(1)).findById(loanId);
			verify(loanRepository, never()).save(any());
			verify(customerService, never()).returnCreditLimit(any(), any());
		}
	}
}