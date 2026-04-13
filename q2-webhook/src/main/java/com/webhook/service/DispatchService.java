package com.webhook.service;

import com.webhook.model.*;
import com.webhook.repository.DeliveryLogRepository;
import com.webhook.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DispatchService {

    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final WebhookHttpClient httpClient;

    @Value("${webhook.dispatch.max-retries:3}")
    private int maxRetries;

    @Value("${webhook.dispatch.retry-interval-ms:2000}")
    private long retryIntervalMs;

    public DispatchService(SubscriptionRepository subscriptionRepository,
                           DeliveryLogRepository deliveryLogRepository,
                           WebhookHttpClient httpClient) {
        this.subscriptionRepository = subscriptionRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.httpClient = httpClient;
    }

    public List<DeliveryLog> dispatch(String eventType, String payload) {
        List<Subscription> activeSubscriptions = subscriptionRepository.findByEnabledTrue();
        List<DeliveryLog> deliveryLogs = new ArrayList<>();

        for (Subscription subscription : activeSubscriptions) {
            if (!subscription.matchesEventType(eventType)) {
                continue;
            }

            DeliveryLog deliveryLog = new DeliveryLog();
            deliveryLog.setSubscriptionId(subscription.getId());
            deliveryLog.setEventType(eventType);
            deliveryLog.setPayload(payload);
            deliveryLog.setStatus(DeliveryStatus.PENDING);
            deliveryLog = deliveryLogRepository.save(deliveryLog);

            dispatchWithRetry(subscription, eventType, payload, deliveryLog);
            deliveryLogs.add(deliveryLog);
        }

        return deliveryLogs;
    }

    void dispatchWithRetry(Subscription subscription, String eventType,
                           String payload, DeliveryLog deliveryLog) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            boolean success = attemptDelivery(subscription, eventType, payload, deliveryLog);
            if (success) {
                return;
            }
            deliveryLog.setAttempts(attempt);
            deliveryLogRepository.save(deliveryLog);

            if (attempt < maxRetries) {
                try {
                    Thread.sleep(retryIntervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Retry sleep interrupted for subscription {}", subscription.getId());
                    return;
                }
            }
        }

        deliveryLog.setStatus(DeliveryStatus.EXHAUSTED);
        deliveryLogRepository.save(deliveryLog);
        log.warn("All retries exhausted for subscription {} event {}",
                subscription.getId(), eventType);
    }

    private boolean attemptDelivery(Subscription subscription, String eventType,
                                    String payload, DeliveryLog deliveryLog) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Webhook-Event", eventType);

        try {
            HttpResult result = httpClient.send(subscription.getUrl(), payload, headers);
            deliveryLog.setHttpStatusCode(result.statusCode());
            deliveryLog.setLastAttemptAt(LocalDateTime.now());

            if (result.success()) {
                deliveryLog.setStatus(DeliveryStatus.SUCCESS);
                deliveryLog.setAttempts(deliveryLog.getAttempts() + 1);
                deliveryLogRepository.save(deliveryLog);
                log.info("Successfully delivered to subscription {} at {}",
                        subscription.getId(), subscription.getUrl());
                return true;
            } else {
                deliveryLog.setStatus(DeliveryStatus.FAILED);
                log.info("Delivery attempt failed for subscription {} with status {}",
                        subscription.getId(), result.statusCode());
                return false;
            }
        } catch (Exception e) {
            deliveryLog.setLastAttemptAt(LocalDateTime.now());
            deliveryLog.setStatus(DeliveryStatus.FAILED);
            log.error("Error delivering to subscription {}: {}",
                    subscription.getId(), e.getMessage());
            return false;
        }
    }
}
