package org.motechproject.ghana.mtn.process;

import org.joda.time.DateTime;
import org.motechproject.ghana.mtn.billing.dto.*;
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
import static org.motechproject.ghana.mtn.domain.MessageBundle.*;
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
        if (subscription.nextBillingDate().isBefore(subscription.getSubscriptionEndDate())) {
            BillingCycleRequest request = billingRequest(subscription);
            return startFor(subscription, request, BILLING_SUCCESS);
        } else
            return chargeFee(subscription);
    }

    @Override
    public Boolean stopExpired(Subscription subscription) {
        return stop(subscription, messageFor(BILLING_STOPPED));
    }

    @Override
    public Boolean stopByUser(Subscription subscription) {
        BillingCycleRequest request = billingRequest(subscription);
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        return stopFor(subscription, request, messageFor(BILLING_STOPPED));
    }

    @Override
    public Boolean rollOver(Subscription fromSubscription, Subscription toSubscription) {
        return WAITING_FOR_ROLLOVER_RESPONSE.equals(fromSubscription.getStatus()) || performRollOver(fromSubscription, toSubscription, messageFor(BILLING_ROLLOVER));
    }

    @Override
    public Boolean retainExistingChildCare(Subscription pregnancySubscriptionWaitingForRollOver, Subscription childCareSubscription) {
        return stopFor(pregnancySubscriptionWaitingForRollOver, billingRequest(pregnancySubscriptionWaitingForRollOver.subscriberNumber(),
                pregnancySubscriptionWaitingForRollOver.getProgramType(), null, null), null);
    }

    @Override
    public Boolean rollOverToNewChildCareProgram(Subscription pregnancyProgramWaitingForRollOver, Subscription newChildCareToRollOver, Subscription existingChildCare) {
        if (!stop(existingChildCare, null)) return false;
        performRollOver(pregnancyProgramWaitingForRollOver, newChildCareToRollOver, messageFor(PENDING_ROLLOVER_SWITCH_TO_NEW_CHILDCARE_BILLING));
        return true;
    }

    private Boolean performRollOver(Subscription fromSubscription, Subscription toSubscription, String successMsg) {
        DateTime billingStartDateFromSubscription = fromSubscription.getBillingStartDate();
        BillingCycleRequest fromRequest = billingRequest(fromSubscription);
        BillingCycleRequest toRequest = billingRequest(toSubscription.subscriberNumber(), toSubscription.getProgramType(), billingStartDateFromSubscription,
                toSubscription.getSubscriptionEndDate());

        return handleResponse(toSubscription, billingService.rollOverBilling(new BillingCycleRollOverRequest(fromRequest, toRequest)), successMsg);
    }

    private Boolean stop(Subscription subscription, String successMsg) {
        BillingCycleRequest request = billingRequest(subscription.subscriberNumber(), subscription.getProgramType(), null, null);
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        return stopFor(subscription, request, successMsg);
    }

    private Boolean stopFor(Subscription subscription, BillingCycleRequest request, String successMsg) {
        return handleResponse(subscription, billingService.stopBilling(request), successMsg);
    }

    private Boolean startFor(Subscription subscription, BillingCycleRequest request, String msgKey) {
        BillingServiceResponse<CustomerBill> response = billingService.chargeAndStartBilling(request);
        String successMsg = response.hasErrors() ? null : format(messageFor(msgKey), response.getValue().amountCharged());
        return handleResponse(subscription, response, successMsg);
    }

    private Boolean chargeFee(Subscription subscription) {
        BillingServiceResponse<CustomerBill> response = billingService.chargeProgramFee(new BillingServiceRequest(subscription.subscriberNumber(),
                subscription.getProgramType()));
        String successMsg = response.hasErrors() ? null : format(messageFor(BILLING_SUCCESS), response.getValue().amountCharged());
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

    private BillingCycleRequest billingRequest(String subscriberNumber, IProgramType programType, DateTime cycleStartDate, DateTime cycleEndDate) {
        return new BillingCycleRequest(subscriberNumber, programType, cycleStartDate, cycleEndDate);
    }

    private BillingCycleRequest billingRequest(Subscription subscription) {
        return new BillingCycleRequest(subscription.subscriberNumber(), subscription.getProgramType(), subscription.getBillingStartDate(),
                subscription.getSubscriptionEndDate());
    }
}
