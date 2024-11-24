package com.ing.loanapi.mapper;

import org.mapstruct.Mapper;

import com.ing.loanapi.dto.CustomerDto;
import com.ing.loanapi.entity.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

	CustomerDto mapToCustomerDto(Customer customer);

	Customer mapToCustomer(CustomerDto customer);
}
