package com.ing.loanapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ing.loanapi.dto.CustomerDto;
import com.ing.loanapi.entity.Customer;
import com.ing.loanapi.exception.CustomerNotFoundException;
import com.ing.loanapi.exception.InsufficientCreditLimitException;
import com.ing.loanapi.mapper.CustomerMapper;
import com.ing.loanapi.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService tests")
public class CustomerServiceTests {

	@InjectMocks
	CustomerService customerService;

	@Mock
	CustomerRepository customerRepository;

	@Mock
	CustomerMapper customerMapper;

	@Nested
	@DisplayName("findCustomerById tests")
	class FindCustomerByIdTests {
		@Test
		@DisplayName("Given existing customer, findCustomerById should return customer")
		void givenExistingCustomer_findCustomerById_shouldReturnCustomer() throws CustomerNotFoundException {
			final var customerId = 100001L;
			final var name = "John";
			final var surname = "Doe";
			final var creditLimit = new BigDecimal("100000");
			final var usedCreditLimit = BigDecimal.ZERO;

			final var customer = new Customer();
			customer.setId(customerId);
			customer.setName(name);
			customer.setSurname(surname);
			customer.setCreditLimit(creditLimit);
			customer.setUsedCreditLimit(usedCreditLimit);

			when(customerRepository.findById(customerId))
					.thenReturn(Optional.of(customer));

			final var customerDto = new CustomerDto(
					customerId, name, surname, creditLimit, usedCreditLimit);

			when(customerMapper.mapToCustomerDto(same(customer)))
					.thenReturn(customerDto);

			final var result = customerService.findCustomerById(customerId);

			// assertions
			verify(customerRepository, times(1)).findById(customerId);
			verify(customerMapper, times(1)).mapToCustomerDto(customer);

			assertEquals(customerDto, result);
		}

		@Test
		@DisplayName("Given non-existent customer, findCustomerById should throw CustomerNotFoundException")
		void givenNonExistentCustomer_findCustomerById_shouldThrow() {
			final var customerId = 100002L;

			when(customerRepository.findById(customerId))
					.thenReturn(Optional.empty());

			// assertions
			assertThrows(CustomerNotFoundException.class, () -> customerService.findCustomerById(customerId));

			verify(customerRepository, times(1)).findById(customerId);
			verify(customerMapper, times(0)).mapToCustomerDto(null);
		}
	}

	@Nested
	@DisplayName("useCreditLimit tests")
	class UseCreditLimitTests {

		@Test
		@DisplayName("Given customer with sufficient credit limit, useCreditLimit should return updated customer")
		void givenCustomerWithSufficientLimit_useCreditLimit_shouldReturnUpdatedCustomer() throws CustomerNotFoundException, InsufficientCreditLimitException {
			final var creditLimitToUse = new BigDecimal("50000");

			final var customerId = 100001L;
			final var name = "John";
			final var surname = "Doe";
			final var creditLimit = new BigDecimal("100000");
			final var usedCreditLimit = new BigDecimal("20000");

			final var customer = new Customer();
			customer.setId(customerId);
			customer.setName(name);
			customer.setSurname(surname);
			customer.setCreditLimit(creditLimit);
			customer.setUsedCreditLimit(usedCreditLimit);

			when(customerRepository.findById(customerId))
					.thenReturn(Optional.of(customer));

			final var updatedCustomer = new Customer();
			updatedCustomer.setId(customerId);
			updatedCustomer.setName(name);
			updatedCustomer.setSurname(surname);
			updatedCustomer.setCreditLimit(creditLimit);
			updatedCustomer.setUsedCreditLimit(new BigDecimal("70000"));

			when(customerRepository.save(same(customer)))
					.thenReturn(updatedCustomer);

			final var customerDto = new CustomerDto(
					customerId, name, surname, creditLimit, new BigDecimal("70000"));

			when(customerMapper.mapToCustomerDto(same(updatedCustomer)))
					.thenReturn(customerDto);

			final var result = customerService.useCreditLimit(customerId, creditLimitToUse);

			// assertions
			verify(customerRepository, times(1)).findById(customerId);
			verify(customerRepository, times(1)).save(same(customer));
			verify(customerMapper, times(1)).mapToCustomerDto(same(updatedCustomer));

			assertEquals(customerDto, result);
		}

		@Test
		@DisplayName("Given customer with insufficient credit limit, useCreditLimit should throw InsufficientCreditLimitException")
		void givenCustomerWithInsufficientCreditLimit_useCreditLimit_shouldThrow() {
			final var creditLimitToUse = new BigDecimal("50000");

			final var customerId = 100002L;
			final var name = "Jane";
			final var surname = "Doe";
			final var creditLimit = new BigDecimal("100000");
			final var usedCreditLimit = new BigDecimal("80000");

			final var customer = new Customer();
			customer.setId(customerId);
			customer.setName(name);
			customer.setSurname(surname);
			customer.setCreditLimit(creditLimit);
			customer.setUsedCreditLimit(usedCreditLimit);

			when(customerRepository.findById(customerId))
					.thenReturn(Optional.of(customer));

			// assertions
			assertThrows(InsufficientCreditLimitException.class, () -> customerService.useCreditLimit(customerId, creditLimitToUse));

			verify(customerRepository, times(1)).findById(customerId);
			verify(customerRepository, never()).save(any());
			verify(customerMapper, never()).mapToCustomerDto(any());
		}

