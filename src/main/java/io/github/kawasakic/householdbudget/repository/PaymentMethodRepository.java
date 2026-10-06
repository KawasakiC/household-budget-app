package io.github.kawasakic.householdbudget.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.kawasakic.householdbudget.PaymentMethod;

// payment_methodsテーブルの検索や登録などを行うRepository
// JpaRepositoryが基本的なDB操作を用意してくれる
// <PaymentMethod, Long> は、扱うクラスがPaymentMethod、IDの型がLongという意味
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
}