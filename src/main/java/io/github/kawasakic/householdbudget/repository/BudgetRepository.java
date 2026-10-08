package io.github.kawasakic.householdbudget.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.kawasakic.householdbudget.Budget;

// 予算データをDBから取得・保存するRepository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // 対象月が一致する予算を、カテゴリ分まとめて取得する
    List<Budget> findByTargetMonth(LocalDate targetMonth);
    
    // 対象月とカテゴリIDが一致する予算を取得する
    Optional<Budget> findByTargetMonthAndCategoryId(
            LocalDate targetMonth,
            Long categoryId);
}