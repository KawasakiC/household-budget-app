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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.format.annotation.DateTimeFormat;

//expensesテーブルをJavaから扱うためのクラス
@Entity
@Table(name = "expenses")
public class Expense {

	// 支出IDを保持するフィールド
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 支出日を保持するフィールド
    @NotNull(message = "支出日を入力してください。")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    // 支出金額を保持するフィールド
    @NotNull(message = "金額を入力してください。")
    @Positive(message = "金額は0円より大きい値を入力してください。")
    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal amount;

    // カテゴリIDを保持するフィールド
    @NotNull(message = "カテゴリを選択してください。")
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    // 支払い方法IDを保持するフィールド
    @NotNull(message = "支払い方法を選択してください。")
    @Column(name = "payment_method_id", nullable = false)
    private Long paymentMethodId;

    // 店舗名を保持するフィールド
    @Column(length = 100)
    private String store;

    // メモを保持するフィールド
    @Column(length = 500)
    private String memo;

    // 支出に対する満足度を保持するフィールド
    @NotNull(message = "満足度を選択してください。")
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Short satisfaction;

    // 作成日時を保持するフィールド
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 更新日時を保持するフィールド
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPAが使う引数なしコンストラクタ
    public Expense() {
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

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Short getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(Short satisfaction) {
        this.satisfaction = satisfaction;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}