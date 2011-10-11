package org.motechproject.ghana.mtn.repository;

import org.ektorp.CouchDbConnector;
import org.motechproject.dao.MotechAuditableRepository;
import org.motechproject.ghana.mtn.domain.SMSAudit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class AllSMSAudits extends MotechAuditableRepository<SMSAudit> {

    @Autowired
    protected AllSMSAudits(@Qualifier("dbConnector") CouchDbConnector db) {
        super(SMSAudit.class, db);
    }

}