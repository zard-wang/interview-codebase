package com.webhook.controller;

import com.webhook.model.DeliveryLog;
import com.webhook.service.DeliveryLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryLogService deliveryLogService;

    public DeliveryController(DeliveryLogService deliveryLogService) {
        this.deliveryLogService = deliveryLogService;
    }

    @GetMapping
    public ResponseEntity<List<DeliveryLog>> listAll() {
        return ResponseEntity.ok(deliveryLogService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryLog> getById(@PathVariable Long id) {
        return deliveryLogService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
