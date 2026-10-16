package io.github.kawasakic.householdbudget.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.kawasakic.householdbudget.Category;
import io.github.kawasakic.householdbudget.Expense;
import io.github.kawasakic.householdbudget.service.TopPageService.LowestCategoryResult;

class TopPageServiceTest {

    private TopPageService topPageService;

    @BeforeEach
    void setUp() {
        topPageService = new TopPageService();
    }

    @Test
    void TOP001_10月1日の残り日数は31日() {
        assertEquals(
                31,
                topPageService.calculateRemainingDays(LocalDate.of(2026, 10, 1))
        );
    }

    @Test
    void TOP002_10月15日の残り日数は17日() {
        assertEquals(
                17,
                topPageService.calculateRemainingDays(LocalDate.of(2026, 10, 15))
        );
    }

    @Test
    void TOP003_10月31日の残り日数は1日() {
        assertEquals(
                1,
                topPageService.calculateRemainingDays(LocalDate.of(2026, 10, 31))
        );
    }

    @Test
    void TOP004_うるう年の2月1日の残り日数は29日() {
        assertEquals(
                29,
                topPageService.calculateRemainingDays(LocalDate.of(2024, 2, 1))
        );
    }

    @Test
    void TOP005_うるう年の2月29日の残り日数は1日() {
        assertEquals(
                1,
                topPageService.calculateRemainingDays(LocalDate.of(2024, 2, 29))
        );
    }
    
    @Test
    void TOP006_10月1日から月末までの土曜日は5回() {
        assertEquals(
                5,
                topPageService.countRemainingSaturdays(LocalDate.of(2026, 10, 1))
        );
    }

    @Test
    void TOP007_10月11日から月末までの土曜日は3回() {
        assertEquals(
                3,
                topPageService.countRemainingSaturdays(LocalDate.of(2026, 10, 11))
        );
    }

    @Test
    void TOP008_土曜日の10月17日も含めて3回() {
        assertEquals(
                3,
                topPageService.countRemainingSaturdays(LocalDate.of(2026, 10, 17))
        );
    }

    @Test
    void TOP009_10月31日当日の土曜日を含めて1回() {
        assertEquals(
                1,
                topPageService.countRemainingSaturdays(LocalDate.of(2026, 10, 31))
        );
    }

    @Test
    void TOP010_11月29日以降の土曜日は0回() {
        assertEquals(
                0,
                topPageService.countRemainingSaturdays(LocalDate.of(2026, 11, 29))
        );
    }
    
    @Test
    void TOP011_残金30000円を残り3回で割ると10000円() {
        Category food = new Category();
        food.setId(1L);
        food.setName("食費");

        Map<Long, BigDecimal> categoryRemaining = Map.of(
                1L, new BigDecimal("30000")
        );

        BigDecimal actual = topPageService.calculateWeeklyFoodBudget(
                List.of(food),
                categoryRemaining,
                LocalDate.of(2026, 10, 11)
        );

        assertEquals(0, new BigDecimal("10000").compareTo(actual));
    }

    @Test
    void TOP012_割り切れない金額は1円未満を切り捨てる() {
        Category food = new Category();
        food.setId(1L);
        food.setName("食費");

        Map<Long, BigDecimal> categoryRemaining = Map.of(
                1L, new BigDecimal("10000")
        );

        BigDecimal actual = topPageService.calculateWeeklyFoodBudget(
                List.of(food),
                categoryRemaining,
                LocalDate.of(2026, 10, 11)
        );

        assertEquals(0, new BigDecimal("3333").compareTo(actual));
    }

    @Test
    void TOP013_残り土曜日がなければ食費の残金をそのまま返す() {
        Category food = new Category();
        food.setId(1L);
        food.setName("食費");

        Map<Long, BigDecimal> categoryRemaining = Map.of(
                1L, new BigDecimal("30000")
        );

        BigDecimal actual = topPageService.calculateWeeklyFoodBudget(
                List.of(food),
                categoryRemaining,
                LocalDate.of(2026, 11, 29)
        );

        assertEquals(0, new BigDecimal("30000").compareTo(actual));
    }

    @Test
    void TOP014_食費カテゴリがなければ0円を返す() {
        Category dailyGoods = new Category();
        dailyGoods.setId(2L);
        dailyGoods.setName("日用品");

        Map<Long, BigDecimal> categoryRemaining = Map.of(
                2L, new BigDecimal("5000")
        );

        BigDecimal actual = topPageService.calculateWeeklyFoodBudget(
                List.of(dailyGoods),
                categoryRemaining,
                LocalDate.of(2026, 10, 11)
        );

        assertEquals(0, BigDecimal.ZERO.compareTo(actual));
    }

