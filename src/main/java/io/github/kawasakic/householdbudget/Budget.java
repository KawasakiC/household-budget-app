package io.github.kawasakic.householdbudget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.format.annotation.DateTimeFormat;

//budgetsテーブルをJavaから扱うためのクラス
@Entity
@Table(name = "budgets")
public class Budget {

	// 予算IDを保持するフィールド
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 対象月を保持するフィールド
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "target_month", nullable = false)
    private LocalDate targetMonth;

    // カテゴリIDを保持するフィールド
    @NotNull
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    // 予算金額を保持するフィールド
    @NotNull(message = "金額を入力してください。")
    @Positive(message = "金額は0円より大きい値を入力してください。")
    @Column(name = "budget_amount", nullable = false, precision = 10, scale = 0)
    private BigDecimal budgetAmount;

    // 予算が確定済みかどうかを保持するフラグ
    @Column(name = "confirmed", nullable = false)
    private boolean confirmed = false;
    
    // 作成日時を保持するフィールド
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 更新日時を保持するフィールド
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPAが使う引数なしコンストラクタ
    public Budget() {
    }
    
    // レコード作成時の日付
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    // レコード更新時の日付
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getTargetMonth() {
        return targetMonth;
    }

    public void setTargetMonth(LocalDate targetMonth) {
        this.targetMonth = targetMonth;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
