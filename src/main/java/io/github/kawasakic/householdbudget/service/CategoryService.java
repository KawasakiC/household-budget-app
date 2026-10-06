package io.github.kawasakic.householdbudget.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.kawasakic.householdbudget.Category;
import io.github.kawasakic.householdbudget.repository.CategoryRepository;

// カテゴリに関する処理をまとめるクラス
@Service
public class CategoryService {

    // categoriesテーブルを操作するRepository
    private final CategoryRepository categoryRepository;

    // コンストラクタでRepositoryを受け取る
  public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // 登録されているカテゴリをすべて取得する
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
}