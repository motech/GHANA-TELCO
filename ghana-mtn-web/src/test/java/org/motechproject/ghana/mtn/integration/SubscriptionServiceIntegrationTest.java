package org.motechproject.ghana.mtn.integration;

import org.ektorp.DbPath;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.motechproject.ghana.mtn.BaseIntegrationTest;
import org.motechproject.ghana.mtn.billing.domain.BillAccount;
import org.motechproject.ghana.mtn.billing.repository.AllBillAccounts;
import org.motechproject.ghana.mtn.controller.SubscriptionController;
import org.motechproject.ghana.mtn.domain.*;
import org.motechproject.ghana.mtn.domain.builder.ProgramTypeBuilder;
import org.motechproject.ghana.mtn.domain.dto.SubscriptionRequest;
import org.motechproject.ghana.mtn.vo.Money;
import org.motechproject.ghana.mtn.matchers.ProgramTypeMatcher;
import org.motechproject.ghana.mtn.matchers.SubscriberMatcher;
import org.motechproject.ghana.mtn.repository.AllProgramTypes;
import org.motechproject.ghana.mtn.repository.AllSubscribers;
import org.motechproject.ghana.mtn.repository.AllSubscriptions;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class SubscriptionServiceIntegrationTest extends BaseIntegrationTest{

    @Autowired
    private SubscriptionController subscriptionController;
    @Autowired
    private AllSubscriptions allSubscriptions;
    @Autowired
    private AllSubscribers allSubscribers;
    @Autowired
    private AllProgramTypes allProgramTypes;
    @Autowired
    private AllBillAccounts allBillAccounts;

    public final ProgramType childCarePregnancyType = new ProgramTypeBuilder().withFee(new Money(0.60D)).withMinWeek(1).withMaxWeek(52).withProgramName("Child Care").withShortCode("C").withShortCode("c").build();
    public final ProgramType pregnancyProgramType = new ProgramTypeBuilder().withFee(new Money(0.60D)).withMinWeek(5).withMaxWeek(35).withProgramName("Pregnancy").withShortCode("P").withShortCode("p").build();

    @Before
    public void setUp() {
        addAndMarkForDeletion(allProgramTypes, pregnancyProgramType);
        addAndMarkForDeletion(allProgramTypes, childCarePregnancyType);
    }

    //TODO Fix this asap, rewrite for all scenarios
    @Test
    @Ignore
    public void ShouldEnrollSubscriber() throws IOException {
        String shortCode = "P";
        String program = "Pregnancy";
        SubscriptionRequest subscriptionRequest = createSubscriptionRequest(shortCode + " 25", "9500012345");

        subscriptionController.enroll(subscriptionRequest, response);

        List<Subscription> subscriptions = allSubscriptions.getAll();
        List<Subscriber> subscribers = allSubscribers.getAll();
        ProgramType programType = allProgramTypes.findByCampaignShortCode(shortCode);
        Subscription subscription = subscriptions.get(0);

        assertThat(response.getContentType(), is(SubscriptionController.CONTENT_TYPE_JSON));
        assertThat(subscriptions.size(), is(1));
        assertThat(subscription.getProgramType(), new ProgramTypeMatcher(programType));
        assertThat(subscription.getStartWeekAndDay().getWeek().getNumber(), is(25));
        assertThat(subscription.getStatus(), is(SubscriptionStatus.ACTIVE));
        assertThat(subscribers.size(), is(1));
        assertThat(subscription.getSubscriber(), new SubscriberMatcher(subscribers.get(0)));


        String expectedResponse =
                SubscriptionController.JSON_PREFIX
                + String.format("Welcome to Mobile Midwife %s Program. You are now enrolled & will receive SMSs full of great info every Mon,Weds &Fri. To stop these messages send STOP", program)
                + SubscriptionController.JSON_SUFFIX;
        assertThat(response.getContentAsString(), is(expectedResponse));
    }

    @Test
    public void ShouldSendFailureResponseForInvalidMessage() throws IOException {
        SubscriptionRequest subscriptionRequest = createSubscriptionRequest("P25", "1234567890");

        subscriptionController.enroll(subscriptionRequest, response);

        assertThat(response.getContentAsString(), is(SubscriptionController.JSON_PREFIX
                + "Sorry we are having trouble processing your request."
                + SubscriptionController.JSON_SUFFIX));
        assertFalse(couchDbInstance.checkIfDbExists(new DbPath(dbConnector.getDatabaseName() + "/Subscription")));
    }

    private SubscriptionRequest createSubscriptionRequest(String inputMessage, String subscriberNumber) {
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setInputMessage(inputMessage);
        subscriptionRequest.setSubscriberNumber(subscriberNumber);
        return subscriptionRequest;
    }

    @After
    public void after() {
        super.after();
        remove(allSubscriptions.getAll());
        remove(allSubscribers.getAll());
        for(BillAccount billAccount : allBillAccounts.getAll()) allBillAccounts.remove(billAccount);                
        removeAllQuartzJobs();
    }

}