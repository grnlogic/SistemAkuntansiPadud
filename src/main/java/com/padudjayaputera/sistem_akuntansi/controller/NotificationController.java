package com.padudjayaputera.sistem_akuntansi.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.padudjayaputera.sistem_akuntansi.dto.SendNotificationRequest;
import com.padudjayaputera.sistem_akuntansi.model.Notification;
import com.padudjayaputera.sistem_akuntansi.model.User;
import com.padudjayaputera.sistem_akuntansi.service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Endpoint untuk mengambil semua notifikasi milik pengguna yang sedang login
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Notification>> getUserNotifications() {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Notification> notifications = notificationService.getNotificationsForUser(loggedInUser.getId());
        return ResponseEntity.ok(notifications);
    }

    // Endpoint untuk menandai notifikasi sebagai sudah dibaca
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Integer id) {
        // Tambahan: Seharusnya ada validasi untuk memastikan user hanya bisa mengubah notifikasi miliknya
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
    
    // ✅ NEW: Endpoint untuk Super Admin mengirim notifikasi ke semua user
    @PostMapping("/send")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> sendNotificationToAllUsers(@Valid @RequestBody SendNotificationRequest request) {
        try {
            User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            System.out.println("🔔 NOTIFICATION SEND REQUEST:");
            System.out.println("Sender: " + loggedInUser.getUsername());
            System.out.println("Message: " + request.getMessage());
            System.out.println("Link URL: " + request.getLinkUrl());
            
            // Kirim notifikasi ke semua user
            notificationService.sendNotificationToAllUsers(
                request.getMessage(), 
                request.getLinkUrl()
            );
            
            return ResponseEntity.ok("Notifikasi berhasil dikirim ke semua pengguna");
            
        } catch (Exception e) {
            System.err.println("❌ Error sending notification: " + e.getMessage());
            return ResponseEntity.status(500)
                .body("Gagal mengirim notifikasi: " + e.getMessage());
        }
    }
}