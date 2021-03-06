package com.sequenceiq.cloudbreak.service.cluster;

import com.sequenceiq.cloudbreak.service.CloudbreakServiceException;

public class AmbariServiceException extends CloudbreakServiceException {

    public AmbariServiceException(String message) {
        super(message);
    }

    public AmbariServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
