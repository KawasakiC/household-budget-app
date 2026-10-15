const amountInputs = document.querySelectorAll(".budget-amount");
const totalElement = document.getElementById("totalBudget");

// 入力中に合計を計算する
function updateTotal() {
    let total = 0;
    let hasValue = false;

    amountInputs.forEach(input => {
        if (input.value !== "") {
            total += Number(input.value);
            hasValue = true;
        }
    });

    totalElement.textContent = hasValue
        ? `${new Intl.NumberFormat("ja-JP").format(total)}円`
        : "-";
}

amountInputs.forEach(input => {
    input.addEventListener("input", updateTotal);
});

// 画面を開いたときにも、入力済みの金額から合計を表示する
updateTotal();

// 予算欄の入力チェック
const budgetForm = document.getElementById("budget-form");
const budgetInputs = document.querySelectorAll(".budget-amount");

budgetForm.addEventListener("submit", (event) => {
	// 自動計算ボタンの場合は予算額の入力チェックをしない
	if (event.submitter?.value === "calculate") {
	  return;
	}
	
	let hasError = false;

	budgetInputs.forEach((input) => {
    	input.setCustomValidity("");

		if (input.value === "") {
			input.setCustomValidity("予算額を入力してください。");
			hasError = true;
		} else if (Number(input.value) < 0) {
			input.setCustomValidity("予算額は0円以上で入力してください。");
	  		hasError = true;
  		}
	});

	if (hasError) {
	    event.preventDefault();

    	// エラーになった欄のメッセージを表示
		budgetForm.reportValidity();
	}
});

// 入力を修正したら、独自エラーを解除する
budgetInputs.forEach((input) => {
  input.addEventListener("input", () => {
    input.setCustomValidity("");
  });
});