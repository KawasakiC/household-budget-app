package io.github.kawasakic.householdbudget.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.kawasakic.householdbudget.Budget;
import io.github.kawasakic.householdbudget.Category;
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
    
    // カテゴリごとの月予算を計算する
    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> calculateMonthlyAmounts(
            YearMonth month,
            List<Category> categories) {

        Map<Long, BigDecimal> amounts = new LinkedHashMap<>();

        // 指定月に含まれる土曜日の数を数える
        long saturdayCount = month.atDay(1)
                .datesUntil(month.atEndOfMonth().plusDays(1))
                .filter(date -> date.getDayOfWeek() == DayOfWeek.SATURDAY)
                .count();

        // カテゴリごとの予算計算
        for (Category category : categories) {
            BigDecimal calculationAmount = category.getCalculationAmount();
            BigDecimal monthlyAmount;

            if ("WEEKLY".equals(category.getCalculationMethod())) {
                monthlyAmount = calculationAmount.multiply(
                        BigDecimal.valueOf(saturdayCount));

            } else if ("FIXED".equals(category.getCalculationMethod())) {
                monthlyAmount = calculationAmount;

            } else {
                throw new IllegalArgumentException(
                        "未対応の計算方法です: " + category.getCalculationMethod());
            }

            amounts.put(category.getId(), monthlyAmount);
        }

        return amounts;
    }
    
    // 月ごとの予算を確定して保存する
    @Transactional
    public List<Budget> confirmBudgets(
            YearMonth month,
            Map<Long, BigDecimal> budgetAmounts) {

        // DBには対象月の1日を保存する
        LocalDate targetMonth = month.atDay(1);

        List<Budget> savedBudgets = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : budgetAmounts.entrySet()) {
            Long categoryId = entry.getKey();
            BigDecimal budgetAmount = entry.getValue();

            // 同じ対象月・カテゴリの予算があれば更新し、なければ新規作成する
            Budget budget = budgetRepository
                    .findByTargetMonthAndCategoryId(targetMonth, categoryId)
                    .orElseGet(Budget::new);

            budget.setTargetMonth(targetMonth);
            budget.setCategoryId(categoryId);
            budget.setBudgetAmount(budgetAmount);
            budget.setConfirmed(true);

            savedBudgets.add(budgetRepository.save(budget));
        }

        return savedBudgets;
    }
    
    // カテゴリごとの残金を計算する
    public Map<Long, BigDecimal> calculateCategoryRemaining(
            List<Category> categories,
            List<Budget> monthlyBudgets,
            Map<Long, BigDecimal> categoryTotals) {

        // カテゴリIDから、確定済み予算額を取得できるMapを作る
        Map<Long, BigDecimal> monthlyBudgetAmounts = new HashMap<>();

        for (Budget budget : monthlyBudgets) {
            // 未確定の予算は残金計算に使わない
            if (budget.isConfirmed()) {
                monthlyBudgetAmounts.put(
                        budget.getCategoryId(),
                        budget.getBudgetAmount()
                );
            }
        }

        // 予算額から支出合計を引いた残金を計算する
        Map<Long, BigDecimal> categoryRemaining = new HashMap<>();

        for (Category category : categories) {
            BigDecimal budget = monthlyBudgetAmounts.get(category.getId());

            // 確定済みの予算があるカテゴリだけ計算する
            if (budget != null) {
                BigDecimal spent = categoryTotals.getOrDefault(
                        category.getId(),
                        BigDecimal.ZERO
                );

                categoryRemaining.put(
                        category.getId(),
                        budget.subtract(spent)
                );
            }
        }

        return categoryRemaining;
    }
    
    // 確定済みの予算が1件以上あるか確認する
    public boolean hasConfirmedBudget(List<Budget> budgets) {
        return budgets.stream()
                .anyMatch(Budget::isConfirmed);
    }

    // 確定済み予算の金額だけを合計する
    public BigDecimal calculateConfirmedTotal(List<Budget> budgets) {
        return budgets.stream()
                .filter(Budget::isConfirmed)
                .map(Budget::getBudgetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 確定済み予算がある場合、予算合計から支出合計を引いて残金を計算する
    public BigDecimal calculateRemainingAmount(
            List<Budget> budgets,
            BigDecimal totalExpense) {

        // 確定済み予算がなければ、残金は未設定とする
        if (!hasConfirmedBudget(budgets)) {
            return null;
        }

        return calculateConfirmedTotal(budgets).subtract(totalExpense);
    }
    
    // 入力された予算額を合計する。すべて未入力ならnullを返す
    public BigDecimal calculateTotalBudget(List<BigDecimal> amounts) {
        boolean allAmountsEmpty = amounts.stream()
                .allMatch(Objects::isNull);

        if (allAmountsEmpty) {
            return null;
        }

        // 未入力の欄は0円として合計する
        return amounts.stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
