package io.github.kawasakic.householdbudget.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.kawasakic.householdbudget.PaymentMethod;
import io.github.kawasakic.householdbudget.repository.PaymentMethodRepository;

// 支払方法に関する処理をまとめるクラス
@Service
public class PaymentMethodService {

    // payment_methodsテーブルを操作するRepository
    private final PaymentMethodRepository paymentMethodRepository;

    // コンストラクタでRepositoryを受け取る
    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
    }

    // 登録されている支払方法をすべて取得する
    @Transactional(readOnly = true)
    public List<PaymentMethod> findAll() {
        return paymentMethodRepository.findAll();
    }
}