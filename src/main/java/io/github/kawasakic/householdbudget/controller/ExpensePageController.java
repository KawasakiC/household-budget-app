package io.github.kawasakic.householdbudget.controller;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.github.kawasakic.householdbudget.Category;
import io.github.kawasakic.householdbudget.Expense;
import io.github.kawasakic.householdbudget.PaymentMethod;
import io.github.kawasakic.householdbudget.service.CategoryService;
import io.github.kawasakic.householdbudget.service.ExpenseService;
import io.github.kawasakic.householdbudget.service.PaymentMethodService;

@Controller
@RequestMapping("/expenses")
public class ExpensePageController {

    // 支出データの登録・検索を担当するService
    private final ExpenseService expenseService;

    // カテゴリ一覧を取得するService
    private final CategoryService categoryService;

    // 支払方法一覧を取得するService
    private final PaymentMethodService paymentMethodService;

    // Springから3つのServiceを受け取る
    public ExpensePageController(
            ExpenseService expenseService,
            CategoryService categoryService,
            PaymentMethodService paymentMethodService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
        this.paymentMethodService = paymentMethodService;
    }

    // GET /expenses/new にアクセスしたとき、支出登録画面を表示する
    @GetMapping("/new")
    public String showRegisterForm(Model model) {
        // 入力フォームに結び付ける空の支出データを用意する
        model.addAttribute("expense", new Expense());

        // フォームのカテゴリ選択肢をデータベースから取得する
        model.addAttribute("categories", categoryService.findAll());

        // フォームの支払方法選択肢をデータベースから取得する
        model.addAttribute("paymentMethods", paymentMethodService.findAll());

        // src/main/resources/templates/expenses/form.html を表示する
        return "expenses/form";
    }

    // GET /expenses/edit にアクセスしたとき、IDに紐づいた支出登録画面を表示する
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,  Model model, RedirectAttributes redirectAttributes) {
        // 入力フォームに結び付けるID指定の支出データを呼び出す
    	Optional<Expense> result = expenseService.findById(id);
    	
    	// 指定した支出がデータなしの場合、エラーメッセージを支出一覧画面に出力する
    	if (result.isEmpty()) {
    	    redirectAttributes.addFlashAttribute(
    	            "errorMessage",
    	            "指定された支出は見つかりませんでした。"
    	    );
    	    return "redirect:/expenses";
    	}
    	
    	// 取得した支出データを支出登録画面にセットする。
        model.addAttribute("expense", result.get());

        // フォームのカテゴリ選択肢をデータベースから取得する
        model.addAttribute("categories", categoryService.findAll());

        // フォームの支払方法選択肢をデータベースから取得する
        model.addAttribute("paymentMethods", paymentMethodService.findAll());

        // src/main/resources/templates/expenses/form.html を表示する
        return "expenses/form";
    }

    // POST /expenses にフォームの入力内容が送信されたとき実行する
    @PostMapping
    public String register(
            @Valid @ModelAttribute("expense") Expense expense,
            BindingResult bindingResult,
            Model model) {

        // 入力エラーがあれば、選択肢を再設定してフォームに戻す
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("paymentMethods", paymentMethodService.findAll());
            return "expenses/form";
        }

        // 入力エラーがなければ、支出をデータベースに登録する
        expenseService.save(expense);

        // 登録後は支出一覧画面へ移動する
        return "redirect:/expenses";
    }
    
    // POST /expenses/{id}/deleteに削除フォームが送信されたときに実行する
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
    	// URLから受け取ったIDの支出を削除する
    	expenseService.deleteById(id);
    	
    	//削除後は支出一覧へ戻る
    	return "redirect:/expenses";
    }

    // GET /expenses にアクセスしたとき、支出一覧画面を表示する
    @GetMapping
    public String showList(
            @RequestParam(required = false) String month,Model model) {
        // URLから月が指定されていればその月を使い、なければ今月を選択する
        YearMonth selectedMonth =
                (month == null || month.isBlank())
                        ? YearMonth.now()
                        : YearMonth.parse(month);

        // 選択した月の支出を取得する
        List<Expense> expenses = expenseService.findByMonth(selectedMonth);

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
        
        // カテゴリごとの残金を入れるMapを用意する
        Map<Long, BigDecimal> categoryRemaining = new HashMap<>();

        for (Category category : categories) {
            // カテゴリに予算額が設定されている場合だけ残金を計算する
            BigDecimal budget = category.getCalculationAmount();

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
        
        // 月の支出金額を合計する
        BigDecimal monthlyTotal = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 一覧とカテゴリごとの合計、月合計を画面に渡す
        model.addAttribute("expenses", expenses);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryTotals", categoryTotals);
        model.addAttribute("categoryRemaining", categoryRemaining);
        model.addAttribute("monthlyTotal", monthlyTotal);
        
        // カテゴリIDからカテゴリ名を引けるMapを作る
        Map<Long, String> categoryNames = categoryService.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // 支払方法IDから支払方法名を引けるMapを作る
        Map<Long, String> paymentMethodNames = paymentMethodService.findAll().stream()
                .collect(Collectors.toMap(PaymentMethod::getId, PaymentMethod::getName));

        // Thymeleafの一覧画面から参照できるようにModelへ渡す
        model.addAttribute("categoryNames", categoryNames);
        model.addAttribute("paymentMethodNames", paymentMethodNames);
        
        // 今月の前後12か月を選択肢として用意する
        List<YearMonth> months = new ArrayList<>();
        for (int i = -12; i <= 12; i++) {
            months.add(selectedMonth.plusMonths(i));
        }

        // 画面で選択肢と選択中の月を使えるように渡す
        model.addAttribute("months", months);
        model.addAttribute("selectedMonth", selectedMonth.toString());

         
        // src/main/resources/templates/expenses/list.html を表示する
        return "expenses/list";
    }
 }