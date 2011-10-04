package org.motechproject.ghana.mtn.repository;

import org.ektorp.CouchDbConnector;
import org.motechproject.dao.MotechAuditableRepository;
import org.motechproject.ghana.mtn.domain.ProgramType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AllProgramTypes extends MotechAuditableRepository<ProgramType> {
    @Autowired
    protected AllProgramTypes(@Qualifier("dbConnector") CouchDbConnector db) {
        super(ProgramType.class, db);
    }

    public ProgramType findByCampaignShortCode(String shortCode) {
        List<ProgramType> programTypes = getAll();
        for (ProgramType programType : programTypes) {
            if (programType.getShortCodes().contains(shortCode))
                return programType;
        }
        return null;
    }

}