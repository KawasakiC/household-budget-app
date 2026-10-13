package io.github.kawasakic.householdbudget.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.github.kawasakic.householdbudget.Budget;
import io.github.kawasakic.householdbudget.Category;
import io.github.kawasakic.householdbudget.Expense;
import io.github.kawasakic.householdbudget.SatisfactionComment;
import io.github.kawasakic.householdbudget.service.BudgetService;
import io.github.kawasakic.householdbudget.service.CategoryService;
import io.github.kawasakic.householdbudget.service.ExpenseService;


@Controller
public class TopPageController {
	
    // 予算データの検索を担当するService
    private final BudgetService budgetService;
    
    // 支出データの検索を担当するService
    private final ExpenseService expenseService;
    
    // カテゴリ一覧を取得するService
    private final CategoryService categoryService;
    

    // SpringからServiceを受け取る
    public TopPageController(
            BudgetService budgetService,
            ExpenseService expenseService,
            CategoryService categoryService) {
        this.budgetService = budgetService;
        this.expenseService = expenseService;
        this.categoryService = categoryService;
    }
	
	@GetMapping("/")
	public String showTopPage(Model model) {
		// 今月の年月を取得する
		YearMonth currentMonth = YearMonth.now();
			    
	    // 今月の予算を取得する
	    List<Budget> budgets = budgetService.findByMonth(currentMonth);

	    // 確定済み予算があるか確認する
	    boolean hasConfirmedBudget = budgets.stream()
	            .anyMatch(Budget::isConfirmed);

	    // 確定済み予算の合計を計算する
	    BigDecimal totalBudget = budgets.stream()
	            .filter(Budget::isConfirmed)
	            .map(Budget::getBudgetAmount)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);

	    // 今月の支出を取得して合計する
	    List<Expense> expenses = expenseService.findByMonth(currentMonth);

	    BigDecimal totalExpense = expenses.stream()
	            .map(Expense::getAmount)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);

	    // 確定済み予算があれば「予算 − 支出」、なければnullにする
	    BigDecimal remainingAmount = hasConfirmedBudget
	            ? totalBudget.subtract(totalExpense)
	            : null;
	    

	    // 今月の残り日数を取得する
	    LocalDate today = LocalDate.now();
	    LocalDate lastDay = YearMonth.from(today).atEndOfMonth();

	    long remainingDays = ChronoUnit.DAYS.between(today, lastDay) + 1;
	    
        // カテゴリをすべて取得する
        List<Category> categories = categoryService.findAll();
        
        // カテゴリIDごとの支出金額を合計する
        Map<Long, BigDecimal> categoryTotals = new HashMap<>();
        
        // すべてのカテゴリを0円で初期化する
        for (Category category : categories) {
            categoryTotals.put(category.getId(), BigDecimal.ZERO);
        }
        
        // 支出があるカテゴリには金額を加算する
        for (Expense expense : expenses) {
            categoryTotals.merge(
                    expense.getCategoryId(),
                    expense.getAmount(),
                    BigDecimal::add
            );
        }
        
        // 今月の予算を取得する
        List<Budget> monthlyBudgets = budgetService.findByMonth(currentMonth);

        // カテゴリIDから今月の予算額を取り出せるMapを作る
        Map<Long, BigDecimal> monthlyBudgetAmounts = new HashMap<>();

        for (Budget budget : monthlyBudgets) {
            // 未確定の予算は残金計算に使わない
            if (budget.isConfirmed()) {
                monthlyBudgetAmounts.put(
                        budget.getCategoryId(),
                        budget.getBudgetAmount()
                );
            }
        }
        
        // カテゴリごとの残金を入れるMapを用意する
        Map<Long, BigDecimal> categoryRemaining = new HashMap<>();

        for (Category category : categories) {
            // カテゴリに予算額が設定されている場合だけ残金を計算する
        	BigDecimal budget = monthlyBudgetAmounts.get(category.getId());

            if (budget != null) {
                // そのカテゴリの支出合計を取得する。支出がなければ0円とする
                BigDecimal spent = categoryTotals.getOrDefault(
                        category.getId(),
                        BigDecimal.ZERO
                );

                // 予算額から支出額を引いて残金を保存する
                categoryRemaining.put(
                        category.getId(),
                        budget.subtract(spent)
                );
            }
        }
        
        // 食費の残金を取得
        BigDecimal foodRemaining = categories.stream()
                .filter(category -> "食費".equals(category.getName()))
                .findFirst()
                .map(category -> categoryRemaining.get(category.getId()))
                .orElse(BigDecimal.ZERO);
        
        // 今月の残り土曜日の数を取得
        long remainingSaturdays = today
                .datesUntil(currentMonth.atEndOfMonth().plusDays(1))
                .filter(date -> date.getDayOfWeek() == DayOfWeek.SATURDAY)
                .count();
        
        BigDecimal weeklyFoodBudget = remainingSaturdays == 0
                ? foodRemaining
                : foodRemaining.divide(
                        BigDecimal.valueOf(remainingSaturdays),
                        0,
                        RoundingMode.DOWN
                );
        
        // 今月の満足度
        List<Expense> monthlyExpenses = expenseService.findByMonth(currentMonth);

        Integer satisfactionPercent = null;
        
        // 今月全体の満足度の%を取得
        if (!monthlyExpenses.isEmpty()) {
            int totalScore = monthlyExpenses.stream()
                    .mapToInt(Expense::getSatisfaction)
                    .sum();

            satisfactionPercent = (int) Math.round(
                    totalScore * 100.0 / (monthlyExpenses.size() * 5)
            );
        }
        
        // 満足度コメントを渡す
        if (satisfactionPercent != null) {
            String commentId =
                    SatisfactionComment.findIdByPercent(satisfactionPercent);
            String comment = SatisfactionComment.findById(commentId);

            model.addAttribute("satisfactionComment", comment);
        }
        
        // ちょっと気になるカテゴリ
        String lowestCategoryName = null;
        // 60%未満だけを対象にする
        int lowestPercent = 60;

        // ちょっと気になるカテゴリは60%未満の中で最下位のものだけ取得
        for (Category category : categories) {
            List<Expense> categoryExpenses = monthlyExpenses.stream()
                    .filter(expense ->
                            expense.getCategoryId().equals(category.getId()))
                    .toList();

            if (categoryExpenses.isEmpty()) {
                continue;
            }

            int totalScore = categoryExpenses.stream()
                    .mapToInt(Expense::getSatisfaction)
                    .sum();

            int percent = (int) Math.round(
                    totalScore * 100.0 / (categoryExpenses.size() * 5)
            );

            // これまでの最低値より低ければ更新
            if (percent < lowestPercent) {
                lowestPercent = percent;
                lowestCategoryName = category.getName();
            }
        }
	    
	    // HTMLに渡す
	    model.addAttribute("currentMonth", currentMonth);
	    model.addAttribute("totalExpense", totalExpense);
	    model.addAttribute("remainingAmount", remainingAmount);
	    model.addAttribute("remainingDays", remainingDays);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryRemaining", categoryRemaining);
        model.addAttribute("weeklyFoodBudget", weeklyFoodBudget);
        model.addAttribute("satisfactionPercent", satisfactionPercent);
        model.addAttribute("lowestCategoryName", lowestCategoryName);
        model.addAttribute("lowestCategoryPercent", lowestPercent);

	 
	    return "top/index";
	}
}