    @Test
    void TOP015_食費の残金がMapになければ0円を返す() {
        Category food = new Category();
        food.setId(1L);
        food.setName("食費");

        Map<Long, BigDecimal> categoryRemaining = Map.of(
                2L, new BigDecimal("5000")
        );

        BigDecimal actual = topPageService.calculateWeeklyFoodBudget(
                List.of(food),
                categoryRemaining,
                LocalDate.of(2026, 10, 11)
        );

        assertEquals(0, BigDecimal.ZERO.compareTo(actual));
    }
    
    @Test
    void TOP016_支出がなければnullを返す() {
        Integer actual = topPageService.calculateSatisfactionPercent(List.of());

        assertNull(actual);
    }

    @Test
    void TOP017_満足度がすべて5なら100パーセント() {
        Integer actual = topPageService.calculateSatisfactionPercent(
                List.of(createExpense(5))
        );

        assertEquals(100, actual);
    }

    @Test
    void TOP018_満足度がすべて1なら20パーセント() {
        Integer actual = topPageService.calculateSatisfactionPercent(
                List.of(createExpense(1))
        );

        assertEquals(20, actual);
    }

    @Test
    void TOP019_満足度が5_4_3_3なら75パーセント() {
        Integer actual = topPageService.calculateSatisfactionPercent(
                List.of(
                        createExpense(5),
                        createExpense(4),
                        createExpense(3),
                        createExpense(3)
                )
        );

        assertEquals(75, actual);
    }

    @Test
    void TOP020_平均が97_5パーセントなら四捨五入して98パーセント() {
        Integer actual = topPageService.calculateSatisfactionPercent(
                List.of(
                        createExpense(5),
                        createExpense(5),
                        createExpense(5),
                        createExpense(5),
                        createExpense(5),
                        createExpense(5),
                        createExpense(5),
                        createExpense(4)
                )
        );

        assertEquals(98, actual);
    }

    // 満足度だけ設定したExpenseを作る
    private Expense createExpense(int satisfaction) {
        Expense expense = new Expense();
        expense.setSatisfaction((short) satisfaction);
        return expense;
    }
    
    @Test
    void TOP021_60パーセント未満のうち最も低いカテゴリだけ返す() {
        Category food = createCategory(1L, "食費");
        Category dailyGoods = createCategory(2L, "日用品");

        LowestCategoryResult actual =
                topPageService.findLowestSatisfactionCategories(
                        List.of(food, dailyGoods),
                        List.of(
                                createExpense(1L, 2), // 食費：40%
                                createExpense(2L, 3)  // 日用品：60%
                        )
                );

        assertEquals(List.of("食費"), actual.names());
        assertEquals(40, actual.percent());
    }

    @Test
    void TOP022_最低値が同率のカテゴリをすべて返す() {
        Category food = createCategory(1L, "食費");
        Category dailyGoods = createCategory(2L, "日用品");

        LowestCategoryResult actual =
                topPageService.findLowestSatisfactionCategories(
                        List.of(food, dailyGoods),
                        List.of(
                                createExpense(1L, 2),
                                createExpense(1L, 3), // 食費：50%
                                createExpense(2L, 2),
                                createExpense(2L, 3)  // 日用品：50%
                        )
                );

        assertEquals(List.of("食費", "日用品"), actual.names());
        assertEquals(50, actual.percent());
    }

    @Test
    void TOP023_すべて60パーセント以上なら空の名前リストとnullを返す() {
        Category food = createCategory(1L, "食費");
        Category dailyGoods = createCategory(2L, "日用品");

        LowestCategoryResult actual =
                topPageService.findLowestSatisfactionCategories(
                        List.of(food, dailyGoods),
                        List.of(
                                createExpense(1L, 3), // 食費：60%
                                createExpense(2L, 4)  // 日用品：80%
                        )
                );

        assertEquals(List.of(), actual.names());
        assertNull(actual.percent());
    }

    @Test
    void TOP024_支出がないカテゴリは判定対象に含めない() {
        Category food = createCategory(1L, "食費");
        Category dailyGoods = createCategory(2L, "日用品");

        LowestCategoryResult actual =
                topPageService.findLowestSatisfactionCategories(
                        List.of(food, dailyGoods),
                        List.of(
                                createExpense(2L, 2) // 日用品だけ支出あり：40%
                        )
                );

        assertEquals(List.of("日用品"), actual.names());
        assertEquals(40, actual.percent());
    }

    @Test
    void TOP025_対象月の支出がなければ空の名前リストとnullを返す() {
        Category food = createCategory(1L, "食費");
        Category dailyGoods = createCategory(2L, "日用品");

        LowestCategoryResult actual =
                topPageService.findLowestSatisfactionCategories(
                        List.of(food, dailyGoods),
                        List.of()
                );

        assertEquals(List.of(), actual.names());
        assertNull(actual.percent());
    }

    private Category createCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    private Expense createExpense(Long categoryId, int satisfaction) {
        Expense expense = new Expense();
        expense.setCategoryId(categoryId);
        expense.setSatisfaction((short) satisfaction);
        return expense;
    }
}