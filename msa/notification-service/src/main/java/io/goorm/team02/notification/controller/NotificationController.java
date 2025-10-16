package io.goorm.team02.notification.controller;

import io.goorm.team02.notification.dto.NotificationResponse;
import io.goorm.team02.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApiDocs {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getUserNotificationsPage(
            @PathVariable Long userId,
            Pageable pageable) {

        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable)
                .map(NotificationResponse::from);

        return ResponseEntity.ok(notifications);
    }
}