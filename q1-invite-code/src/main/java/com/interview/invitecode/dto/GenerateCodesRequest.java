package com.interview.invitecode.dto;

import java.time.LocalDate;

public class GenerateCodesRequest {

    private int count;
    private int maxRedemptions;
    private LocalDate expiresAt;
    private String inviterId;

    public GenerateCodesRequest() {
    }

    public GenerateCodesRequest(int count, int maxRedemptions, LocalDate expiresAt, String inviterId) {
        this.count = count;
        this.maxRedemptions = maxRedemptions;
        this.expiresAt = expiresAt;
        this.inviterId = inviterId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getMaxRedemptions() {
        return maxRedemptions;
    }

    public void setMaxRedemptions(int maxRedemptions) {
        this.maxRedemptions = maxRedemptions;
    }

    public LocalDate getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDate expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getInviterId() {
        return inviterId;
    }

    public void setInviterId(String inviterId) {
        this.inviterId = inviterId;
    }
}
