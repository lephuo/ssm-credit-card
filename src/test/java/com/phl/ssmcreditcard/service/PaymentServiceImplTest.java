package com.phl.ssmcreditcard.service;

import com.phl.ssmcreditcard.domain.Payment;
import com.phl.ssmcreditcard.domain.PaymentEvent;
import com.phl.ssmcreditcard.domain.PaymentState;
import com.phl.ssmcreditcard.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class PaymentServiceImplTest {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Transactional
    @Test
    void preAuthorize() {
        Payment newPayment = paymentService.newPayment(payment);
        assertThat(newPayment.getState()).isEqualTo(PaymentState.NEW);

        StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.preAuthorize(newPayment.getId());
        assertThat(stateMachine.getState().getId()).isEqualTo(PaymentState.PRE_AUTH);

        Payment preAuthorizedPayment = paymentRepository.getOne(newPayment.getId());
        assertThat(preAuthorizedPayment.getState()).isEqualTo(PaymentState.PRE_AUTH);
        assertThat(preAuthorizedPayment.getAmount()).isEqualTo(BigDecimal.valueOf(12.99));
    }
}