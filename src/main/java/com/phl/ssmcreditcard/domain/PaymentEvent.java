package com.phl.ssmcreditcard.domain;

public enum PaymentEvent {
    PRE_AUTHORIZE("preAuthorize"),
    PRE_AUTH_APPROVED("preAuthorizeApproved"),
    PRE_AUTH_DECLINED("preAuthorizeDeclined"),
    AUTHORIZE("authorize"),
    AUTH_APPROVED("authApproved"),
    AUTH_DECLINED("authDeclined");

    private final String text;

    PaymentEvent(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
