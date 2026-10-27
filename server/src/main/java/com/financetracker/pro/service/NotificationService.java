package com.financetracker.pro.service;

import com.financetracker.pro.document.Notification;

import java.util.List;

public interface NotificationService {
    List<Notification> getNotifications(String userId);
    void markAsRead(String userId, String id);
    void markAllAsRead(String userId);
    Notification createNotification(String userId, String title, String message, String type);
}
