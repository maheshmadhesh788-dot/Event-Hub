package com.example.demo.service;

import com.example.demo.model.Notification;
import java.util.List;

public interface NotificationService {
    List<Notification> getRecentNotifications();
    Notification createNotification(Notification notification);
    void deleteNotification(Long id);
}
