package io.github.kawasakic.householdbudget.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.kawasakic.householdbudget.Budget;
import io.github.kawasakic.householdbudget.Category;

public class BudgetServiceTest {
	
	private BudgetService budgetService;
	
	@BeforeEach
	void setUp() {
	    budgetService = new BudgetService(null);
	}
	
	@Test
	void BUD001_2026年10月の週予算3000円は15000円() {
	    Category category = createCategory(1L, "WEEKLY", "3000");

	    Map<Long, BigDecimal> actual = budgetService.calculateMonthlyAmounts(
	            YearMonth.of(2026, 10),
	            List.of(category)
	    );

	    assertEquals(0, new BigDecimal("15000").compareTo(actual.get(1L)));
	}

	@Test
	void BUD002_2026年11月の週予算3000円は12000円() {
	    Category category = createCategory(1L, "WEEKLY", "3000");

	    Map<Long, BigDecimal> actual = budgetService.calculateMonthlyAmounts(
	            YearMonth.of(2026, 11),
	            List.of(category)
	    );

	    assertEquals(0, new BigDecimal("12000").compareTo(actual.get(1L)));
	}

	@Test
	void BUD003_FIXEDは設定額をそのまま返す() {
	    Category category = createCategory(1L, "FIXED", "20000");

	    Map<Long, BigDecimal> actual = budgetService.calculateMonthlyAmounts(
	            YearMonth.of(2026, 10),
	            List.of(category)
	    );

	    assertEquals(0, new BigDecimal("20000").compareTo(actual.get(1L)));
	}

	@Test
	void BUD004_計算結果をカテゴリIDに対応させて返す() {
	    Category weekly = createCategory(1L, "WEEKLY", "3000");
	    Category fixed = createCategory(2L, "FIXED", "20000");

	    Map<Long, BigDecimal> actual = budgetService.calculateMonthlyAmounts(
	            YearMonth.of(2026, 10),
	            List.of(weekly, fixed)
	    );

	    assertEquals(2, actual.size());
	    assertEquals(0, new BigDecimal("15000").compareTo(actual.get(1L)));
	    assertEquals(0, new BigDecimal("20000").compareTo(actual.get(2L)));
	}

	@Test
	void BUD005_未対応の計算方法なら例外が発生する() {
	    Category category = createCategory(1L, "OTHER", "1000");

	    assertThrows(
	            IllegalArgumentException.class,
	            () -> budgetService.calculateMonthlyAmounts(
	                    YearMonth.of(2026, 10),
	                    List.of(category)
	            )
	    );
	}

	@Test
	void BUD006_カテゴリ一覧が空なら空のMapを返す() {
	    Map<Long, BigDecimal> actual = budgetService.calculateMonthlyAmounts(
	            YearMonth.of(2026, 10),
	            List.of()
	    );

	    assertTrue(actual.isEmpty());
	}

	private Category createCategory(
	        Long id,
	        String calculationMethod,
	        String calculationAmount) {

	    Category category = new Category();
	    category.setId(id);
	    category.setCalculationMethod(calculationMethod);
	    category.setCalculationAmount(new BigDecimal(calculationAmount));
	    return category;
	}
	
	@Test
	void BUD007_確定済み予算から支出合計を引いた残金を返す() {
	    Category category = createCategory(1L);
	    Budget budget = createBudget(1L, "40000", true);
	    Map<Long, BigDecimal> categoryTotals =
	            Map.of(1L, new BigDecimal("20000"));

	    Map<Long, BigDecimal> actual = budgetService.calculateCategoryRemaining(
	            List.of(category),
	            List.of(budget),
	            categoryTotals
	    );

	    assertEquals(0, new BigDecimal("20000").compareTo(actual.get(1L)));
	}

	@Test
	void BUD008_支出MapにカテゴリIDがなければ支出を0円として計算する() {
	    Category category = createCategory(1L);
	    Budget budget = createBudget(1L, "40000", true);

	    Map<Long, BigDecimal> actual = budgetService.calculateCategoryRemaining(
	            List.of(category),
	            List.of(budget),
	            Map.of()
	    );

	    assertEquals(0, new BigDecimal("40000").compareTo(actual.get(1L)));
	}

	@Test
	void BUD009_未確定予算のカテゴリはMapに含めない() {
	    Category category = createCategory(1L);
	    Budget budget = createBudget(1L, "40000", false);
	    Map<Long, BigDecimal> categoryTotals =
	            Map.of(1L, new BigDecimal("20000"));

	    Map<Long, BigDecimal> actual = budgetService.calculateCategoryRemaining(
	            List.of(category),
	            List.of(budget),
	            categoryTotals
	    );

	    assertFalse(actual.containsKey(1L));
	}

	@Test
	void BUD010_予算データがないカテゴリはMapに含めない() {
	    Category category = createCategory(1L);

	    Map<Long, BigDecimal> actual = budgetService.calculateCategoryRemaining(
	            List.of(category),
	            List.of(),
	            Map.of()
	    );

	    assertFalse(actual.containsKey(1L));
	}

	@Test
	void BUD011_支出が予算を超えた場合はマイナスの残金を返す() {
	    Category category = createCategory(1L);
	    Budget budget = createBudget(1L, "10000", true);
	    Map<Long, BigDecimal> categoryTotals =
	            Map.of(1L, new BigDecimal("12000"));

	    Map<Long, BigDecimal> actual = budgetService.calculateCategoryRemaining(
	            List.of(category),
	            List.of(budget),
	            categoryTotals
	    );

	    assertEquals(0, new BigDecimal("-2000").compareTo(actual.get(1L)));
	}

