package com.example.attendancesystem.dto;

public class ImportResult {
    private int successCount = 0;
    private int failCount = 0;
    private StringBuilder errorMessages = new StringBuilder();

    public void incrementSuccess() {
        this.successCount++;
    }

    public void incrementFail() {
        this.failCount++;
    }

    public void addErrorMessage(String message) {
        this.errorMessages.append(message).append("\n");
    }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }

    public int getFailCount() { return failCount; }
    public void setFailCount(int failCount) { this.failCount = failCount; }

    public String getErrorMessages() { return errorMessages.toString(); }
    public void setErrorMessages(String errorMessages) { this.errorMessages = new StringBuilder(errorMessages); }
}