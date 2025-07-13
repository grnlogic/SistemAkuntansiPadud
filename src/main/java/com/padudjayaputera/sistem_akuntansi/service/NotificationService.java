package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;

import com.padudjayaputera.sistem_akuntansi.model.Notification;
import com.padudjayaputera.sistem_akuntansi.model.User;

public interface NotificationService {
    // Method untuk dipanggil oleh service lain saat ingin membuat notifikasi
    void createNotification(User recipient, String message, String linkUrl);

    // Method untuk mendapatkan notifikasi milik seorang pengguna
    List<Notification> getNotificationsForUser(Integer userId);

    // Method untuk menandai notifikasi sebagai sudah dibaca
    void markAsRead(Integer notificationId);
    
    // Method untuk mengirim notifikasi ke semua user (untuk Super Admin)
    void sendNotificationToAllUsers(String message, String linkUrl);
}