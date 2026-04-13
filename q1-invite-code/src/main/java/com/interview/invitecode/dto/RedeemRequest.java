package com.interview.invitecode.dto;

public class RedeemRequest {

    private String userId;

    public RedeemRequest() {
    }

    public RedeemRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
