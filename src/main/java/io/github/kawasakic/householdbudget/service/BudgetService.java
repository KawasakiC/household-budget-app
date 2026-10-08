package io.github.kawasakic.householdbudget.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.kawasakic.householdbudget.Budget;
import io.github.kawasakic.householdbudget.repository.BudgetRepository;

//予算に関する処理をまとめるクラス
@Service
public class BudgetService {

    // データベースへの登録・検索などを担当するRepository
    private final BudgetRepository budgetRepository;

    // コンストラクタでRepositoryを受け取る
    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    // 予算をすべて取得する
    @Transactional(readOnly = true)
    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    // 予算を新規登録、または既存データを更新する
    // IDが未設定なら新規登録、設定済みなら更新として扱われる
    @Transactional
    public Budget save(Budget budget) {
        return budgetRepository.save(budget);
    }

    // 指定した月の予算をすべて取得する
    @Transactional(readOnly = true)
    public List<Budget> findByMonth(YearMonth month) {
        // DBでは対象月をその月の1日として保存する
        LocalDate targetMonth = month.atDay(1);

        return budgetRepository.findByTargetMonth(targetMonth);
    }
    
    // 対象月とカテゴリIDを指定して予算を1件取得する
    @Transactional(readOnly = true)
    public Optional<Budget> findByMonthAndCategory(
            YearMonth month,
            Long categoryId) {

        // DBでは対象月を、その月の1日として扱う
        LocalDate targetMonth = month.atDay(1);

        return budgetRepository.findByTargetMonthAndCategoryId(
                targetMonth,
                categoryId
        );
    }
}
