package com.webhook.service;

import com.webhook.model.Subscription;
import com.webhook.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription create(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    public List<Subscription> listAll() {
        return subscriptionRepository.findAll();
    }

    public Optional<Subscription> findById(Long id) {
        return subscriptionRepository.findById(id);
    }

    public Optional<Subscription> enable(Long id) {
        return subscriptionRepository.findById(id).map(sub -> {
            sub.setEnabled(true);
            return subscriptionRepository.save(sub);
        });
    }

    public Optional<Subscription> disable(Long id) {
        return subscriptionRepository.findById(id).map(sub -> {
            sub.setEnabled(false);
            return subscriptionRepository.save(sub);
        });
    }
}
