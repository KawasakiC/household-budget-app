package io.github.kawasakic.householdbudget.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.kawasakic.householdbudget.Category;
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
    
    // 指定した月の支出を、支出日の昇順で取得する
    @Transactional(readOnly = true)
    public List<Expense> findByMonth(YearMonth month) {
        // 指定月の初日と末日を検索範囲にする
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        return expenseRepository.findByExpenseDateBetweenOrderByExpenseDateAsc(startDate, endDate);
    }
    
    // 指定月の支出から、満足度3以上の上位3件を取得する
    @Transactional(readOnly = true)
    public List<Expense> findTop3GoodExpensesByMonth(YearMonth month) {
        return findByMonth(month).stream()
                // 「使ってよかった」として満足度3以上に絞る
                .filter(expense -> expense.getSatisfaction() >= 3)
                // 満足度の高い順、同じ満足度なら支出日の新しい順に並べる
                .sorted(
                        Comparator.comparing(Expense::getSatisfaction).reversed()
                                .thenComparing(
                                        Expense::getExpenseDate,
                                        Comparator.reverseOrder()
                                )
                )
                // 上位3件だけにする
                .limit(3)
                .toList();
    }
    
    // カテゴリIDごとの支出金額を合計する
    public Map<Long, BigDecimal> calculateCategoryTotals(
            List<Category> categories,
            List<Expense> expenses) {

        Map<Long, BigDecimal> categoryTotals = new HashMap<>();

        // すべてのカテゴリを0円で初期化する
        for (Category category : categories) {
            categoryTotals.put(category.getId(), BigDecimal.ZERO);
        }

        // 支出があるカテゴリには金額を加算する
        for (Expense expense : expenses) {
            categoryTotals.merge(
                    expense.getCategoryId(),
                    expense.getAmount(),
                    BigDecimal::add
            );
        }

        return categoryTotals;
    }
    
    // 月の支出金額を合計する
    public BigDecimal calculateTotalAmount(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}