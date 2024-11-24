package com.ing.loanapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ing.loanapi.dto.LoanDto;
import com.ing.loanapi.entity.Loan;

@Mapper(componentModel = "spring")
public interface LoanMapper {

	@Mapping(source = "customer.id", target = "customerId")
	LoanDto mapToLoanDto(Loan loan);
}
