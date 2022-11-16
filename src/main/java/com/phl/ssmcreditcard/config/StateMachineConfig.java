package com.phl.ssmcreditcard.config;

import com.phl.ssmcreditcard.domain.PaymentEvent;
import com.phl.ssmcreditcard.domain.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

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
            .withExternal().event(PaymentEvent.PRE_AUTHORIZE).source(PaymentState.NEW).target(PaymentState.NEW)
            .and()
            .withExternal().event(PaymentEvent.PRE_AUTH_APPROVED).source(PaymentState.NEW).target(PaymentState.PRE_AUTH)
            .and()
            .withExternal().event(PaymentEvent.PRE_AUTH_DECLINED).source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR);
    }
}
