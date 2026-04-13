package com.webhook.service;

import com.webhook.model.DeliveryLog;
import com.webhook.repository.DeliveryLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeliveryLogService {

    private final DeliveryLogRepository deliveryLogRepository;

    public DeliveryLogService(DeliveryLogRepository deliveryLogRepository) {
        this.deliveryLogRepository = deliveryLogRepository;
    }

    public List<DeliveryLog> listAll() {
        return deliveryLogRepository.findAll();
    }

    public Optional<DeliveryLog> findById(Long id) {
        return deliveryLogRepository.findById(id);
    }

    public List<DeliveryLog> findBySubscriptionId(Long subscriptionId) {
        return deliveryLogRepository.findBySubscriptionId(subscriptionId);
    }
}
