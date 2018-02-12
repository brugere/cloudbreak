package com.sequenceiq.it.cloudbreak;

import com.sequenceiq.it.cloudbreak.newway.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.newway.CloudbreakTest;
import com.sequenceiq.it.cloudbreak.newway.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.BadRequestException;

public class CredentialTests extends CloudbreakTest {

    public static final String VALID_CRED_NAME = "valid-credential";

    public static final String AGAIN_CRED_NAME = "again-credential";

    public static final String LONG_DC_CRED_NAME = "long-description-credential";

    public static final String DELETE_CRED_NAME = "delete-credential";

    public static final String SPECIAL_CRED_NAME = "@#$%|:&*;";

    public static final String INVALID_SHORT_CRED_NAME = "";

    public static final String INVALID_LONG_CRED_NAME = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    public static final String EMPTY_CRED_NAME = "temp-empty-credential";

    public static final String INVALID_KEY_CRED_NAME = "temp-key-credential";

    public static final String CRED_DESCRIPTION = "temporary credential for API E2E tests";

    public static final String INVALID_LONG_DESCRIPTION = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudbreakTest.class);

    private String errorMessage = "";

    @Test(expectedExceptions = BadRequestException.class)
    public void testCreateCredentialWithNameOnlyException() throws Exception {
        given(CloudbreakClient.isCreated());
        given(Credential.request()
                .withName(EMPTY_CRED_NAME)
                .withDescription(CRED_DESCRIPTION));
        when(Credential.post());
    }

    @Test
    public void testCreateCredentialWithNameOnlyMessage() throws Exception {
        given(CloudbreakClient.isCreated());
        given(Credential.request()
                .withName(EMPTY_CRED_NAME)
                .withDescription(CRED_DESCRIPTION));
        try {
            when(Credential.post());
        } catch (BadRequestException e) {
            String exceptionMessage = e.getResponse().readEntity(String.class);
            this.errorMessage = exceptionMessage.substring(exceptionMessage.lastIndexOf(":") + 1);
            LOGGER.info("BadRequestException message ::: " + this.errorMessage);
        }
        then(Credential.assertThis(
                (credential, t) -> {
                    Assert.assertEquals(this.errorMessage, "\"may not be null\"}}");
                })
        );
    }
}
