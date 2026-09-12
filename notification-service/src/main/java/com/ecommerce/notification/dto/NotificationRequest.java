package com.ecommerce.notification.dto;

public record NotificationRequest(String toEmail, String subject, String body) {}
