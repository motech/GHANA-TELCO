package org.motechproject.ghana.mtn.process;

import org.joda.time.DateTime;
import org.motechproject.ghana.mtn.billing.dto.BillingCycleRequest;
import org.motechproject.ghana.mtn.billing.dto.BillingCycleRollOverRequest;
import org.motechproject.ghana.mtn.billing.dto.BillingServiceResponse;
import org.motechproject.ghana.mtn.billing.dto.CustomerBill;
import org.motechproject.ghana.mtn.billing.service.BillingService;
import org.motechproject.ghana.mtn.domain.IProgramType;
import org.motechproject.ghana.mtn.domain.MessageBundle;
import org.motechproject.ghana.mtn.domain.Subscription;
import org.motechproject.ghana.mtn.domain.SubscriptionStatus;
import org.motechproject.ghana.mtn.repository.AllSubscriptions;
import org.motechproject.ghana.mtn.service.SMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.motechproject.ghana.mtn.domain.MessageBundle.BILLING_ROLLOVER;
import static org.motechproject.ghana.mtn.domain.MessageBundle.BILLING_STOPPED;
import static org.motechproject.ghana.mtn.domain.SubscriptionStatus.WAITING_FOR_ROLLOVER_RESPONSE;

@Component
public class BillingCycleProcess extends BaseSubscriptionProcess implements ISubscriptionFlowProcess {
    private BillingService billingService;
    private AllSubscriptions allSubscriptions;

    @Autowired
    public BillingCycleProcess(BillingService billingService, SMSService smsService, MessageBundle messageBundle, AllSubscriptions allSubscriptions) {
        super(smsService, messageBundle);
        this.billingService = billingService;
        this.allSubscriptions = allSubscriptions;
    }

    @Override
    public Boolean startFor(Subscription subscription) {
        BillingCycleRequest request = new BillingCycleRequest(
                subscription.subscriberNumber(),
                subscription.getProgramType(),
                subscription.billingStartDate());
        return startFor(subscription, request, MessageBundle.BILLING_SUCCESS);
    }

    @Override
    public Boolean stopExpired(Subscription subscription) {
        BillingCycleRequest request = new BillingCycleRequest(subscription.subscriberNumber(),
                subscription.getProgramType(), subscription.billingStartDate());
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        return stopFor(subscription, request, messageFor(BILLING_STOPPED));
    }

    @Override
    public Boolean stopByUser(Subscription subscription) {
        BillingCycleRequest request = new BillingCycleRequest(subscription.subscriberNumber(),
                subscription.getProgramType(), subscription.billingStartDate());
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        return stopFor(subscription, request, messageFor(BILLING_STOPPED));
    }

    @Override
    public Boolean rollOver(Subscription fromSubscription, Subscription toSubscription) {
        if (WAITING_FOR_ROLLOVER_RESPONSE.equals(fromSubscription.getStatus())) {
            return stopFor(fromSubscription, billingRequest(fromSubscription.subscriberNumber(), fromSubscription.getProgramType(), null), null);
        }

        DateTime billingStartDateFromSubscription = fromSubscription.billingStartDate();
        BillingCycleRequest fromRequest = billingRequest(fromSubscription.subscriberNumber(), fromSubscription.getProgramType(), billingStartDateFromSubscription);
        BillingCycleRequest toRequest = billingRequest(toSubscription.subscriberNumber(), toSubscription.getProgramType(), billingStartDateFromSubscription);

        return handleResponse(toSubscription, billingService.rollOverBilling(new BillingCycleRollOverRequest(fromRequest, toRequest)), messageFor(BILLING_ROLLOVER));
    }

    @Override
    public Boolean retainExistingChildCare(Subscription pregnancySubscriptionWaitingForRollOver, Subscription childCareSubscription) {
        return true;
    }

    @Override
    public Boolean rollOverToNewChildCareProgram(Subscription pregnancyProgramWaitingForRollOver, Subscription existingChildCare) {
        return true;
    }

    private Boolean stopFor(Subscription subscription, BillingCycleRequest request, String successMsg) {
        return handleResponse(subscription, billingService.stopBilling(request), successMsg);
    }

    private Boolean startFor(Subscription subscription, BillingCycleRequest request, String msgKey) {
        BillingServiceResponse<CustomerBill> response = billingService.startBilling(request);
        String successMsg = response.hasErrors() ? null : format(messageFor(msgKey), response.getValue().amountCharged());
        return handleResponse(subscription, response, successMsg);
    }

    private boolean handleResponse(Subscription subscription, BillingServiceResponse response, String successMsg) {
        if (response.hasErrors()) {
            sendMessage(subscription, messageFor(response.getValidationErrors()));
            return false;
        }
        if (isNotEmpty(successMsg)) sendMessage(subscription, successMsg);
        return true;
    }

    private BillingCycleRequest billingRequest(String subscriberNumber, IProgramType programType, DateTime cycleStartDate) {
        return new BillingCycleRequest(subscriberNumber, programType, cycleStartDate);
    }
}
