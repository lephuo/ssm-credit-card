package com.phl.ssmcreditcard.service;

import org.springframework.statemachine.StateMachine;

public class StateMachineMermaidGenerator {

    public static <S, E> String generate(StateMachine<S, E> stateMachine) {
        System.out.println("Generating mermaid 🧜\n");
        StringBuilder mmBuilder = new StringBuilder();

        mmBuilder.append("%% States %%\n");
        generateStates(stateMachine, mmBuilder);

        mmBuilder.append("\n%% Transitions %%\n");
        generateTransitions(stateMachine, mmBuilder);

        return mmBuilder.toString();
    }

    private static <S, E> void generateStates(StateMachine<S, E> stateMachine, StringBuilder mmBuilder) {
        stateMachine.getStates().forEach(state -> mmBuilder.append(String.format("%s(%s)\n", state.getId(), state.getId())));
    }

    private static <S, E> void generateTransitions(StateMachine<S, E> stateMachine, StringBuilder mmBuilder) {
        stateMachine.getTransitions().forEach(transition -> {
            S source = transition.getSource().getId();
            S target = transition.getTarget().getId();
            E event = transition.getTrigger().getEvent();
            mmBuilder.append(String.format("%s -->|%s| %s\n", source, event, target));
        });
    }
}
