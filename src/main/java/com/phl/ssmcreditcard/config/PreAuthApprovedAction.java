package com.phl.ssmcreditcard.config;

import com.phl.ssmcreditcard.domain.PaymentEvent;
import com.phl.ssmcreditcard.domain.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import static com.phl.ssmcreditcard.service.PaymentServiceImpl.PAYMENT_ID_HEADER;

@Slf4j
@Component
public class PreAuthApprovedAction implements Action<PaymentState, PaymentEvent> {

    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> context) {

        Long paymentId = (Long) context.getMessageHeader(PAYMENT_ID_HEADER);
        log.info(String.format("Payment %s was preAuth approved", paymentId));
    }
}
