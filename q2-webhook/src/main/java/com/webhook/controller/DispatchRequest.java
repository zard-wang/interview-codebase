package com.webhook.controller;

public class DispatchRequest {

    private String eventType;
    private String payload;

    public DispatchRequest() {
    }

    public DispatchRequest(String eventType, String payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
