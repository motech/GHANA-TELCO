package org.motechproject.ghana.telco.domain;

public enum SubscriptionStatus {

    ACTIVE("subscription is active and sending messages to subscriber"),
    ROLLED_OFF("subscription is rolled-off, stops sending its messages"),
    EXPIRED("subscription has ended, all messages are sent"),
    SUSPENDED("subscriber has ended the subscription, not all messages are sent"),
    WAITING_FOR_ROLLOVER_RESPONSE("subscriber has existing child care & pregnancy program. roll over for pregnancy program. user choose any one");

    private String description;

    SubscriptionStatus(String description) {
        this.description = description;
    }


}
