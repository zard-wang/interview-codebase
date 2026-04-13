package com.webhook.controller;

import com.webhook.model.DeliveryLog;
import com.webhook.service.DispatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final DispatchService dispatchService;

    public WebhookController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping("/dispatch")
    public ResponseEntity<Map<String, Object>> dispatch(@RequestBody DispatchRequest request) {
        if (request.getEventType() == null || request.getEventType().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "eventType is required"));
        }

        List<DeliveryLog> deliveries = dispatchService.dispatch(
                request.getEventType(), request.getPayload());

        return ResponseEntity.ok(Map.of(
                "eventType", request.getEventType(),
                "deliveriesCreated", deliveries.size(),
                "deliveries", deliveries
        ));
    }
}
