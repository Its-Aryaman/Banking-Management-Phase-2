package com.example.demo.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "notification-service")

public interface NotificationClient {
  @PostMapping("/api/notifications/send")
  void sendNotification(NotificationPayload payload);
}
