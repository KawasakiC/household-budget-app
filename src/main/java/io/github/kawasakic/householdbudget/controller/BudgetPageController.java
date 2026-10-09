package io.github.kawasakic.householdbudget.controller;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.kawasakic.householdbudget.Budget;
import io.github.kawasakic.householdbudget.Category;
import io.github.kawasakic.householdbudget.form.BudgetForm;
import io.github.kawasakic.householdbudget.service.BudgetService;
import io.github.kawasakic.householdbudget.service.CategoryService;

@Controller
@RequestMapping("/budgets")
public class BudgetPageController {
	
    // 予算データの登録・検索を担当するService
    private final BudgetService budgetService;

    // カテゴリ一覧を取得するService
    private final CategoryService categoryService;
    
    // Springから2つのServiceを受け取る
    public BudgetPageController(
            BudgetService budgetService,
            CategoryService categoryService) {
        this.budgetService = budgetService;
        this.categoryService = categoryService;
    }

    // GET /expenses にアクセスしたとき、予算登録画面を表示する
    @GetMapping
    public String showBudgetRegist(
            @RequestParam(required = false) String month,Model model) {
    	
        // URLから月が指定されていればその月を使い、なければ今月を選択する
        YearMonth selectedMonth =
                (month == null || month.isBlank())
                        ? YearMonth.now()
                        : YearMonth.parse(month);
        
        // 前後12か月を選択肢として用意する
        List<YearMonth> months = new ArrayList<>();
        for (int i = -12; i <= 12; i++) {
            months.add(selectedMonth.plusMonths(i));
        }

        // 画面で選択肢と選択中の月を使えるように渡す
        model.addAttribute("months", months);
        model.addAttribute("selectedMonth", selectedMonth.toString());
        
        // カテゴリをすべて取得する
        List<Category> categories = categoryService.findAll();
        
        model.addAttribute("categories", categories);
        
        // 画面入力用のフォームを作る
        BudgetForm budgetForm = new BudgetForm();
        budgetForm.setMonth(selectedMonth.toString());

        // 選択した月にすでに保存されている予算を取得する
        List<Budget> savedBudgets = budgetService.findByMonth(selectedMonth);

        // カテゴリIDをキーにして、保存済み予算を探せるようにする
        Map<Long, Budget> savedBudgetMap = new HashMap<>();
        for (Budget budget : savedBudgets) {
            savedBudgetMap.put(budget.getCategoryId(), budget);
        }

        // カテゴリごとにフォームの行を作る
        List<BudgetForm.BudgetRow> budgetRows = new ArrayList<>();

        for (Category category : categories) {
            BudgetForm.BudgetRow row = new BudgetForm.BudgetRow();

            // カテゴリ名とIDを画面に表示・送信するためにセット
            row.setCategoryId(category.getId());
            row.setCategoryName(category.getName());

            // その月の保存済み予算があれば、入力欄に表示する
            Budget savedBudget = savedBudgetMap.get(category.getId());
            if (savedBudget != null) {
                row.setBudgetAmount(savedBudget.getBudgetAmount());
            }

            budgetRows.add(row);
        }

        // 作った行をフォームに入れて、HTMLから使えるようにModelへ渡す
        budgetForm.setBudgets(budgetRows);
        model.addAttribute("budgetForm", budgetForm);
        
        // カテゴリごとの予算額を合計する
        BigDecimal totalBudget = BigDecimal.ZERO;

        for (BudgetForm.BudgetRow row : budgetRows) {
            if (row.getBudgetAmount() != null) {
                totalBudget = totalBudget.add(row.getBudgetAmount());
            }
        }

        // HTMLで合計を表示できるように渡す
        model.addAttribute("totalBudget", totalBudget);
    	
        // src/main/resources/templates/budgets/form.html を表示する
        return "budgets/form";
 
    }
    
    // 自動計算ボタンが押されたときに実行する
    @PostMapping("/calculate")
    public String calculate(
            @ModelAttribute("budgetForm") BudgetForm budgetForm,
            Model model) {

        // フォームから受け取った年月をYearMonthにする
        YearMonth selectedMonth = YearMonth.parse(budgetForm.getMonth());

        // カテゴリをDBから取得する
        List<Category> categories = categoryService.findAll();

        // Serviceにカテゴリごとの予算計算を依頼する
        Map<Long, BigDecimal> calculatedAmounts =
                budgetService.calculateMonthlyAmounts(selectedMonth, categories);

        // DBから取得したカテゴリ情報を使って、画面用の行を作り直す
        List<BudgetForm.BudgetRow> rows = new ArrayList<>();

        for (Category category : categories) {
            BudgetForm.BudgetRow row = new BudgetForm.BudgetRow();
            row.setCategoryId(category.getId());
            row.setCategoryName(category.getName());
            row.setBudgetAmount(calculatedAmounts.get(category.getId()));
            rows.add(row);
        }

        budgetForm.setBudgets(rows);

        // HTMLで使う情報をModelに戻す
        model.addAttribute("budgetForm", budgetForm);
        model.addAttribute("selectedMonth", selectedMonth.toString());

        List<YearMonth> months = new ArrayList<>();
        YearMonth thisMonth = YearMonth.now();

        // 前後12か月を選択肢として用意する
        for (int i = -12; i <= 12; i++) {
            months.add(thisMonth.plusMonths(i));
        }
        model.addAttribute("months", months);

        // 合計金額を計算する
        BigDecimal totalBudget = BigDecimal.ZERO;
        for (BudgetForm.BudgetRow row : rows) {
            totalBudget = totalBudget.add(row.getBudgetAmount());
        }
        model.addAttribute("totalBudget", totalBudget);

        return "budgets/form";
    }
    
    // 予算確定ボタンが押されたときに実行する
    @PostMapping("/confirm")
    public String confirm(
            @Valid @ModelAttribute("budgetForm") BudgetForm budgetForm,
            BindingResult bindingResult,
            Model model) {
    	
        List<Category> categories = categoryService.findAll();

        // カテゴリIDからカテゴリ名を引けるようにする
        Map<Long, String> categoryNames = new HashMap<>();
        for (Category category : categories) {
            categoryNames.put(category.getId(), category.getName());
        }

        // 入力行にカテゴリ名を戻す
        for (BudgetForm.BudgetRow row : budgetForm.getBudgets()) {
            row.setCategoryName(categoryNames.get(row.getCategoryId()));
        }

        YearMonth selectedMonth = YearMonth.parse(budgetForm.getMonth());

        if (bindingResult.hasErrors()) {
            // エラー時も画面に必要な月の選択肢を用意する
            List<YearMonth> months = new ArrayList<>();
            YearMonth thisMonth = YearMonth.now();

            for (int i = -12; i <= 12; i++) {
                months.add(thisMonth.plusMonths(i));
            }

            model.addAttribute("months", months);
            model.addAttribute("selectedMonth", selectedMonth.toString());

            return "budgets/form";
        }

        // カテゴリIDをキー、入力された予算額を値にしたMapを作る
        Map<Long, BigDecimal> budgetAmounts = new LinkedHashMap<>();

        for (BudgetForm.BudgetRow row : budgetForm.getBudgets()) {
            budgetAmounts.put(row.getCategoryId(), row.getBudgetAmount());
        }

        // Serviceに保存を依頼する
        budgetService.confirmBudgets(selectedMonth, budgetAmounts);

        // 保存後は同じ月の画面へ戻る
        return "redirect:/budgets?month=" + selectedMonth;
    }
}
