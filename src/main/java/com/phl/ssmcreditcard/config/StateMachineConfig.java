package com.phl.ssmcreditcard.config;

import com.phl.ssmcreditcard.domain.PaymentEvent;
import com.phl.ssmcreditcard.domain.PaymentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Optional;

@Slf4j
@EnableStateMachineFactory
@RequiredArgsConstructor
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    private final Action<PaymentState, PaymentEvent> preAuthAction;
    private final Action<PaymentState, PaymentEvent> preAuthApprovedAction;
    private final Action<PaymentState, PaymentEvent> preAuthDeclinedAction;
    private final Action<PaymentState, PaymentEvent> authAction;
    private final Guard<PaymentState, PaymentEvent> paymentIdGuard;

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
            .withInternal()
            .event(PaymentEvent.PRE_AUTHORIZE)
            .source(PaymentState.NEW)
            .action(preAuthAction)
            .guard(paymentIdGuard)
            .and()

            .withExternal()
            .event(PaymentEvent.PRE_AUTH_APPROVED)
            .source(PaymentState.NEW)
            .target(PaymentState.PRE_AUTH)
            .action(preAuthApprovedAction)
            .and()

            .withExternal()
            .event(PaymentEvent.PRE_AUTH_DECLINED)
            .source(PaymentState.NEW)
            .target(PaymentState.PRE_AUTH_ERROR)
            .action(preAuthDeclinedAction)
            .and()

            .withInternal()
            .event(PaymentEvent.AUTHORIZE)
            .source(PaymentState.PRE_AUTH)
            .action(authAction)
            .and()

            .withExternal()
            .event(PaymentEvent.AUTH_APPROVED)
            .source(PaymentState.PRE_AUTH)
            .target(PaymentState.AUTH)
            .and()
            
            .withExternal()
            .event(PaymentEvent.AUTH_DECLINED)
            .source(PaymentState.PRE_AUTH)
            .target(PaymentState.AUTH_ERROR);
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
