package com.ecommerce.notification.controller;

import com.ecommerce.notification.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Mock notification sender: logs instead of calling a real email/SMS provider.
 * Swap the log line for a JavaMailSender or Twilio client to make it real,
 * and trigger this from order-service via an event (Kafka/RabbitMQ) instead
 * of a direct call once you want full async decoupling.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @PostMapping
    public String send(@RequestBody NotificationRequest request) {
        log.info("Sending notification to {} | subject: {} | body: {}",
                request.toEmail(), request.subject(), request.body());
        return "Notification queued for " + request.toEmail();
    }
}
