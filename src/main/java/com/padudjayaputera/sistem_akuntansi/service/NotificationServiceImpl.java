package com.padudjayaputera.sistem_akuntansi.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.padudjayaputera.sistem_akuntansi.model.Notification;
import com.padudjayaputera.sistem_akuntansi.model.User;
import com.padudjayaputera.sistem_akuntansi.repository.NotificationRepository;
import com.padudjayaputera.sistem_akuntansi.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository; // ✅ ADD: Inject UserRepository

    @Override
    public void createNotification(User recipient, String message, String linkUrl) {
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setMessage(message);
        notification.setLinkUrl(linkUrl);
        notification.setRead(false); // Set default sebagai belum dibaca
        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getNotificationsForUser(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public void markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notifikasi tidak ditemukan"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void sendNotificationToAllUsers(String message, String linkUrl) {
        // ✅ NEW: Ambil semua user yang aktif
        List<User> allUsers = userRepository.findAll();
        
        System.out.println("📢 SENDING NOTIFICATION TO ALL USERS:");
        System.out.println("Message: " + message);
        System.out.println("Link URL: " + linkUrl);
        System.out.println("Total users: " + allUsers.size());
        
        // ✅ Kirim notifikasi ke setiap user
        for (User user : allUsers) {
            try {
                createNotification(user, message, linkUrl);
                System.out.println("✅ Notification sent to user: " + user.getUsername());
            } catch (Exception e) {
                System.err.println("❌ Failed to send notification to user: " + user.getUsername() + " - " + e.getMessage());
            }
        }
        
        System.out.println("📢 NOTIFICATION BROADCAST COMPLETED");
    }

    /**
     * Tugas terjadwal yang berjalan setiap hari pada jam 1 pagi
     * untuk menghapus notifikasi yang lebih tua dari 1 hari.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanupOldNotifications() {
        System.out.println("Running scheduled job: Deleting old notifications...");
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        notificationRepository.deleteByCreatedAtBefore(oneDayAgo);
        System.out.println("Old notifications cleanup finished.");
    }
}