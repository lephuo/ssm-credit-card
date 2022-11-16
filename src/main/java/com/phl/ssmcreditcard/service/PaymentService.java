package com.phl.ssmcreditcard.service;

import com.phl.ssmcreditcard.domain.Payment;
import com.phl.ssmcreditcard.domain.PaymentEvent;
import com.phl.ssmcreditcard.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {

    Payment newPayment(Payment payment);
    StateMachine<PaymentState, PaymentEvent> preAuthorize(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> authorize(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> decline(Long paymentId);
}
