package io.github.kawasakic.householdbudget.form;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// 予算設定画面の入力内容を受け取るクラス
public class BudgetForm {

    // input type="month" から送られる年月（例："2026-10"）
    private String month;

    // カテゴリごとの入力行
    @Valid
    private List<BudgetRow> budgets = new ArrayList<>();

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public List<BudgetRow> getBudgets() {
        return budgets;
    }

    public void setBudgets(List<BudgetRow> budgets) {
        this.budgets = budgets;
    }

    // 予算設定画面の1カテゴリ分の入力内容
    public static class BudgetRow {

    	@NotNull
        private Long categoryId;
    	
        private String categoryName;
        
        @NotNull(message = "予算額を入力してください。")
        @Positive(message = "予算額は0円より大きい値を入力してください。")
        private BigDecimal budgetAmount;

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public BigDecimal getBudgetAmount() {
            return budgetAmount;
        }

        public void setBudgetAmount(BigDecimal budgetAmount) {
            this.budgetAmount = budgetAmount;
        }
    }
}