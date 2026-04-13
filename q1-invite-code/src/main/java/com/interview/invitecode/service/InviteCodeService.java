package com.interview.invitecode.service;

import com.interview.invitecode.dto.*;
import com.interview.invitecode.entity.*;
import com.interview.invitecode.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class InviteCodeService {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SUFFIX_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CampaignRepository campaignRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final RedemptionRepository redemptionRepository;
    private final NotificationService notificationService;

    public InviteCodeService(CampaignRepository campaignRepository,
                             InviteCodeRepository inviteCodeRepository,
                             RedemptionRepository redemptionRepository,
                             NotificationService notificationService) {
        this.campaignRepository = campaignRepository;
        this.inviteCodeRepository = inviteCodeRepository;
        this.redemptionRepository = redemptionRepository;
        this.notificationService = notificationService;
    }

    public Campaign createCampaign(CreateCampaignRequest request) {
        Campaign campaign = new Campaign(
                request.getName(),
                request.getDescription(),
                request.getCodePrefix()
        );
        return campaignRepository.save(campaign);
    }

    public List<InviteCode> generateCodes(Long campaignId, GenerateCodesRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + campaignId));

        List<InviteCode> codes = new ArrayList<>();
        for (int i = 0; i < request.getCount(); i++) {
            String code = generateUniqueCode(campaign.getCodePrefix());
            InviteCode inviteCode = new InviteCode(
                    code,
                    campaignId,
                    request.getMaxRedemptions(),
                    request.getExpiresAt(),
                    request.getInviterId()
            );
            codes.add(inviteCodeRepository.save(inviteCode));
        }
        return codes;
    }

    public InviteCode getCode(String code) {
        return inviteCodeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Code not found: " + code));
    }

    public ValidationResult validate(String code) {
        InviteCode inviteCode = inviteCodeRepository.findByCode(code).orElse(null);

        if (inviteCode == null) {
            return ValidationResult.invalid("Code not found");
        }

        if (inviteCode.getStatus() != CodeStatus.ACTIVE) {
            return ValidationResult.invalid("Code is disabled");
        }

        if (inviteCode.getExpiresAt() != null && LocalDate.now().isAfter(inviteCode.getExpiresAt())) {
            return ValidationResult.invalid("Code has expired");
        }

        if (inviteCode.getCurrentRedemptions() >= inviteCode.getMaxRedemptions()) {
            return ValidationResult.invalid("Code has reached maximum redemptions");
        }

        return ValidationResult.valid();
    }

    @Transactional
    public RedeemResult redeem(String code, RedeemRequest request) {
        InviteCode inviteCode = inviteCodeRepository.findByCode(code).orElse(null);

        if (inviteCode == null) {
            return RedeemResult.failure("Code not found");
        }

        if (inviteCode.getStatus() != CodeStatus.ACTIVE) {
            return RedeemResult.failure("Code is disabled");
        }

        if (inviteCode.getExpiresAt() != null && LocalDate.now().isAfter(inviteCode.getExpiresAt())) {
            return RedeemResult.failure("Code has expired");
        }

        if (inviteCode.getCurrentRedemptions() >= inviteCode.getMaxRedemptions()) {
            return RedeemResult.failure("Code has reached maximum redemptions");
        }

        if (redemptionRepository.existsByCodeIdAndUserId(inviteCode.getId(), request.getUserId())) {
            return RedeemResult.failure("User has already redeemed this code");
        }

        inviteCode.setCurrentRedemptions(inviteCode.getCurrentRedemptions() + 1);
        inviteCodeRepository.save(inviteCode);

        Redemption redemption = new Redemption(inviteCode.getId(), request.getUserId());
        redemption = redemptionRepository.save(redemption);

        notificationService.sendRedemptionNotification(request.getUserId(), code);

        return RedeemResult.success(redemption.getId());
    }

    public List<Redemption> getRedemptions(String code) {
        InviteCode inviteCode = inviteCodeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Code not found: " + code));
        return redemptionRepository.findByCodeId(inviteCode.getId());
    }

    private String generateUniqueCode(String prefix) {
        String code;
        do {
            code = prefix + "-" + generateRandomSuffix();
        } while (inviteCodeRepository.existsByCode(code));
        return code;
    }

    private String generateRandomSuffix() {
        StringBuilder sb = new StringBuilder(SUFFIX_LENGTH);
        for (int i = 0; i < SUFFIX_LENGTH; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
