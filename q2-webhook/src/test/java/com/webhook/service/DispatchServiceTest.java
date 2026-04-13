package com.webhook.service;

import com.webhook.model.*;
import com.webhook.repository.DeliveryLogRepository;
import com.webhook.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DispatchServiceTest {

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public WebhookHttpClient stubHttpClient() {
            return new StubWebhookHttpClient();
        }
    }

    static class StubWebhookHttpClient implements WebhookHttpClient {

        private HttpResult fixedResult = new HttpResult(200, true);
        private final List<Long> callTimestamps = new ArrayList<>();
        private final List<Map<String, String>> capturedHeaders = new ArrayList<>();

        void setFixedResult(HttpResult result) {
            this.fixedResult = result;
        }

        List<Long> getCallTimestamps() {
            return callTimestamps;
        }

        List<Map<String, String>> getCapturedHeaders() {
            return capturedHeaders;
        }

        void reset() {
            fixedResult = new HttpResult(200, true);
            callTimestamps.clear();
            capturedHeaders.clear();
        }

        @Override
        public HttpResult send(String url, String payload, Map<String, String> headers) {
            callTimestamps.add(System.currentTimeMillis());
            capturedHeaders.add(headers);
            return fixedResult;
        }
    }

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private DeliveryLogRepository deliveryLogRepository;

    @Autowired
    private WebhookHttpClient httpClient;

    private StubWebhookHttpClient stubClient() {
        return (StubWebhookHttpClient) httpClient;
    }

    @BeforeEach
    void setUp() {
        deliveryLogRepository.deleteAll();
        subscriptionRepository.deleteAll();
        stubClient().reset();
    }

    @Test
    void testDispatchSuccess() {
        Subscription sub = new Subscription();
        sub.setName("Order Service");
        sub.setUrl("https://orders.example.com/webhook");
        sub.setEventTypes("order.completed,order.cancelled");
        sub.setEnabled(true);
        subscriptionRepository.save(sub);

        stubClient().setFixedResult(new HttpResult(200, true));

        List<DeliveryLog> results = dispatchService.dispatch(
                "order.completed", "{\"orderId\": 42}");

        assertEquals(1, results.size());

        DeliveryLog log = results.get(0);
        assertEquals(DeliveryStatus.SUCCESS, log.getStatus());
        assertEquals(200, log.getHttpStatusCode());
        assertEquals("order.completed", log.getEventType());
        assertEquals(sub.getId(), log.getSubscriptionId());
    }

    @Test
    void testDispatchSkipsDisabledSubscription() {
        Subscription enabled = new Subscription();
        enabled.setName("Active Service");
        enabled.setUrl("https://active.example.com/webhook");
        enabled.setEventTypes("user.registered");
        enabled.setEnabled(true);
        subscriptionRepository.save(enabled);

        Subscription disabled = new Subscription();
        disabled.setName("Disabled Service");
        disabled.setUrl("https://disabled.example.com/webhook");
        disabled.setEventTypes("user.registered");
        disabled.setEnabled(false);
        subscriptionRepository.save(disabled);

        stubClient().setFixedResult(new HttpResult(200, true));

        List<DeliveryLog> results = dispatchService.dispatch(
                "user.registered", "{\"userId\": 1}");

        assertEquals(1, results.size());
        assertEquals(enabled.getId(), results.get(0).getSubscriptionId());
    }

    @Test
    void testDispatchFiltersEventType() {
        Subscription orderSub = new Subscription();
        orderSub.setName("Order Handler");
        orderSub.setUrl("https://orders.example.com/hook");
        orderSub.setEventTypes("order.completed");
        orderSub.setEnabled(true);
        subscriptionRepository.save(orderSub);

        Subscription userSub = new Subscription();
        userSub.setName("User Handler");
        userSub.setUrl("https://users.example.com/hook");
        userSub.setEventTypes("user.registered");
        userSub.setEnabled(true);
        subscriptionRepository.save(userSub);

        Subscription bothSub = new Subscription();
        bothSub.setName("Audit Service");
        bothSub.setUrl("https://audit.example.com/hook");
        bothSub.setEventTypes("order.completed,user.registered");
        bothSub.setEnabled(true);
        subscriptionRepository.save(bothSub);

        stubClient().setFixedResult(new HttpResult(200, true));

        List<DeliveryLog> results = dispatchService.dispatch(
                "order.completed", "{\"orderId\": 99}");

        assertEquals(2, results.size());

        List<Long> deliveredSubIds = results.stream()
                .map(DeliveryLog::getSubscriptionId)
                .sorted()
                .toList();
        assertTrue(deliveredSubIds.contains(orderSub.getId()));
        assertTrue(deliveredSubIds.contains(bothSub.getId()));
        assertFalse(deliveredSubIds.contains(userSub.getId()));
    }

    @Test
    void test_retry_uses_backoff() {
        Subscription sub = new Subscription();
        sub.setName("Failing Service");
        sub.setUrl("https://failing.example.com/webhook");
        sub.setEventTypes("payment.failed");
        sub.setEnabled(true);
        subscriptionRepository.save(sub);

        stubClient().setFixedResult(new HttpResult(500, false));

        dispatchService.dispatch("payment.failed", "{\"paymentId\": 7}");

        List<Long> timestamps = stubClient().getCallTimestamps();
        assertEquals(3, timestamps.size());

        long gap1 = timestamps.get(1) - timestamps.get(0);
        long gap2 = timestamps.get(2) - timestamps.get(1);

        assertTrue(gap2 > gap1 * 1.5,
                "Expected exponential backoff: second gap (" + gap2 +
                        "ms) should be at least 1.5x the first gap (" + gap1 + "ms)");
    }

    @Test
    void testRetryExhausted() {
        Subscription sub = new Subscription();
        sub.setName("Unstable Service");
        sub.setUrl("https://unstable.example.com/webhook");
        sub.setEventTypes("invoice.created");
        sub.setEnabled(true);
        subscriptionRepository.save(sub);

        stubClient().setFixedResult(new HttpResult(503, false));

        List<DeliveryLog> results = dispatchService.dispatch(
                "invoice.created", "{\"invoiceId\": 100}");

        assertEquals(1, results.size());

        DeliveryLog log = deliveryLogRepository.findById(results.get(0).getId()).orElseThrow();
        assertEquals(DeliveryStatus.EXHAUSTED, log.getStatus());
        assertEquals(3, log.getAttempts());
        assertEquals(503, log.getHttpStatusCode());
    }

    @Test
    void testDeliveryLogCreated() {
        Subscription sub = new Subscription();
        sub.setName("Analytics");
        sub.setUrl("https://analytics.example.com/events");
        sub.setSecret("my-secret-key");
        sub.setEventTypes("page.viewed,button.clicked");
        sub.setEnabled(true);
        subscriptionRepository.save(sub);

        stubClient().setFixedResult(new HttpResult(200, true));

        String payload = "{\"page\": \"/home\", \"userId\": 55}";
        dispatchService.dispatch("page.viewed", payload);

        List<DeliveryLog> logs = deliveryLogRepository.findBySubscriptionId(sub.getId());
        assertEquals(1, logs.size());

        DeliveryLog log = logs.get(0);
        assertEquals(sub.getId(), log.getSubscriptionId());
        assertEquals("page.viewed", log.getEventType());
        assertEquals(payload, log.getPayload());
        assertEquals(DeliveryStatus.SUCCESS, log.getStatus());
        assertEquals(200, log.getHttpStatusCode());
        assertNotNull(log.getLastAttemptAt());
        assertNotNull(log.getCreatedAt());
    }
}
