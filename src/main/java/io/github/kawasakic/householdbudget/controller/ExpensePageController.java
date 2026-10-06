package io.github.kawasakic.householdbudget.controller;

import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    // GET /expenses にアクセスしたとき、支出一覧画面を表示する
    @GetMapping
    public String showList(Model model) {
        // データベースに登録されている支出を取得する
        model.addAttribute("expenses", expenseService.findAll());

        // カテゴリIDからカテゴリ名を引けるMapを作る
        Map<Long, String> categoryNames = categoryService.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // 支払方法IDから支払方法名を引けるMapを作る
        Map<Long, String> paymentMethodNames = paymentMethodService.findAll().stream()
                .collect(Collectors.toMap(PaymentMethod::getId, PaymentMethod::getName));

        // Thymeleafの一覧画面から参照できるようにModelへ渡す
        model.addAttribute("categoryNames", categoryNames);
        model.addAttribute("paymentMethodNames", paymentMethodNames);

        // src/main/resources/templates/expenses/list.html を表示する
        return "expenses/list";
    }
 }