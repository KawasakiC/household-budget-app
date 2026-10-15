package io.github.kawasakic.householdbudget.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
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
import io.github.kawasakic.householdbudget.service.TopPageService;


@Controller
public class TopPageController {
	
    // 予算データの検索を担当するService
    private final BudgetService budgetService;
    
    // 支出データの検索を担当するService
    private final ExpenseService expenseService;
    
    // カテゴリ一覧を取得するService
    private final CategoryService categoryService;
    
    // トップページ情報を取得するService
    private final TopPageService topPageService;

    // SpringからServiceを受け取る
    public TopPageController(
            BudgetService budgetService,
            ExpenseService expenseService,
            CategoryService categoryService,
            TopPageService topPageService) {
    	this.budgetService = budgetService;
        this.expenseService = expenseService;
        this.categoryService = categoryService;
        this.topPageService = topPageService;
    }
    
	@GetMapping("/")
	public String showTopPage(Model model) {
	    // 今月の年月を取得する
	    YearMonth currentMonth = YearMonth.now();
		
		// 今月の予算を取得する
	    List<Budget> budgets = budgetService.findByMonth(currentMonth);

	    // 今月の支出を取得する
	    List<Expense> expenses = expenseService.findByMonth(currentMonth);

	    // 月の支出金額を合計する
	    BigDecimal totalExpense =
	            expenseService.calculateTotalAmount(expenses);

	    // 確定済み予算がある場合、予算合計から支出合計を引いて残金を計算する
	    BigDecimal remainingAmount =
	            budgetService.calculateRemainingAmount(budgets, totalExpense);	    

	    // 今月の残り日数を取得する
	    LocalDate today = LocalDate.now();
	    long remainingDays = topPageService.calculateRemainingDays(today);
	    
        // カテゴリをすべて取得する
        List<Category> categories = categoryService.findAll();
        
        // カテゴリIDごとの支出金額を計算する
        Map<Long, BigDecimal> categoryTotals =
                expenseService.calculateCategoryTotals(categories, expenses);
                
        // 今月の予算を取得する
        List<Budget> monthlyBudgets = budgetService.findByMonth(currentMonth);

        // カテゴリごとの残金を計算する
        Map<Long, BigDecimal> categoryRemaining =
                budgetService.calculateCategoryRemaining(
                        categories,
                        monthlyBudgets,
                        categoryTotals
                );
        
        // 食費の残金を、今月の残り土曜日数で割って週予算を計算する
        BigDecimal weeklyFoodBudget =
                topPageService.calculateWeeklyFoodBudget(
                        categories,
                        categoryRemaining,
                        today
                );
                
        // 今月全体の満足度を計算する
        Integer satisfactionPercent = null;
        satisfactionPercent = topPageService.calculateSatisfactionPercent(expenses);
        
        // 満足度コメントを渡す
        if (satisfactionPercent != null) {
            String commentId =
                    SatisfactionComment.findIdByPercent(satisfactionPercent);
            String comment = SatisfactionComment.findById(commentId);

            model.addAttribute("satisfactionComment", comment);
        }
        
        // 60%未満で最も満足度が低いカテゴリを取得する。
        TopPageService.LowestCategoryResult result =
                topPageService.findLowestSatisfactionCategories(
                        categories,
                        expenses
                );	    
        
        // HTMLが参照する名前でModelに登録する
        List<String> lowestCategoryNames = result.names();
        Integer lowestPercent = result.percent();
        
	    // HTMLに渡す
	    model.addAttribute("currentMonth", currentMonth);
	    model.addAttribute("totalExpense", totalExpense);
	    model.addAttribute("remainingAmount", remainingAmount);
	    model.addAttribute("remainingDays", remainingDays);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryRemaining", categoryRemaining);
        model.addAttribute("weeklyFoodBudget", weeklyFoodBudget);
        model.addAttribute("satisfactionPercent", satisfactionPercent);
        model.addAttribute("lowestCategoryNames", lowestCategoryNames);
        model.addAttribute("lowestCategoryNamesText", String.join("、", lowestCategoryNames));
        model.addAttribute("lowestCategoryPercent", lowestPercent);
	 
	    return "top/index";
	}
}
