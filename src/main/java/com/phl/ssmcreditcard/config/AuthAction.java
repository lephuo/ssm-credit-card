package com.phl.ssmcreditcard.config;

import com.phl.ssmcreditcard.domain.PaymentEvent;
import com.phl.ssmcreditcard.domain.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Random;

import static com.phl.ssmcreditcard.service.PaymentServiceImpl.PAYMENT_ID_HEADER;

@Slf4j
@Component
public class AuthAction implements Action<PaymentState, PaymentEvent> {

    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> context) {

        Long paymentId = (Long) context.getMessageHeader(PAYMENT_ID_HEADER);
        boolean isApproved = new Random().nextInt() > 8;

        String logMessage = isApproved ? "Auth %s was approved" : "Auth %s was declined";
        log.info(String.format(logMessage, paymentId));

        PaymentEvent payload = isApproved ? PaymentEvent.AUTH_APPROVED : PaymentEvent.AUTH_DECLINED;
        Message<PaymentEvent> message = MessageBuilder.withPayload(payload).setHeader(PAYMENT_ID_HEADER, paymentId).build();
        context.getStateMachine().sendEvent(message);
    }
}
