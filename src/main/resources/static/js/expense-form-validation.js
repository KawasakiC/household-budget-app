// 支出日のエラーメッセージ
const expenseDate = document.getElementById("expenseDate");

expenseDate.addEventListener("invalid", () => {
  if (expenseDate.validity.valueMissing) {
    expenseDate.setCustomValidity("支出日を入力してください。");
  }
});

//入力したらエラーメッセージを解除
expenseDate.addEventListener("input", () => {
  expenseDate.setCustomValidity("");
});

// 金額のエラーメッセージ
const amount = document.getElementById("amount");

amount.addEventListener("invalid", () => {
  if (amount.validity.valueMissing) {
    amount.setCustomValidity("金額を入力してください。");
  } else if (amount.validity.rangeUnderflow) {
    amount.setCustomValidity("金額は0円より大きい値を入力してください。");
  } else if (amount.validity.stepMismatch) {
    amount.setCustomValidity("金額は整数で入力してください。");
  }
});

//入力したらエラーメッセージを解除
amount.addEventListener("input", () => {
  amount.setCustomValidity("");
});

// カテゴリのエラーメッセージ
const categoryId = document.getElementById("categoryId");

categoryId.addEventListener("invalid", () => {
  if (categoryId.validity.valueMissing) {
    categoryId.setCustomValidity("カテゴリを選択してください。");
  }
});

//入力したらエラーメッセージを解除
categoryId.addEventListener("input", () => {
  categoryId.setCustomValidity("");
});

// 支払い方法のエラーメッセージ
const paymentMethodId = document.getElementById("paymentMethodId");

paymentMethodId.addEventListener("invalid", () => {
  if (paymentMethodId.validity.valueMissing) {
    paymentMethodId.setCustomValidity("支払い方法を選択してください。");
  }
});

//入力したらエラーメッセージを解除
paymentMethodId.addEventListener("input", () => {
  paymentMethodId.setCustomValidity("");
});

// 満足度のエラーメッセージ
const satisfaction = document.getElementById("satisfaction");

satisfaction.addEventListener("invalid", () => {
  if (satisfaction.validity.valueMissing) {
    satisfaction.setCustomValidity("満足度を選択してください。");
  }
});

// 入力したらエラーメッセージを解除
satisfaction.addEventListener("input", () => {
  satisfaction.setCustomValidity("");
});

// 店舗名の文字数チェック
const store = document.getElementById("store");
const formStore = store.closest("form");

formStore.addEventListener("submit", (event) => {
  if (store.value.length > 100) {
    event.preventDefault(); // サーバーへの送信を止める
    store.setCustomValidity("店舗名は100文字以内で入力してください。");
    store.reportValidity(); // ブラウザーのエラー表示を出す
  } else {
    store.setCustomValidity("");
  }
});

// 入力を修正したら、次回送信できるようエラーを解除
store.addEventListener("input", () => {
  store.setCustomValidity("");
});

// メモの文字数チェック
const memo = document.getElementById("memo");
const formMemo = memo.closest("form");

formMemo.addEventListener("submit", (event) => {
  if (memo.value.length > 250) {
    event.preventDefault(); // サーバーへの送信を止める
    memo.setCustomValidity("メモは250文字以内で入力してください。");
    memo.reportValidity(); // ブラウザーのエラー表示を出す
  } else {
    memo.setCustomValidity("");
  }
});

// 入力を修正したら、次回送信できるようエラーを解除
memo.addEventListener("input", () => {
  memo.setCustomValidity("");
});
