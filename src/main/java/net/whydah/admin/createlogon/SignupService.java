package net.whydah.admin.createlogon;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by baardl on 30.09.15.
 */
@Service
public class SignupService {
    private static final Logger log = getLogger(SignupService.class);
    private final UibCreateLogonConnection uibConnection;

    @Autowired
    public SignupService(UibCreateLogonConnection uibConnection) {
        this.uibConnection = uibConnection;
    }

}
