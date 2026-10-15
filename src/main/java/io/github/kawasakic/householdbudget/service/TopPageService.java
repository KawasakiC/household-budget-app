package io.github.kawasakic.householdbudget.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github.kawasakic.householdbudget.Category;
import io.github.kawasakic.householdbudget.Expense;

// トップページに関する処理をまとめるクラス
@Service
public class TopPageService {
	
	// 今日を含めた、今月の残り日数を計算する
	public long calculateRemainingDays(LocalDate today) {
	    LocalDate lastDay = YearMonth.from(today).atEndOfMonth();

	    return ChronoUnit.DAYS.between(today, lastDay) + 1;
	}

	// 今日から月末までにある土曜日の数を数える。今日が土曜日なら今日も含む
	public long countRemainingSaturdays(LocalDate today) {
	    LocalDate lastDay = YearMonth.from(today).atEndOfMonth();

	    return today.datesUntil(lastDay.plusDays(1))
	            .filter(date -> date.getDayOfWeek() == DayOfWeek.SATURDAY)
	            .count();
	}

	// 食費の残金を、今月の残り土曜日数で割って週予算を計算する
	public BigDecimal calculateWeeklyFoodBudget(
	        List<Category> categories,
	        Map<Long, BigDecimal> categoryRemaining,
	        LocalDate today) {

	    // 食費カテゴリの残金を取得する。カテゴリや残金がなければ0円とする
	    BigDecimal foodRemaining = categories.stream()
	            .filter(category -> "食費".equals(category.getName()))
	            .findFirst()
	            .map(category -> categoryRemaining.getOrDefault(
	                    category.getId(),
	                    BigDecimal.ZERO))
	            .orElse(BigDecimal.ZERO);

	    long remainingSaturdays = countRemainingSaturdays(today);

	    // 土曜日が残っていなければ残金をそのまま返す
	    if (remainingSaturdays == 0) {
	        return foodRemaining;
	    }

	    // 土曜日の回数で割り、1円未満は切り捨てる
	    return foodRemaining.divide(
	            BigDecimal.valueOf(remainingSaturdays),
	            0,
	            RoundingMode.DOWN
	    );
	}
	
	// 今月の支出リストから満足度をパーセントで計算する
	public Integer calculateSatisfactionPercent(List<Expense> monthlyExpenses) {
	    // 支出がない場合は満足度を計算できない
	    if (monthlyExpenses.isEmpty()) {
	        return null;
	    }

	    int totalScore = monthlyExpenses.stream()
	            .mapToInt(Expense::getSatisfaction)
	            .sum();

	    // 満足度の最大値を5として、平均点をパーセントに換算する
	    return (int) Math.round(
	            totalScore * 100.0 / (monthlyExpenses.size() * 5)
	    );
	}
	
	// 最低満足度のカテゴリ名とパーセントをまとめる
	public record LowestCategoryResult(List<String> names, Integer percent) {
	}

	// 60%未満で最も満足度が低いカテゴリを取得する。同率ならすべて返す
	public LowestCategoryResult findLowestSatisfactionCategories(
	        List<Category> categories,
	        List<Expense> monthlyExpenses) {

	    List<String> lowestCategoryNames = new ArrayList<>();
	    // 60%未満だけを対象にする
	    int lowestPercent = 60;

	    for (Category category : categories) {
	    	// 対象月のカテゴリごとの支出を取得
	        List<Expense> categoryExpenses = monthlyExpenses.stream()
	                .filter(expense ->
	                        expense.getCategoryId().equals(category.getId()))
	                .toList();

	        // 支出がないカテゴリは対象にしない
	        if (categoryExpenses.isEmpty()) {
	            continue;
	        }

	        // 満足度得点を取得
	        int totalScore = categoryExpenses.stream()
	                .mapToInt(Expense::getSatisfaction)
	                .sum();

	        // 満足度を％に計算
	        int percent = (int) Math.round(
	                totalScore * 100.0 / (categoryExpenses.size() * 5)
	        );

	        // 最低値を保存
	        if (percent < lowestPercent) {
	        	// これまでの最低値より低ければ、名前を入れ替える
	            lowestPercent = percent;
	            lowestCategoryNames.clear();
	            lowestCategoryNames.add(category.getName());
	            
	        } else if (percent == lowestPercent) {
	        	// 最低値と同じなら、名前を追加する
	            lowestCategoryNames.add(category.getName());
	        }
	    }

	    // 対象がなければパーセントはnullにする
	    Integer resultPercent =
	            lowestCategoryNames.isEmpty() ? null : lowestPercent;

	    return new LowestCategoryResult(
	            List.copyOf(lowestCategoryNames),
	            resultPercent
	    );
	}
}
