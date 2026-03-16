package com.dira.mqbridge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MqOptions {

    private Integer retries = 1;

    @JsonProperty("retry_wait_ms")
    private Integer retryWaitMs = 4000;

    @JsonProperty("timeout_ms")
    private Integer timeoutMs = 3000;

    private String format = "string";

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Integer getRetryWaitMs() {
        return retryWaitMs;
    }

    public void setRetryWaitMs(Integer retryWaitMs) {
        this.retryWaitMs = retryWaitMs;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
