package com.aqppann.sqlanalyzer;

import org.testcontainers.DockerClientFactory;

public class DockerCondition {
    public static boolean isDockerRunning() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Exception e) {
            return false;
        }
    }
}
