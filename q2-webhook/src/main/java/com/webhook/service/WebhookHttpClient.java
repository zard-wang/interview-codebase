package com.webhook.service;

import com.webhook.model.HttpResult;

import java.util.Map;

public interface WebhookHttpClient {

    HttpResult send(String url, String payload, Map<String, String> headers);
}
