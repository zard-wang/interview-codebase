package com.interview.invitecode.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class NotificationService {

    private final List<String> sentNotifications = Collections.synchronizedList(new ArrayList<>());

    public void sendRedemptionNotification(String userId, String code) {
        String message = String.format("User %s redeemed code %s", userId, code);
        sentNotifications.add(message);
    }

    public List<String> getSentNotifications() {
        return new ArrayList<>(sentNotifications);
    }

    public void clearNotifications() {
        sentNotifications.clear();
    }
}
