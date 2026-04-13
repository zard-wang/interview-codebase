package com.webhook.service;

import com.webhook.model.HttpResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DefaultWebhookHttpClient implements WebhookHttpClient {

    @Override
    public HttpResult send(String url, String payload, Map<String, String> headers) {
        try {
            Thread.sleep(100 + ThreadLocalRandom.current().nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean success = ThreadLocalRandom.current().nextDouble() < 0.8;
        if (success) {
            return new HttpResult(200, true);
        } else {
            int[] failureCodes = {500, 502, 503, 429};
            int code = failureCodes[ThreadLocalRandom.current().nextInt(failureCodes.length)];
            return new HttpResult(code, false);
        }
    }
}
