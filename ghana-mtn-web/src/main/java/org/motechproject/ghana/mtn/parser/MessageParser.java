package org.motechproject.ghana.mtn.parser;

import org.motechproject.ghana.mtn.domain.ProgramType;
import org.motechproject.ghana.mtn.domain.SMS;
import org.motechproject.ghana.mtn.domain.ShortCode;
import org.motechproject.ghana.mtn.repository.AllProgramTypes;
import org.motechproject.ghana.mtn.repository.AllShortCodes;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.regex.Pattern;

import static ch.lambdaj.Lambda.*;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;

public abstract class MessageParser {

    @Autowired
    protected AllProgramTypes allProgramTypes;

    @Autowired
    protected AllShortCodes allShortCodes;

    protected Pattern pattern;

    public abstract SMS parse(String message, String senderMobileNumber);

    public String getProgramCodePatterns() {
        return joinFrom(flatten(extract(allProgramTypes.getAll(), on(ProgramType.class).getShortCodes())), "|").toString();
    }

    protected Pattern pattern() {
        if (pattern == null) recompilePatterns();
        return pattern;
    }

    protected String shortCodePattern(String key) {
        List<ShortCode> shortCodes = allShortCodes.getAllCodesFor(key);
        List<String> codes = isEmpty(shortCodes) ? null : shortCodes.get(0).getCodes();
        return isNotEmpty(codes) ? join(codes, "|") : "";
    }

    public abstract void recompilePatterns();
}
