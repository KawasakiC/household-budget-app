package io.github.kawasakic.householdbudget.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.kawasakic.householdbudget.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}