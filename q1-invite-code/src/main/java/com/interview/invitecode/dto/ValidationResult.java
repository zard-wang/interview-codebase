package com.interview.invitecode.dto;

public class ValidationResult {

    private boolean valid;
    private String reason;

    public ValidationResult() {
    }

    public ValidationResult(boolean valid, String reason) {
        this.valid = valid;
        this.reason = reason;
    }

    public static ValidationResult valid() {
        return new ValidationResult(true, "Code is valid");
    }

    public static ValidationResult invalid(String reason) {
        return new ValidationResult(false, reason);
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
