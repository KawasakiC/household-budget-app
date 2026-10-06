package io.github.kawasakic.householdbudget.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.kawasakic.householdbudget.Category;

//categoriesテーブルの検索や登録などを行うRepository
//JpaRepositoryが基本的なDB操作を用意してくれる
//<Category, Long> は、扱うクラスがCategory、IDの型がLongという意味
public interface CategoryRepository extends JpaRepository<Category, Long> {
}