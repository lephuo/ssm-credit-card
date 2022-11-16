package com.phl.ssmcreditcard.service;

import com.phl.ssmcreditcard.domain.Payment;
import com.phl.ssmcreditcard.domain.PaymentEvent;
import com.phl.ssmcreditcard.domain.PaymentState;
import com.phl.ssmcreditcard.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final StateMachineFactory<PaymentState, PaymentEvent> factory;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return repository.save(payment);
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuthorize(Long paymentId) {
        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorize(Long paymentId) {
        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> decline(Long paymentId) {
        return null;
    }

    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId) {

        Payment payment = repository.getOne(paymentId);
        StateMachine<PaymentState, PaymentEvent> stateMachine = factory.getStateMachine(paymentId.toString());

        stateMachine.stop();
        resetStateMachine(stateMachine, payment);
        stateMachine.start();

        return stateMachine;
    }

    private static void resetStateMachine(StateMachine<PaymentState, PaymentEvent> stateMachine, Payment payment) {
        DefaultStateMachineContext<PaymentState, PaymentEvent> context =
            new DefaultStateMachineContext<>(payment.getState(), null, null, null);
        stateMachine
            .getStateMachineAccessor()
            .doWithAllRegions(sma -> sma.resetStateMachine(context));
    }
}
