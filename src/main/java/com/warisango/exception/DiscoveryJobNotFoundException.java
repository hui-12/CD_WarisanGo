package com.warisango.exception;

public class DiscoveryJobNotFoundException extends RuntimeException {

    public DiscoveryJobNotFoundException(String jobId) {
        super("Discovery processing job was not found: " + jobId);
    }
}
