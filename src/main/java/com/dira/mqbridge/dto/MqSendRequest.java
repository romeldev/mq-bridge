package com.dira.mqbridge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MqSendRequest {

    private MqCredentials credentials;

    @JsonProperty("input_queue")
    private String inputQueue;

    @JsonProperty("output_queue")
    private String outputQueue;

    private String message;

    private MqOptions options;

    public MqCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(MqCredentials credentials) {
        this.credentials = credentials;
    }

    public String getInputQueue() {
        return inputQueue;
    }

    public void setInputQueue(String inputQueue) {
        this.inputQueue = inputQueue;
    }

    public String getOutputQueue() {
        return outputQueue;
    }

    public void setOutputQueue(String outputQueue) {
        this.outputQueue = outputQueue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MqOptions getOptions() {
        return options;
    }

    public void setOptions(MqOptions options) {
        this.options = options;
    }
}
