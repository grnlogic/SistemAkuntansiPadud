package com.padudjayaputera.sistem_akuntansi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.PiutangTransaksi;

@Repository
public interface PiutangTransaksiRepository extends JpaRepository<PiutangTransaksi, Integer> {

    // Di masa depan, kita bisa dengan mudah menambahkan method pencarian custom di sini.
    // Contoh:
    // List<PiutangTransaksi> findByTanggalTransaksiBetween(LocalDate awal, LocalDate akhir);

    /**
     * Cari piutang berdasarkan user ID
     */
    @Query("SELECT p FROM PiutangTransaksi p WHERE p.user.id = :userId")
    List<PiutangTransaksi> findByUserId(@Param("userId") Integer userId);
}