package com.interview.invitecode.dto;

public class RedeemResult {

    private boolean success;
    private String message;
    private Long redemptionId;

    public RedeemResult() {
    }

    public RedeemResult(boolean success, String message, Long redemptionId) {
        this.success = success;
        this.message = message;
        this.redemptionId = redemptionId;
    }

    public static RedeemResult success(Long redemptionId) {
        return new RedeemResult(true, "Code redeemed successfully", redemptionId);
    }

    public static RedeemResult failure(String message) {
        return new RedeemResult(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getRedemptionId() {
        return redemptionId;
    }

    public void setRedemptionId(Long redemptionId) {
        this.redemptionId = redemptionId;
    }
}
