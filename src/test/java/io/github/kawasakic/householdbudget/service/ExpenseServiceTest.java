package io.github.kawasakic.householdbudget.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.kawasakic.householdbudget.Category;
import io.github.kawasakic.householdbudget.Expense;
import io.github.kawasakic.householdbudget.repository.ExpenseRepository;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Spy
    @InjectMocks
    private ExpenseService expenseService;

    @Mock
    private ExpenseRepository expenseRepository;

    private final YearMonth targetMonth = YearMonth.of(2026, 10);

    private List<Expense> findTop3(List<Expense> expenses) {
        doReturn(expenses)
                .when(expenseService)
                .findByMonth(targetMonth);

        return expenseService.findTop3GoodExpensesByMonth(targetMonth);
    }

    @Test
    void EXP001_満足度2の支出は結果に含まれない() {
        Expense low = createExpense(5, 2);
        Expense good = createExpense(6, 4);

        List<Expense> actual = findTop3(List.of(low, good));

        assertEquals(List.of(good), actual);
    }

    @Test
    void EXP002_満足度3以上の支出が結果に含まれる() {
        Expense five = createExpense(5, 5);
        Expense four = createExpense(6, 4);
        Expense three = createExpense(7, 3);

        List<Expense> actual = findTop3(List.of(three, five, four));

        assertEquals(List.of(five, four, three), actual);
    }

    @Test
    void EXP003_満足度順で並び同じ満足度なら日付の新しい順にする() {
        Expense five = createExpense(5, 5);
        Expense fourOn10th = createExpense(10, 4);
        Expense fourOn20th = createExpense(20, 4);
        Expense three = createExpense(25, 3);

        List<Expense> actual =
                findTop3(List.of(fourOn10th, three, five, fourOn20th));

        assertEquals(List.of(five, fourOn20th, fourOn10th), actual);
    }

    @Test
    void EXP004_条件に合う支出が4件以上なら上位3件だけ返す() {
        List<Expense> actual = findTop3(List.of(
                createExpense(5, 5),
                createExpense(10, 4),
                createExpense(20, 4),
                createExpense(25, 3)
        ));

        assertEquals(3, actual.size());
    }

    @Test
    void EXP005_条件に合う支出が2件ならその2件を返す() {
        Expense four = createExpense(5, 4);
        Expense three = createExpense(6, 3);

        List<Expense> actual = findTop3(List.of(three, four));

        assertEquals(List.of(four, three), actual);
    }

    @Test
    void EXP006_支出がなければ空のリストを返す() {
        List<Expense> actual = findTop3(List.of());

        assertTrue(actual.isEmpty());
    }

    @Test
    void EXP007_指定月を引数にして支出を取得する() {
        Expense octoberExpense = createExpense(5, 3);

        List<Expense> actual = findTop3(List.of(octoberExpense));

        assertEquals(List.of(octoberExpense), actual);
        verify(expenseService).findByMonth(targetMonth);
    }

    private Expense createExpense(int day, int satisfaction) {
        Expense expense = new Expense();
        expense.setExpenseDate(LocalDate.of(2026, 10, day));
        expense.setSatisfaction((short) satisfaction);
        return expense;
    }
    
    @Test
    void EXP008_支出がないカテゴリは0円で返す() {
        Category food = createCategory(1L, "食費");

        Map<Long, BigDecimal> actual =
                expenseService.calculateCategoryTotals(
                        List.of(food),
                        List.of()
                );

        assertEquals(0, BigDecimal.ZERO.compareTo(actual.get(1L)));
    }

    @Test
    void EXP009_同じカテゴリの支出金額を合計する() {
        Category food = createCategory(1L, "食費");

        Expense firstExpense = createExpense(1L, "12000");
        Expense secondExpense = createExpense(1L, "8000");

        Map<Long, BigDecimal> actual =
                expenseService.calculateCategoryTotals(
                        List.of(food),
                        List.of(firstExpense, secondExpense)
                );

        assertEquals(0, new BigDecimal("20000").compareTo(actual.get(1L)));
    }

    private Expense createExpense(Long categoryId, String amount) {
        Expense expense = new Expense();
        expense.setCategoryId(categoryId);
        expense.setAmount(new BigDecimal(amount));
        return expense;
    }
    
    private Category createCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }
    
    @Test
    void EXP010_2件の支出金額を合計する() {
        Expense firstExpense = createExpense(1L, "12000");
        Expense secondExpense = createExpense(1L, "8000");

        BigDecimal actual = expenseService.calculateTotalAmount(
                List.of(firstExpense, secondExpense)
        );

        assertEquals(0, new BigDecimal("20000").compareTo(actual));
    }

    @Test
    void EXP011_1件の支出金額を返す() {
        Expense expense = createExpense(1L, "5000");

        BigDecimal actual = expenseService.calculateTotalAmount(
                List.of(expense)
        );

        assertEquals(0, new BigDecimal("5000").compareTo(actual));
    }

    @Test
    void EXP012_支出が0件なら0円を返す() {
        BigDecimal actual = expenseService.calculateTotalAmount(List.of());

        assertEquals(0, BigDecimal.ZERO.compareTo(actual));
    }
}
