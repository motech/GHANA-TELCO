package org.motechproject.ghana.telco.domain.builder;

import org.joda.time.DateTime;
import org.motechproject.ghana.telco.domain.ProgramType;
import org.motechproject.ghana.telco.domain.Subscriber;
import org.motechproject.ghana.telco.domain.Subscription;
import org.motechproject.ghana.telco.domain.SubscriptionStatus;
import org.motechproject.ghana.telco.domain.vo.WeekAndDay;

public class SubscriptionBuilder extends Builder<Subscription> {
    private Subscriber subscriber;
    private ProgramType programType;
    private SubscriptionStatus status;
    private WeekAndDay startWeekAndDay;
    private DateTime registrationDate;
    private DateTime cycleStartDate;

    public SubscriptionBuilder() {
        super(new Subscription());
    }

    public SubscriptionBuilder withSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
        return this;
    }

    public SubscriptionBuilder withType(ProgramType type) {
        this.programType = type;
        return this;
    }

    public SubscriptionBuilder withStatus(SubscriptionStatus status) {
        this.status = status;
        return this;
    }

    public SubscriptionBuilder withStartWeekAndDay(WeekAndDay week) {
        this.startWeekAndDay = week;
        return this;
    }

    public SubscriptionBuilder withRegistrationDate(DateTime dateTime) {
        this.registrationDate = dateTime;
        return this;
    }

    public SubscriptionBuilder withCycleStartDate(DateTime cycleStartDate) {
        this.cycleStartDate = cycleStartDate;
        return this;
    }
}
