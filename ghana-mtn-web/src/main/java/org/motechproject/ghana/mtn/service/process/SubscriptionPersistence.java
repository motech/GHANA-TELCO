package org.motechproject.ghana.mtn.service.process;

import org.motechproject.ghana.mtn.domain.MessageBundle;
import org.motechproject.ghana.mtn.domain.Subscription;
import org.motechproject.ghana.mtn.domain.SubscriptionStatus;
import org.motechproject.ghana.mtn.repository.AllSubscribers;
import org.motechproject.ghana.mtn.repository.AllSubscriptions;
import org.motechproject.ghana.mtn.service.SMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionPersistence extends BaseSubscriptionProcess {
    private AllSubscribers allSubscribers;
    private AllSubscriptions allSubscriptions;

    @Autowired
    public SubscriptionPersistence(AllSubscribers allSubscribers, AllSubscriptions allSubscriptions, SMSService smsService, MessageBundle messageBundle) {
        super(smsService, messageBundle);
        this.allSubscribers = allSubscribers;
        this.allSubscriptions = allSubscriptions;
    }

    @Override
    public Boolean startFor(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.updateStartCycleInfo();
        allSubscribers.add(subscription.getSubscriber());
        allSubscriptions.add(subscription);
        return true;
    }

    @Override
    public Boolean endFor(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        allSubscriptions.update(subscription);
        return true;
    }
}