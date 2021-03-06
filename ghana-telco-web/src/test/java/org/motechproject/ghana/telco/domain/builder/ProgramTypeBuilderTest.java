package org.motechproject.ghana.telco.domain.builder;

import org.junit.Test;
import org.motechproject.ghana.telco.domain.ProgramType;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ProgramTypeBuilderTest {
    @Test
    public void ShouldCreateProgramTypeObjectUsingBuilder() {
        Integer minWeek = 5;
        Integer maxWeek = 10;
        String programName = "Pregnancy";
        String shortCode = "P";
        ProgramType programType = new ProgramTypeBuilder()
                .withProgramName(programName)
                .withShortCode(shortCode)
                .withProgramKey(ProgramType.PREGNANCY)
                .withMinWeek(minWeek).withMaxWeek(maxWeek).build();

        assertThat(programType.getMaxWeek(), is(maxWeek));
        assertThat(programType.getMinWeek(), is(minWeek));
        assertThat(programType.getProgramName(), is(programName));
        assertThat(programType.getProgramKey(), is(ProgramType.PREGNANCY));
        assertThat(programType.getShortCodes().get(0), is(shortCode));
    }
}
