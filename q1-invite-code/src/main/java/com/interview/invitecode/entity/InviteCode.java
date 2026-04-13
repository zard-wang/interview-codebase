package com.interview.invitecode.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invite_codes")
public class InviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Long campaignId;

    @Column(nullable = false)
    private int maxRedemptions;

    @Column(nullable = false)
    private int currentRedemptions;

    private LocalDate expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CodeStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String inviterId;

    public InviteCode() {
    }

    public InviteCode(String code, Long campaignId, int maxRedemptions,
                      LocalDate expiresAt, String inviterId) {
        this.code = code;
        this.campaignId = campaignId;
        this.maxRedemptions = maxRedemptions;
        this.currentRedemptions = 0;
        this.expiresAt = expiresAt;
        this.status = CodeStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.inviterId = inviterId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public int getMaxRedemptions() {
        return maxRedemptions;
    }

    public void setMaxRedemptions(int maxRedemptions) {
        this.maxRedemptions = maxRedemptions;
    }

    public int getCurrentRedemptions() {
        return currentRedemptions;
    }

    public void setCurrentRedemptions(int currentRedemptions) {
        this.currentRedemptions = currentRedemptions;
    }

    public LocalDate getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDate expiresAt) {
        this.expiresAt = expiresAt;
    }

    public CodeStatus getStatus() {
        return status;
    }

    public void setStatus(CodeStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getInviterId() {
        return inviterId;
    }

    public void setInviterId(String inviterId) {
        this.inviterId = inviterId;
    }
}
