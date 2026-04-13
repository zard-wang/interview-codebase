package com.interview.invitecode;

import com.interview.invitecode.dto.*;
import com.interview.invitecode.entity.*;
import com.interview.invitecode.repository.*;
import com.interview.invitecode.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InviteCodeServiceTest {

    @Autowired
    private InviteCodeService inviteCodeService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private InviteCodeRepository inviteCodeRepository;

    @Autowired
    private RedemptionRepository redemptionRepository;

    private Campaign campaign;

    @BeforeEach
    void setUp() {
        redemptionRepository.deleteAll();
        inviteCodeRepository.deleteAll();
        campaignRepository.deleteAll();
        notificationService.clearNotifications();

        CreateCampaignRequest campaignRequest = new CreateCampaignRequest(
                "Test Campaign", "A test campaign", "TEST"
        );
        campaign = inviteCodeService.createCampaign(campaignRequest);
    }

    @Test
    void testRedeemSuccess() {
        GenerateCodesRequest genRequest = new GenerateCodesRequest(
                1, 10, LocalDate.now().plusDays(30), "inviter-1"
        );
        List<InviteCode> codes = inviteCodeService.generateCodes(campaign.getId(), genRequest);
        String code = codes.get(0).getCode();

        RedeemResult result = inviteCodeService.redeem(code, new RedeemRequest("user-1"));

        assertTrue(result.isSuccess());
        assertNotNull(result.getRedemptionId());
        assertEquals("Code redeemed successfully", result.getMessage());

        InviteCode updated = inviteCodeService.getCode(code);
        assertEquals(1, updated.getCurrentRedemptions());
    }

    @Test
    void testRedeemExhausted() {
        GenerateCodesRequest genRequest = new GenerateCodesRequest(
                1, 2, LocalDate.now().plusDays(30), "inviter-1"
        );
        List<InviteCode> codes = inviteCodeService.generateCodes(campaign.getId(), genRequest);
        String code = codes.get(0).getCode();

        inviteCodeService.redeem(code, new RedeemRequest("user-1"));
        inviteCodeService.redeem(code, new RedeemRequest("user-2"));

        RedeemResult result = inviteCodeService.redeem(code, new RedeemRequest("user-3"));

        assertFalse(result.isSuccess());
        assertEquals("Code has reached maximum redemptions", result.getMessage());
    }

    @Test
    void testRedeemExpiredCode() {
        GenerateCodesRequest genRequest = new GenerateCodesRequest(
                1, 10, LocalDate.now().minusDays(1), "inviter-1"
        );
        List<InviteCode> codes = inviteCodeService.generateCodes(campaign.getId(), genRequest);
        String code = codes.get(0).getCode();

        RedeemResult result = inviteCodeService.redeem(code, new RedeemRequest("user-1"));

        assertFalse(result.isSuccess());
        assertEquals("Code has expired", result.getMessage());
    }

    @Test
    void testValidateActiveCode() {
        GenerateCodesRequest genRequest = new GenerateCodesRequest(
                1, 10, LocalDate.now().plusDays(30), "inviter-1"
        );
        List<InviteCode> codes = inviteCodeService.generateCodes(campaign.getId(), genRequest);
        String code = codes.get(0).getCode();

        ValidationResult result = inviteCodeService.validate(code);

        assertTrue(result.isValid());
        assertEquals("Code is valid", result.getReason());
    }

    @Test
    void testDuplicateRedemption() {
        GenerateCodesRequest genRequest = new GenerateCodesRequest(
                1, 10, LocalDate.now().plusDays(30), "inviter-1"
        );
        List<InviteCode> codes = inviteCodeService.generateCodes(campaign.getId(), genRequest);
        String code = codes.get(0).getCode();

        RedeemResult first = inviteCodeService.redeem(code, new RedeemRequest("user-1"));
        assertTrue(first.isSuccess());

        RedeemResult second = inviteCodeService.redeem(code, new RedeemRequest("user-1"));
        assertFalse(second.isSuccess());
        assertEquals("User has already redeemed this code", second.getMessage());
    }

    @Test
    void test_concurrent_redemption() throws InterruptedException {
        GenerateCodesRequest genRequest = new GenerateCodesRequest(
                1, 5, LocalDate.now().plusDays(30), "inviter-1"
        );
        List<InviteCode> codes = inviteCodeService.generateCodes(campaign.getId(), genRequest);
        String code = codes.get(0).getCode();

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final String userId = "concurrent-user-" + i;
            executor.submit(() -> {
                try {
                    latch.await();
                    RedeemResult result = inviteCodeService.redeem(code, new RedeemRequest(userId));
                    if (result.isSuccess()) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        InviteCode updated = inviteCodeService.getCode(code);

        assertTrue(successCount.get() <= 5,
                "Expected at most 5 successful redemptions but got " + successCount.get()
                        + ". currentRedemptions=" + updated.getCurrentRedemptions());
    }

    @Test
    void testNotificationSentOnRedeem() {
        GenerateCodesRequest genRequest = new GenerateCodesRequest(
                1, 10, LocalDate.now().plusDays(30), "inviter-1"
        );
        List<InviteCode> codes = inviteCodeService.generateCodes(campaign.getId(), genRequest);
        String code = codes.get(0).getCode();

        inviteCodeService.redeem(code, new RedeemRequest("user-notify"));

        List<String> notifications = notificationService.getSentNotifications();
        assertEquals(1, notifications.size());
        assertTrue(notifications.get(0).contains("user-notify"));
        assertTrue(notifications.get(0).contains(code));
    }
}
