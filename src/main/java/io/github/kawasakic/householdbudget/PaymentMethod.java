package io.github.kawasakic.householdbudget;

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

// payment_methodsテーブルをJavaから扱うためのクラス
@Entity
@Table(name = "payment_methods")
public class PaymentMethod {

    // 支払方法IDを保持するフィールド
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 支払方法名を保持するフィールド
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    // 作成日時を保持するフィールド
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 更新日時を保持するフィールド
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPAがデータを読み込むときに使う引数なしコンストラクタ
    public PaymentMethod() {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}