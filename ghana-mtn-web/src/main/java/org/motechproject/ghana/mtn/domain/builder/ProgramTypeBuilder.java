package org.motechproject.ghana.mtn.domain.builder;

import org.motechproject.ghana.mtn.domain.ProgramType;

import java.util.ArrayList;
import java.util.List;

public class ProgramTypeBuilder extends Builder<ProgramType> {
    private List<String> shortCodes;
    private String programName;
    private Integer minWeek;
    private Integer maxWeek;

    public ProgramTypeBuilder() {
        super(new ProgramType());
        this.shortCodes = new ArrayList<String>();
    }

    public ProgramTypeBuilder withMinWeek(Integer minWeek) {
        this.minWeek = minWeek;
        return this;
    }

    public ProgramTypeBuilder withMaxWeek(Integer maxWeek) {
        this.maxWeek = maxWeek;
        return this;
    }

    public ProgramTypeBuilder withProgramName(String programName) {
        this.programName = programName;
        return this;
    }

    public ProgramTypeBuilder withShortCode(String shortCode) {
        this.shortCodes.add(shortCode);
        return this;
    }
}