# DB設計

## 1. テーブル一覧

| テーブル名 | 内容 |
|---|---|
| `categories` | 支出カテゴリを管理 |
| `payment_methods` | 支払い方法を管理 |
| `budgets` | 月ごとのカテゴリ別予算を管理 |
| `expenses` | 支出情報を管理 |

---

## 2. categories（カテゴリ）

支出のカテゴリを管理する。

| カラム名 | 型 | NULL | PK | 備考 |
|---|---|---|---|---|
| `id` | BIGINT | NO | ○ | カテゴリID |
| `name` | VARCHAR(50) | NO |  | カテゴリ名 |
| `calculation_method` | VARCHAR(20) | NO |  | 予算計算方法 |
| `calculation_amount` | DECIMAL(10,2) | YES |  | 計算に使用する金額 |
| `created_at` | TIMESTAMP | NO |  | 作成日時 |
| `updated_at` | TIMESTAMP | NO |  | 更新日時 |

### 初期データ

| id | name | calculation_method | calculation_amount |
|---:|---|---|---:|
| 1 | 食費 | WEEKLY | 7500 |
| 2 | 日用品 | FIXED | - |
| 3 | 車 | FIXED | - |
| 4 | 医療費 | FIXED | - |
| 5 | 娯楽費 | FIXED | - |

---

## 3. payment_methods（支払い方法）

支出時の支払い方法を管理する。

| カラム名 | 型 | NULL | PK | 備考 |
|---|---|---|---|---|
| `id` | BIGINT | NO | ○ | 支払い方法ID |
| `name` | VARCHAR(50) | NO |  | 支払い方法名 |
| `created_at` | TIMESTAMP | NO |  | 作成日時 |
| `updated_at` | TIMESTAMP | NO |  | 更新日時 |

### 初期データ

| id | name |
|---:|---|
| 1 | 楽天 |
| 2 | ららぽ |
| 3 | ヨドバシ |
| 4 | PayPay |
| 5 | 現金 |

---

## 4. budgets（予算）

月ごとのカテゴリ別予算を管理する。

| カラム名 | 型 | NULL | PK | 備考 |
|---|---|---|---|---|
| `id` | BIGINT | NO | ○ | 予算ID |
| `target_month` | DATE | NO |  | 対象月 |
| `category_id` | BIGINT | NO |  | カテゴリID |
| `budget_amount` | DECIMAL(10,2) | NO |  | 予算額 |
| `confirmed` | BOOLEAN | NO |  | 確定済みフラグ |
| `created_at` | TIMESTAMP | NO |  | 作成日時 |
| `updated_at` | TIMESTAMP | NO |  | 更新日時 |

### 外部キー

- `category_id` → `categories.id`

### 一意制約

- `target_month` + `category_id`

同じ月・同じカテゴリの予算を複数登録しない。

---

## 5. expenses（支出）

日々の支出を管理する。

| カラム名 | 型 | NULL | PK | 備考 |
|---|---|---|---|---|
| `id` | BIGINT | NO | ○ | 支出ID |
| `expense_date` | DATE | NO |  | 支出日 |
| `amount` | DECIMAL(10,2) | NO |  | 支出金額 |
| `category_id` | BIGINT | NO |  | カテゴリID |
| `payment_method_id` | BIGINT | NO |  | 支払い方法ID |
| `store` | VARCHAR(100) | YES |  | 店舗名 |
| `memo` | VARCHAR(500) | YES |  | メモ |
| `satisfaction` | SMALLINT | NO |  | 満足度 |
| `created_at` | TIMESTAMP | NO |  | 作成日時 |
| `updated_at` | TIMESTAMP | NO |  | 更新日時 |

### 外部キー

- `category_id` → `categories.id`
- `payment_method_id` → `payment_methods.id`

### satisfaction（満足度）

| 値 | 内容 |
|---:|---|
| 5 | 大満足 |
| 4 | 満足 |
| 3 | まぁまぁ |
| 2 | ちょっと後悔 |
| 1 | 後悔 |

---

## 6. テーブル関連

```text
categories
    │
    ├──────────────┐
    │              │
    ▼              ▼
budgets         expenses
                   ▲
                   │
            payment_methods
```

### リレーション

- `categories` 1 : N `budgets`
- `categories` 1 : N `expenses`
- `payment_methods` 1 : N `expenses`

---

## 7. ER図

```text
┌─────────────────────┐
│     categories      │
├─────────────────────┤
│ PK id               │
│    name             │
│    calculation_...  │
│    calculation_...  │
│    created_at       │
│    updated_at       │
└─────────┬───────────┘
          │ 1
          │
          │ N
┌─────────▼───────────┐
│       budgets       │
├─────────────────────┤
│ PK id               │
│    target_month     │
│ FK category_id      │
│    budget_amount    │
│    confirmed        │
│    created_at       │
│    updated_at       │
└─────────────────────┘


┌─────────────────────┐
│  payment_methods    │
├─────────────────────┤
│ PK id               │
│    name             │
│    created_at       │
│    updated_at       │
└─────────┬───────────┘
          │ 1
          │
          │ N
┌─────────▼───────────┐
│      expenses       │
├─────────────────────┤
│ PK id               │
│    expense_date     │
│    amount           │
│ FK category_id      │
│ FK payment_method_id│
│    store            │
│    memo             │
│    satisfaction     │
│    created_at       │
│    updated_at       │
└─────────────────────┘
          ▲
          │
          │ N
          │
┌─────────┴───────────┐
│     categories      │
└─────────────────────┘
```

## 8. 補足

### 予算について

`budgets` は「対象月 × カテゴリ」で1件とする。

例：

```text
2026-10 × 食費     → 30,000円
2026-10 × 日用品   → 10,000円
2026-10 × 車       → 15,000円
```

### 支出について

1件の支出につき、カテゴリと支払い方法を1つずつ設定する。

例：

```text
支出日：2026/10/02
金額：1,500円
カテゴリ：食費
支払い方法：PayPay
店舗：○○スーパー
満足度：3（まぁまぁ）
```

### 満足度について

支出ごとに「使ってよかったか」を記録し、家計簿トップで以下を集計する。

- 今月の支出全体の満足度
- 満足度が低いカテゴリ
- 満足度が低いカテゴリへの注意メッセージ
