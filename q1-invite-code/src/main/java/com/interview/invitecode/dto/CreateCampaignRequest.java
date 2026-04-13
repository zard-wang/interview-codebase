package com.interview.invitecode.dto;

public class CreateCampaignRequest {

    private String name;
    private String description;
    private String codePrefix;

    public CreateCampaignRequest() {
    }

    public CreateCampaignRequest(String name, String description, String codePrefix) {
        this.name = name;
        this.description = description;
        this.codePrefix = codePrefix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCodePrefix() {
        return codePrefix;
    }

    public void setCodePrefix(String codePrefix) {
        this.codePrefix = codePrefix;
    }
}
