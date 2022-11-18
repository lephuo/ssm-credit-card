package com.phl.ssmcreditcard.service;

import com.phl.ssmcreditcard.domain.Payment;
import com.phl.ssmcreditcard.domain.PaymentEvent;
import com.phl.ssmcreditcard.domain.PaymentState;
import com.phl.ssmcreditcard.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    public static String PAYMENT_ID_HEADER = "payment_id";

    private final PaymentRepository repository;
    private final StateMachineFactory<PaymentState, PaymentEvent> factory;
    private final PaymentStateChangeInterceptor interceptor;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return repository.save(payment);
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuthorize(Long paymentId) {
        return sendEvent(PaymentEvent.PRE_AUTHORIZE, paymentId);
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorize(Long paymentId) {
        return sendEvent(PaymentEvent.AUTHORIZE, paymentId);
    }

    @Deprecated
    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        return sendEvent(PaymentEvent.AUTH_DECLINED, paymentId);
    }

    private StateMachine<PaymentState, PaymentEvent> sendEvent(PaymentEvent event, Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);
        sendEvent(event, paymentId, stateMachine);
        return stateMachine;
    }

    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId) {

        Payment payment = repository.getOne(paymentId);
        StateMachine<PaymentState, PaymentEvent> stateMachine = factory.getStateMachine(paymentId.toString());

        System.out.println(StateMachineMermaidGenerator.generate(stateMachine));;

        stateMachine.stop();
        resetStateMachine(stateMachine, payment);
        stateMachine.start();

        return stateMachine;
    }

    private void resetStateMachine(StateMachine<PaymentState, PaymentEvent> stateMachine, Payment payment) {
        DefaultStateMachineContext<PaymentState, PaymentEvent> context =
            new DefaultStateMachineContext<>(payment.getState(), null, null, null);
        stateMachine
            .getStateMachineAccessor()
            .doWithAllRegions(sma -> {
                sma.addStateMachineInterceptor(interceptor);
                sma.resetStateMachine(context);
            });
    }

    private void sendEvent(PaymentEvent event, Long paymentId, StateMachine<PaymentState, PaymentEvent> stateMachine) {
        Message<PaymentEvent> message = MessageBuilder
            .withPayload(event)
            .setHeader(PAYMENT_ID_HEADER, paymentId)
            .build();
        stateMachine.sendEvent(message);
    }
}
