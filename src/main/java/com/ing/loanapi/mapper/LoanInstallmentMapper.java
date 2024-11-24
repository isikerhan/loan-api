package com.ing.loanapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ing.loanapi.dto.LoanInstallmentDto;
import com.ing.loanapi.entity.LoanInstallment;

@Mapper(componentModel = "spring")
public interface LoanInstallmentMapper {

	@Mapping(source = "loan.id", target = "loanId")
	LoanInstallmentDto mapToLoanInstallmentDto(LoanInstallment loanInstallment);
}
