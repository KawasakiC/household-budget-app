const amountInputs = document.querySelectorAll(".budget-amount");
const totalElement = document.getElementById("totalBudget");

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