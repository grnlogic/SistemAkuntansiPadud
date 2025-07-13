package com.padudjayaputera.sistem_akuntansi.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.Notification;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Mencari semua notifikasi untuk satu pengguna, diurutkan dari yang terbaru
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);

    // Menghitung jumlah notifikasi yang belum dibaca untuk satu pengguna
    long countByUserIdAndIsRead(Integer userId, boolean isRead);

    // Method baru untuk menghapus notifikasi sebelum tanggal tertentu
    void deleteByCreatedAtBefore(LocalDateTime timestamp);
}