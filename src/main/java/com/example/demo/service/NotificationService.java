package com.example.demo.service;

import com.example.demo.dto.response.NotificationResponse;
import com.example.demo.enums.NotificationType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void createNotification(Long userId, String title, String message,
                            NotificationType type, Long referenceId);

    Page<NotificationResponse> getMyNotifications(Long userId, Pageable pageable);

    long getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);
}