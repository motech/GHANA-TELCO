package org.motechproject.ghana.telco.process;

import org.motechproject.ghana.telco.domain.MessageBundle;
import org.motechproject.ghana.telco.domain.Subscription;
import org.motechproject.ghana.telco.domain.SubscriptionStatus;
import org.motechproject.ghana.telco.repository.AllSubscribers;
import org.motechproject.ghana.telco.repository.AllSubscriptions;
import org.motechproject.ghana.telco.service.SMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.motechproject.ghana.telco.domain.SubscriptionStatus.WAITING_FOR_ROLLOVER_RESPONSE;

@Component
public class PersistenceProcess extends BaseSubscriptionProcess implements ISubscriptionFlowProcess {
    private AllSubscribers allSubscribers;
    private AllSubscriptions allSubscriptions;

    @Autowired
    public PersistenceProcess(AllSubscribers allSubscribers, AllSubscriptions allSubscriptions, SMSService smsService, MessageBundle messageBundle) {
        super(smsService, messageBundle);
        this.allSubscribers = allSubscribers;
        this.allSubscriptions = allSubscriptions;
    }

    @Override
    public Boolean startFor(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        allSubscribers.add(subscription.getSubscriber());
        allSubscriptions.add(subscription);
        return true;
    }

    @Override
    public Boolean stopExpired(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        allSubscriptions.update(subscription);
        return true;
    }

    @Override
    public Boolean stopByUser(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        allSubscriptions.update(subscription);
        return true;
    }

    @Override
    public Boolean rollOver(Subscription fromSubscription, Subscription toSubscription) {
        if (!WAITING_FOR_ROLLOVER_RESPONSE.equals(fromSubscription.getStatus())) {
            performRollOver(fromSubscription, toSubscription);
        } else allSubscriptions.update(fromSubscription);
        return true;
    }

    @Override
    public Boolean retainExistingChildCare(Subscription pregnancySubscriptionWaitingForRollOver, Subscription childCareSubscription) {
        pregnancySubscriptionWaitingForRollOver.setStatus(SubscriptionStatus.EXPIRED);
        allSubscriptions.update(pregnancySubscriptionWaitingForRollOver);
        return true;
    }

    @Override
    public Boolean rollOverToNewChildCareProgram(Subscription pregnancyProgramWaitingForRollOver, Subscription newChildCareToRollOver, Subscription existingChildCare) {
        performRollOver(pregnancyProgramWaitingForRollOver, newChildCareToRollOver);
        existingChildCare.setStatus(SubscriptionStatus.EXPIRED);
        allSubscriptions.update(existingChildCare);
        return true;
    }

    private void performRollOver(Subscription fromSubscription, Subscription toSubscription) {
        fromSubscription.setStatus(SubscriptionStatus.ROLLED_OFF);
        toSubscription.setStatus(SubscriptionStatus.ACTIVE);
        allSubscriptions.add(toSubscription);
        allSubscriptions.update(fromSubscription);
    }
}
