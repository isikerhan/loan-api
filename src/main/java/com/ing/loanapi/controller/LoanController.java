package com.ing.loanapi.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ing.loanapi.dto.LoanDto;
import com.ing.loanapi.dto.LoanInstallmentDto;
import com.ing.loanapi.dto.LoanPaymentResultDto;
import com.ing.loanapi.dto.command.CreateLoanCommand;
import com.ing.loanapi.dto.command.PayLoanCommand;
import com.ing.loanapi.exception.BusinessException;
import com.ing.loanapi.exception.CustomerNotFoundException;
import com.ing.loanapi.exception.LoanNotFoundException;
import com.ing.loanapi.service.LoanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

	private final LoanService loanService;

	@GetMapping
	@Operation(summary = "Get loans of a customer")
	@ApiResponse(responseCode = "200", description = "Customer exists and successfully retrieved loans",
			content = {@Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = LoanDto.class)))})
	@ApiResponse(responseCode = "404", description = "Customer is not found", content = @Content)
	public List<LoanDto> getLoans(@RequestParam Long customerId) throws CustomerNotFoundException {
		return loanService.findLoansOfCustomer(customerId);
	}

	@GetMapping("/{loanId}/installments")
	@Operation(summary = "Get installments of a loan")
	@ApiResponse(responseCode = "200", description = "Loan exists and successfully retrieved installments",
			content = {@Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = LoanInstallmentDto.class)))})
	@ApiResponse(responseCode = "404", description = "Loan is not found", content = @Content)
	public List<LoanInstallmentDto> getLoanInstallments(@PathVariable Long loanId) throws LoanNotFoundException {
		return loanService.findInstallmentsOfLoan(loanId);
	}

	@PostMapping
	@Validated
	@Operation(summary = "Create a loan")
	@ApiResponse(responseCode = "200", description = "Successfully created the loan",
			content = {@Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = LoanDto.class)))})
	@ApiResponse(responseCode = "409", description = "Customer does not have enough credit limit", content = @Content)
	@ApiResponse(responseCode = "422", description = "Invalid number of installments provided", content = @Content)
	@ApiResponse(responseCode = "422", description = "Invalid interest rate provided", content = @Content)
	@ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
	public LoanDto createLoan(@RequestBody @Valid CreateLoanCommand createLoanCommand) throws BusinessException {
		return loanService.createLoan(createLoanCommand);
	}

	@PostMapping("/{loanId}/payments")
	@Validated
	@Operation(summary = "Pay loan installments")
	@ApiResponse(responseCode = "200", description = "Successfully paid the loan installments",
			content = {@Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = LoanPaymentResultDto.class)))})
	@ApiResponse(responseCode = "409", description = "All the installments of the loan are already paid", content = @Content)
	@ApiResponse(responseCode = "404", description = "Loan not found", content = @Content)
	public LoanPaymentResultDto payLoan(@PathVariable Long loanId, @RequestBody @Valid PayLoanCommand payLoanCommand) throws BusinessException {
		return loanService.payLoan(loanId, payLoanCommand);
	}

}
