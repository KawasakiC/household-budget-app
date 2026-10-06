package io.github.kawasakic.householdbudget;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// categoriesテーブルをJavaから扱うためのクラス
@Entity
@Table(name = "categories")
public class Category {

    // カテゴリIDを保持するフィールド
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // カテゴリ名を保持するフィールド
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    // 集計方法を保持するフィールド
    @NotBlank
    @Size(max = 20)
    @Column(name = "calculation_method", nullable = false, length = 20)
    private String calculationMethod;

    // 集計に使う金額を保持するフィールド
    @Column(name = "calculation_amount")
    private BigDecimal calculationAmount;

    // 作成日時を保持するフィールド
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 更新日時を保持するフィールド
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPAがデータを読み込むときに使う引数なしコンストラクタ
    public Category() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCalculationMethod() {
        return calculationMethod;
    }

    public void setCalculationMethod(String calculationMethod) {
        this.calculationMethod = calculationMethod;
    }

    public BigDecimal getCalculationAmount() {
        return calculationAmount;
    }

    public void setCalculationAmount(BigDecimal calculationAmount) {
        this.calculationAmount = calculationAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}