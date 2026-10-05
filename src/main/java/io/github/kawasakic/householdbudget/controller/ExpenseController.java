package io.github.kawasakic.householdbudget.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.kawasakic.householdbudget.Expense;
import io.github.kawasakic.householdbudget.service.ExpenseService;

// ブラウザーからのHTTPリクエストを受け取り、支出データを返すクラス
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    // 支出の検索などを行うService
    private final ExpenseService expenseService;

    // SpringがExpenseServiceを自動で渡してくれる
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // GET /api/expenses にアクセスしたときに実行する
    // データベースにある支出をすべて取得し、JSON形式で返す
    @GetMapping
    public List<Expense> findAll() {
        return expenseService.findAll();
    }
}