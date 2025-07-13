package com.padudjayaputera.sistem_akuntansi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.padudjayaputera.sistem_akuntansi.model.Perusahaan;

public interface PerusahaanRepository extends JpaRepository<Perusahaan, Integer> {
    // Tambahkan custom query jika diperlukan
} 