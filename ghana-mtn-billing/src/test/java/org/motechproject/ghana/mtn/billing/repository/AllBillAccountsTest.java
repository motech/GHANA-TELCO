package org.motechproject.ghana.mtn.billing.repository;

import org.junit.After;
import org.junit.Test;
import org.motechproject.ghana.mtn.billing.domain.BillAccount;
import org.motechproject.ghana.mtn.billing.domain.BillProgramAccount;
import org.motechproject.ghana.mtn.domain.IProgramType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AllBillAccountsTest extends RepositoryTest {
    @Autowired
    private AllBillAccounts allBillingAccounts;

    @Test
    public void ShouldUpdateBillAccount() {
        String mobileNumber = "1234567890";
        Double currentBalance = 2D;
        allBillingAccounts.updateBillAccount(mobileNumber, currentBalance, getPregnancyProgramType());

        assertBillAccount();

        allBillingAccounts.updateBillAccount(mobileNumber, currentBalance, getPregnancyProgramType());
        assertBillAccount();
    }

    private void assertBillAccount() {
        List<BillAccount> billAccounts = allBillingAccounts.getAll();
        assertThat(billAccounts.size(), is(1));

        BillAccount billAccount = billAccounts.get(0);

        List<BillProgramAccount> programAccounts = billAccount.getProgramAccounts();
        assertThat(programAccounts.size(), is(1));
        assertThat(programAccounts.get(0).getProgramName(), is(getPregnancyProgramType().getProgramName()));
    }

    @After
    public void destroy() {
        for (BillAccount billAccount : allBillingAccounts.getAll()) {
            allBillingAccounts.remove(billAccount);
        }
    }

    public IProgramType getPregnancyProgramType() {
        return new IProgramType() {
            @Override
            public String getProgramName() {
                return "Pregnancy";
            }

            @Override
            public List<String> getShortCodes() {
                return Arrays.asList("P");
            }

            @Override
            public Integer getMinWeek() {
                return 5;
            }

            @Override
            public Integer getMaxWeek() {
                return 35;
            }

            @Override
            public Double getFee() {
                return 0.60D;
            }
        };
    }

    public IProgramType getChildCareProgramType() {
        return new IProgramType() {
            @Override
            public String getProgramName() {
                return "Child Care";
            }

            @Override
            public List<String> getShortCodes() {
                return Arrays.asList("C");
            }

            @Override
            public Integer getMinWeek() {
                return 1;
            }

            @Override
            public Integer getMaxWeek() {
                return 52;
            }

            @Override
            public Double getFee() {
                return 0.60D;
            }
        };
    }
}