	@Test
	void BUD012_カテゴリ一覧が空なら空のMapを返す() {
	    Map<Long, BigDecimal> actual = budgetService.calculateCategoryRemaining(
	            List.of(),
	            List.of(),
	            Map.of()
	    );

	    assertTrue(actual.isEmpty());
	}

	@Test
	void BUD013_確定済み予算があるカテゴリだけMapに含める() {
	    Category category1 = createCategory(1L);
	    Category category2 = createCategory(2L);
	    Budget budget = createBudget(1L, "40000", true);

	    Map<Long, BigDecimal> actual = budgetService.calculateCategoryRemaining(
	            List.of(category1, category2),
	            List.of(budget),
	            Map.of()
	    );

	    assertEquals(1, actual.size());
	    assertTrue(actual.containsKey(1L));
	    assertFalse(actual.containsKey(2L));
	    assertEquals(0, new BigDecimal("40000").compareTo(actual.get(1L)));
	}

	private Category createCategory(Long id) {
	    Category category = new Category();
	    category.setId(id);
	    return category;
	}

	private Budget createBudget(
	        Long categoryId,
	        String budgetAmount,
	        boolean confirmed) {

	    Budget budget = new Budget();
	    budget.setCategoryId(categoryId);
	    budget.setBudgetAmount(new BigDecimal(budgetAmount));
	    budget.setConfirmed(confirmed);
	    return budget;
	}
	
	@Test
	void BUD014_確定済み予算が1件あればtrueを返す() {
	    Budget budget = createBudget(1L, "10000", true);

	    boolean actual = budgetService.hasConfirmedBudget(List.of(budget));

	    assertTrue(actual);
	}

	@Test
	void BUD015_すべて未確定ならfalseを返す() {
	    Budget budget1 = createBudget(1L, "10000", false);
	    Budget budget2 = createBudget(2L, "20000", false);

	    boolean actual = budgetService.hasConfirmedBudget(
	            List.of(budget1, budget2)
	    );

	    assertFalse(actual);
	}

	@Test
	void BUD016_予算リストが空ならfalseを返す() {
	    boolean actual = budgetService.hasConfirmedBudget(List.of());

	    assertFalse(actual);
	}
	
	@Test
	void BUD017_確定済み予算だけを合計する() {
	    Budget budget1 = createBudget(1L, "80000", true);
	    Budget budget2 = createBudget(2L, "20000", true);
	    Budget budget3 = createBudget(3L, "50000", false);

	    BigDecimal actual = budgetService.calculateConfirmedTotal(
	            List.of(budget1, budget2, budget3)
	    );

	    assertEquals(0, new BigDecimal("100000").compareTo(actual));
	}

	@Test
	void BUD018_すべて未確定なら0円を返す() {
	    Budget budget = createBudget(1L, "50000", false);

	    BigDecimal actual = budgetService.calculateConfirmedTotal(
	            List.of(budget)
	    );

	    assertEquals(0, BigDecimal.ZERO.compareTo(actual));
	}

	@Test
	void BUD019_予算リストが空なら0円を返す() {
	    BigDecimal actual = budgetService.calculateConfirmedTotal(List.of());

	    assertEquals(0, BigDecimal.ZERO.compareTo(actual));
	}
	
	@Test
	void BUD020_確定済み予算から支出合計を引いて残金を計算する() {
	    Budget budget = createBudget(1L, "100000", true);

	    BigDecimal actual = budgetService.calculateRemainingAmount(
	            List.of(budget),
	            new BigDecimal("25000")
	    );

	    assertEquals(0, new BigDecimal("75000").compareTo(actual));
	}

	@Test
	void BUD021_未確定予算を除いて残金を計算する() {
	    Budget budget1 = createBudget(1L, "80000", true);
	    Budget budget2 = createBudget(2L, "20000", true);
	    Budget budget3 = createBudget(3L, "50000", false);

	    BigDecimal actual = budgetService.calculateRemainingAmount(
	            List.of(budget1, budget2, budget3),
	            new BigDecimal("25000")
	    );

	    assertEquals(0, new BigDecimal("75000").compareTo(actual));
	}

	@Test
	void BUD022_確定済み予算がなければnullを返す() {
	    Budget budget = createBudget(1L, "50000", false);

	    BigDecimal actual = budgetService.calculateRemainingAmount(
	            List.of(budget),
	            new BigDecimal("25000")
	    );

	    assertNull(actual);
	}

	@Test
	void BUD023_支出合計が0円なら確定済み予算の合計を返す() {
	    Budget budget = createBudget(1L, "100000", true);

	    BigDecimal actual = budgetService.calculateRemainingAmount(
	            List.of(budget),
	            BigDecimal.ZERO
	    );

	    assertEquals(0, new BigDecimal("100000").compareTo(actual));
	}
	
	@Test
	void BUD024_すべて未入力ならnullを返す() {
	    BigDecimal actual = budgetService.calculateTotalBudget(
	    		// List.of()はnullを含められないため、Arrays.asList()を使用
	            Arrays.asList(null, null, null)
	    );

	    assertNull(actual);
	}

	@Test
	void BUD025_未入力を0円として合計する() {
	    BigDecimal actual = budgetService.calculateTotalBudget(
	            Arrays.asList(
	                    new BigDecimal("40000"),
	                    new BigDecimal("20000"),
	                    null
	            )
	    );

	    assertEquals(0, new BigDecimal("60000").compareTo(actual));
	}

	@Test
	void BUD026_すべて0円なら0円を返す() {
	    BigDecimal actual = budgetService.calculateTotalBudget(
	            List.of(
	                    BigDecimal.ZERO,
	                    BigDecimal.ZERO,
	                    BigDecimal.ZERO
	            )
	    );

	    assertEquals(0, BigDecimal.ZERO.compareTo(actual));
	}
}
