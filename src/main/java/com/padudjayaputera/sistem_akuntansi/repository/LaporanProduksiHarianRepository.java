package com.padudjayaputera.sistem_akuntansi.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.model.LaporanProduksiHarian;

@Repository
public interface LaporanProduksiHarianRepository extends JpaRepository<LaporanProduksiHarian, Integer> {
    // Method untuk logika UPSERT (Update or Insert)
    Optional<LaporanProduksiHarian> findByTanggalLaporanAndAccount(LocalDate tanggalLaporan, Account account);

    /**
     * Cari laporan produksi berdasarkan user ID
     */
    @Query("SELECT l FROM LaporanProduksiHarian l WHERE l.createdBy.id = :userId")
    List<LaporanProduksiHarian> findByUserId(@Param("userId") Integer userId);
}