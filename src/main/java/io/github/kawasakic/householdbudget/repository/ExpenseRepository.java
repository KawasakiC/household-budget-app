package io.github.kawasakic.householdbudget.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.kawasakic.householdbudget.Expense;

//expensesテーブルの検索や登録などを行うRepository
//JpaRepositoryが基本的なDB操作を用意してくれる
//<Expense, Long> は、扱うクラスがExpense、IDの型がLongという意味
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}