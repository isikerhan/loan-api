package com.ing.loanapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ing.loanapi.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
