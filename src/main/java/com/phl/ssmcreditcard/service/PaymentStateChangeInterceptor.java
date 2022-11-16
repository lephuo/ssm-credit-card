package com.phl.ssmcreditcard.service;

import com.phl.ssmcreditcard.domain.Payment;
import com.phl.ssmcreditcard.domain.PaymentEvent;
import com.phl.ssmcreditcard.domain.PaymentState;
import com.phl.ssmcreditcard.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import static com.phl.ssmcreditcard.service.PaymentServiceImpl.PAYMENT_ID_HEADER;

@RequiredArgsConstructor
@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(
        State<PaymentState, PaymentEvent> state,
        Message<PaymentEvent> message,
        Transition<PaymentState, PaymentEvent> transition,
        StateMachine<PaymentState, PaymentEvent> stateMachine,
        StateMachine<PaymentState, PaymentEvent> rootStateMachine
    ) {
        if (message == null || !message.getHeaders().containsKey(PAYMENT_ID_HEADER)) return;

        Long paymentId = (Long) message.getHeaders().get(PAYMENT_ID_HEADER);
        if (paymentId == null) return;

        Payment payment = paymentRepository.getOne(paymentId);
        payment.setState(state.getId());
        paymentRepository.save(payment);
    }
}