		@Test
		@DisplayName("Given non-existent customer, useCreditLimit should throw CustomerNotFoundException")
		void givenNonExistentCustomer_useCreditLimit_shouldThrow() {
			final var creditLimitToUse = new BigDecimal("50000");
			final var customerId = 100003L;

			when(customerRepository.findById(customerId))
					.thenReturn(Optional.empty());

			// assertions
			assertThrows(CustomerNotFoundException.class, () -> customerService.useCreditLimit(customerId, creditLimitToUse));

			verify(customerRepository, times(1)).findById(customerId);
			verify(customerRepository, never()).save(any());
			verify(customerMapper, never()).mapToCustomerDto(any());
		}
	}

	@Nested
	@DisplayName("returnCreditLimit tests")
	class ReturnCreditLimitTests {

		@Test
		@DisplayName("Given customer with used credit limit, returnCreditLimit should return updated customer")
		void givenCustomerWithUsedCreditLimit_returnCreditLimit_shouldReturnUpdatedCustomer() throws CustomerNotFoundException {
			final var creditLimitToReturn = new BigDecimal("50000");

			final var customerId = 100001L;
			final var name = "John";
			final var surname = "Doe";
			final var creditLimit = new BigDecimal("100000");
			final var usedCreditLimit = new BigDecimal("90000");

			final var customer = new Customer();
			customer.setId(customerId);
			customer.setName(name);
			customer.setSurname(surname);
			customer.setCreditLimit(creditLimit);
			customer.setUsedCreditLimit(usedCreditLimit);

			when(customerRepository.findById(customerId))
					.thenReturn(Optional.of(customer));

			final var updatedCustomer = new Customer();
			updatedCustomer.setId(customerId);
			updatedCustomer.setName(name);
			updatedCustomer.setSurname(surname);
			updatedCustomer.setCreditLimit(creditLimit);
			updatedCustomer.setUsedCreditLimit(new BigDecimal("40000"));

			when(customerRepository.save(same(customer)))
					.thenReturn(updatedCustomer);

			final var customerDto = new CustomerDto(
					customerId, name, surname, creditLimit, new BigDecimal("40000"));

			when(customerMapper.mapToCustomerDto(same(updatedCustomer)))
					.thenReturn(customerDto);

			final var result = customerService.returnCreditLimit(customerId, creditLimitToReturn);

			// assertions
			verify(customerRepository, times(1)).findById(customerId);
			verify(customerRepository, times(1)).save(same(customer));
			verify(customerMapper, times(1)).mapToCustomerDto(same(updatedCustomer));

			assertEquals(customerDto, result);
		}

		@Test
		@DisplayName("Given customer with less used credit limit than creditLimit to return, returnCreditLimit should return updated customer with usedCreditLimit set to zero")
		void givenCustomerWithNoUsedCreditLimit_returnCreditLimit_shouldThrow() throws CustomerNotFoundException {
			final var creditLimitToReturn = new BigDecimal("50000");

			final var customerId = 100002L;
			final var name = "Jane";
			final var surname = "Doe";
			final var creditLimit = new BigDecimal("100000");
			final var usedCreditLimit = new BigDecimal("20000");

			final var customer = new Customer();
			customer.setId(customerId);
			customer.setName(name);
			customer.setSurname(surname);
			customer.setCreditLimit(creditLimit);
			customer.setUsedCreditLimit(usedCreditLimit);

			when(customerRepository.findById(customerId))
					.thenReturn(Optional.of(customer));

			final var updatedCustomer = new Customer();
			updatedCustomer.setId(customerId);
			updatedCustomer.setName(name);
			updatedCustomer.setSurname(surname);
			updatedCustomer.setCreditLimit(creditLimit);
			updatedCustomer.setUsedCreditLimit(BigDecimal.ZERO);

			when(customerRepository.save(same(customer)))
					.thenReturn(updatedCustomer);

			final var customerDto = new CustomerDto(
					customerId, name, surname, creditLimit, BigDecimal.ZERO);

			when(customerMapper.mapToCustomerDto(same(updatedCustomer)))
					.thenReturn(customerDto);

			final var result = customerService.returnCreditLimit(customerId, creditLimitToReturn);

			// assertions
			verify(customerRepository, times(1)).findById(customerId);
			verify(customerRepository, times(1)).save(same(customer));
			verify(customerMapper, times(1)).mapToCustomerDto(same(updatedCustomer));

			assertEquals(customerDto, result);
		}

		@Test
		@DisplayName("Given non-existent customer, returnCreditLimit should throw CustomerNotFoundException")
		void givenNonExistentCustomer_returnCreditLimit_shouldThrow() {
			final var creditLimitToReturn = new BigDecimal("50000");
			final var customerId = 100003L;

			when(customerRepository.findById(customerId))
					.thenReturn(Optional.empty());

			// assertions
			assertThrows(CustomerNotFoundException.class, () -> customerService.returnCreditLimit(customerId, creditLimitToReturn));

			verify(customerRepository, times(1)).findById(customerId);
			verify(customerRepository, never()).save(any());
			verify(customerMapper, never()).mapToCustomerDto(any());
		}
	}
}
