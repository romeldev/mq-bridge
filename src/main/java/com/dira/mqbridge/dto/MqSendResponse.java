package com.dira.mqbridge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MqSendResponse {

    private boolean success;
    private String messageId;
    private String data;
    private String error;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int retries;

    public static MqSendResponse ok(String messageId, String data, int retries) {
        MqSendResponse r = new MqSendResponse();
        r.success = true;
        r.messageId = messageId;
        r.data = data;
        r.retries = retries;
        return r;
    }

    public static MqSendResponse fail(String error) {
        MqSendResponse r = new MqSendResponse();
        r.success = false;
        r.error = error;
        return r;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }
}
