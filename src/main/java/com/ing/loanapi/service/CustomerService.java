package com.ing.loanapi.service;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ing.loanapi.dto.CustomerDto;
import com.ing.loanapi.exception.BusinessException;
import com.ing.loanapi.exception.CustomerNotFoundException;
import com.ing.loanapi.exception.InsufficientCreditLimitException;
import com.ing.loanapi.mapper.CustomerMapper;
import com.ing.loanapi.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = BusinessException.class)
public class CustomerService {

	private final CustomerRepository customerRepository;
	private final CustomerMapper customerMapper;

	@Transactional(readOnly = true)
	public CustomerDto findCustomerById(Long id) throws CustomerNotFoundException {
		final var customer = customerRepository.findById(id)
				.orElseThrow(() -> new CustomerNotFoundException(id));
		return customerMapper.mapToCustomerDto(customer);
	}

	public CustomerDto useCreditLimit(Long customerId, BigDecimal creditLimitToUse) throws CustomerNotFoundException, InsufficientCreditLimitException {
		if (Objects.isNull(creditLimitToUse) || creditLimitToUse.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Credit limit to use must be greater than zero!");
		}

		final var customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new CustomerNotFoundException(customerId));
		if (customer.getCreditLimit().subtract(customer.getUsedCreditLimit()).compareTo(creditLimitToUse) < 0) {
			throw new InsufficientCreditLimitException();
		}

		customer.setUsedCreditLimit(customer.getUsedCreditLimit().add(creditLimitToUse));
		final var savedCustomer = customerRepository.save(customer);
		return customerMapper.mapToCustomerDto(savedCustomer);
	}

	public CustomerDto returnCreditLimit(Long customerId, BigDecimal creditLimitToReturn) throws CustomerNotFoundException {
		if (Objects.isNull(creditLimitToReturn) || creditLimitToReturn.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Credit limit to return must be greater than zero!");
		}

		final var customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new CustomerNotFoundException(customerId));

		customer.setUsedCreditLimit(customer.getUsedCreditLimit().subtract(creditLimitToReturn).max(BigDecimal.ZERO));
		final var savedCustomer = customerRepository.save(customer);
		return customerMapper.mapToCustomerDto(savedCustomer);
	}
}
