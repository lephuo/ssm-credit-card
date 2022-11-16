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
            .withExternal().event(PaymentEvent.PRE_AUTHORIZE).source(PaymentState.NEW).target(PaymentState.NEW).action(preAuthAction())
            .and()
            .withExternal().event(PaymentEvent.PRE_AUTH_APPROVED).source(PaymentState.NEW).target(PaymentState.PRE_AUTH)
            .and()
            .withExternal().event(PaymentEvent.PRE_AUTH_DECLINED).source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR);
    }

    private Action<PaymentState, PaymentEvent> preAuthAction() {
        return context -> {
            System.out.println("preAuthAction was called ⚡");

            PaymentEvent payload = new Random().nextInt() > 8
                ? PaymentEvent.PRE_AUTH_APPROVED
                : PaymentEvent.PRE_AUTH_DECLINED;
            Message<PaymentEvent> message = MessageBuilder
                .withPayload(payload)
                .setHeader(PAYMENT_ID_HEADER, context.getMessageHeader(PAYMENT_ID_HEADER))
                .build();
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
            System.out.printf("State changed from %s to %s%n",
                Optional.ofNullable(from).map(State::getId).orElse(null),
                to.getId());
        }
    };
}
