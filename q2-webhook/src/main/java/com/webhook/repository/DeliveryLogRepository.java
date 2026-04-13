package com.webhook.repository;

import com.webhook.model.DeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {

    List<DeliveryLog> findBySubscriptionId(Long subscriptionId);

    List<DeliveryLog> findByEventType(String eventType);
}
