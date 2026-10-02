**********************************************

-- 支出カテゴリ管理テーブル作成
CREATE TABLE categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    calculation_method VARCHAR(20) NOT NULL,
    calculation_amount NUMERIC(10, 0),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE categories IS '支出カテゴリ管理テーブル';

COMMENT ON COLUMN categories.id IS 'カテゴリID';
COMMENT ON COLUMN categories.name IS '支出カテゴリ名';
COMMENT ON COLUMN categories.calculation_method IS '予算の計算方法';
COMMENT ON COLUMN categories.calculation_amount IS '予算計算に使用する金額';
COMMENT ON COLUMN categories.created_at IS '作成日時';
COMMENT ON COLUMN categories.updated_at IS '更新日時';

-- 初期データ登録
INSERT INTO categories (
    name,
    calculation_method,
    calculation_amount
)
VALUES
    ('食費', 'WEEKLY', 7500),
    ('日用品', 'FIXED', NULL),
    ('車', 'FIXED', NULL),
    ('医療費', 'FIXED', NULL),
    ('娯楽費', 'FIXED', NULL);

**********************************************

-- 支払い方法管理テーブル作成
CREATE TABLE payment_methods (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE payment_methods IS '支払い方法管理テーブル';

COMMENT ON COLUMN payment_methods.id IS '支払い方法ID';
COMMENT ON COLUMN payment_methods.name IS '支払い方法名';
COMMENT ON COLUMN payment_methods.created_at IS '作成日時';
COMMENT ON COLUMN payment_methods.updated_at IS '更新日時';

-- 初期データ登録
INSERT INTO payment_methods (
    name
)
VALUES
    ('楽天'),
    ('ららぽ'),
    ('ヨドバシ'),
    ('PayPay'),
    ('現金');

**********************************************

-- カテゴリ別予算管理テーブル
CREATE TABLE budgets (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    target_month DATE NOT NULL,
    category_id BIGINT NOT NULL,
    budget_amount NUMERIC(10, 0) NOT NULL,
    confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    --外部キー
    CONSTRAINT fk_budgets_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id),
    -- 一意制約
    CONSTRAINT uq_budgets_month_category
        UNIQUE (target_month, category_id)
);

COMMENT ON TABLE budgets IS 'カテゴリ別予算管理テーブル';

COMMENT ON COLUMN budgets.id IS '予算ID';
COMMENT ON COLUMN budgets.target_month IS '対象月';
COMMENT ON COLUMN budgets.category_id IS 'カテゴリID';
COMMENT ON COLUMN budgets.budget_amount IS '予算額';
COMMENT ON COLUMN budgets.confirmed IS '予算確定済みフラグ';
COMMENT ON COLUMN budgets.created_at IS '作成日時';
COMMENT ON COLUMN budgets.updated_at IS '更新日時';

**********************************************

-- 支出管理テーブル作成
CREATE TABLE expenses (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    expense_date DATE NOT NULL,
    amount NUMERIC(10, 0) NOT NULL,
    category_id BIGINT NOT NULL,
    payment_method_id BIGINT NOT NULL,
    store VARCHAR(100),
    memo VARCHAR(500),
    satisfaction SMALLINT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 外部キー
    CONSTRAINT fk_expenses_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id),

    CONSTRAINT fk_expenses_payment_method
        FOREIGN KEY (payment_method_id)
        REFERENCES payment_methods(id),

    -- 満足度は1～5のみ許可
    CONSTRAINT chk_expenses_satisfaction
        CHECK (satisfaction BETWEEN 1 AND 5),

    -- 支出金額は0円より大きい値のみ許可
    CONSTRAINT chk_expenses_amount
        CHECK (amount > 0)
);

COMMENT ON TABLE expenses IS '支出管理テーブル';

COMMENT ON COLUMN expenses.id IS '支出ID';
COMMENT ON COLUMN expenses.expense_date IS '支出日';
COMMENT ON COLUMN expenses.amount IS '支出金額';
COMMENT ON COLUMN expenses.category_id IS 'カテゴリID';
COMMENT ON COLUMN expenses.payment_method_id IS '支払い方法ID';
COMMENT ON COLUMN expenses.store IS '店舗名';
COMMENT ON COLUMN expenses.memo IS 'メモ';
COMMENT ON COLUMN expenses.satisfaction IS '支出に対する満足度';
COMMENT ON COLUMN expenses.created_at IS '作成日時';
COMMENT ON COLUMN expenses.updated_at IS '更新日時';

**********************************************
