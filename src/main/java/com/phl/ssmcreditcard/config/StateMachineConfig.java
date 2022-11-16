package com.phl.ssmcreditcard.config;

import com.phl.ssmcreditcard.domain.PaymentEvent;
import com.phl.ssmcreditcard.domain.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;

import static com.phl.ssmcreditcard.service.PaymentServiceImpl.PAYMENT_ID_HEADER;

@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states
            .withStates()
            .initial(PaymentState.NEW)
            .states(EnumSet.allOf(PaymentState.class))
            .end(PaymentState.AUTH)
            .end(PaymentState.AUTH_ERROR)
            .end(PaymentState.PRE_AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions
            .withInternal().event(PaymentEvent.PRE_AUTHORIZE).source(PaymentState.NEW).action(preAuthAction()).and()

            .withExternal().event(PaymentEvent.PRE_AUTH_APPROVED).source(PaymentState.NEW).target(PaymentState.PRE_AUTH).and()
            .withExternal().event(PaymentEvent.PRE_AUTH_DECLINED).source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).and()

            .withInternal().event(PaymentEvent.AUTHORIZE).source(PaymentState.PRE_AUTH).action(authAction()).and()

            .withExternal().event(PaymentEvent.AUTH_APPROVED).source(PaymentState.PRE_AUTH).target(PaymentState.AUTH).and()
            .withExternal().event(PaymentEvent.AUTH_DECLINED).source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR);
    }

    private Action<PaymentState, PaymentEvent> preAuthAction() {
        return context -> {
            Long paymentId = (Long) context.getMessageHeader(PAYMENT_ID_HEADER);
            boolean isApproved = new Random().nextInt() > 8;

            String logMessage = isApproved ? "PreAuth %s was approved" : "PreAuth %s was declined";
            log.info(String.format(logMessage, paymentId));

            PaymentEvent payload = isApproved ? PaymentEvent.PRE_AUTH_APPROVED : PaymentEvent.PRE_AUTH_DECLINED;
            Message<PaymentEvent> message = MessageBuilder.withPayload(payload).setHeader(PAYMENT_ID_HEADER, paymentId).build();
            context.getStateMachine().sendEvent(message);
        };
    }

    private Action<PaymentState, PaymentEvent> authAction() {
        return context -> {
            Long paymentId = (Long) context.getMessageHeader(PAYMENT_ID_HEADER);
            boolean isApproved = new Random().nextInt() > 8;

            String logMessage = isApproved ? "Auth %s was approved" : "Auth %s was declined";
            log.info(String.format(logMessage, paymentId));

            PaymentEvent payload = isApproved ? PaymentEvent.AUTH_APPROVED : PaymentEvent.AUTH_DECLINED;
            Message<PaymentEvent> message = MessageBuilder.withPayload(payload).setHeader(PAYMENT_ID_HEADER, paymentId).build();
            context.getStateMachine().sendEvent(message);
        };
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        config.withConfiguration().listener(listener);
    }

    private final StateMachineListenerAdapter<PaymentState, PaymentEvent> listener = new StateMachineListenerAdapter<>() {
        @Override
        public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
            PaymentState fromStateId = Optional.ofNullable(from).map(State::getId).orElse(null);
            String message = String.format("State changed from %s to %s%n", fromStateId, to.getId());
            log.info(message);
        }
    };
}
