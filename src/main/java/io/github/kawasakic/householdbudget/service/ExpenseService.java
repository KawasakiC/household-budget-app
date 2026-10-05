package io.github.kawasakic.householdbudget.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.kawasakic.householdbudget.Expense;
import io.github.kawasakic.householdbudget.repository.ExpenseRepository;

// 支出に関する処理をまとめるクラス
@Service
public class ExpenseService {

    // データベースへの登録・検索などを担当するRepository
    private final ExpenseRepository expenseRepository;

    // コンストラクタでRepositoryを受け取る
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    // 支出をすべて取得する
    @Transactional(readOnly = true)
    public List<Expense> findAll() {
        return expenseRepository.findAll();
    }

    // IDを指定して支出を1件取得する
    // 該当データがない場合もあるため、Optionalで結果を返す
    @Transactional(readOnly = true)
    public Optional<Expense> findById(Long id) {
        return expenseRepository.findById(id);
    }

    // 支出を新規登録、または既存データを更新する
    // IDが未設定なら新規登録、設定済みなら更新として扱われる
    @Transactional
    public Expense save(Expense expense) {
        return expenseRepository.save(expense);
    }

    // IDを指定して支出を削除する
    @Transactional
    public void deleteById(Long id) {
        expenseRepository.deleteById(id);
    }
}