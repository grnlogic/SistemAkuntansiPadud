package com.padudjayaputera.sistem_akuntansi.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.model.LaporanGudangHarian;

@Repository
public interface LaporanGudangHarianRepository extends JpaRepository<LaporanGudangHarian, Integer> {
    Optional<LaporanGudangHarian> findByTanggalLaporanAndAccount(LocalDate tanggalLaporan, Account account);

    /**
     * Cari laporan gudang berdasarkan user ID
     */
    @Query("SELECT l FROM LaporanGudangHarian l WHERE l.createdBy.id = :userId")
    List<LaporanGudangHarian> findByUserId(@Param("userId") Integer userId);

    /**
     * Cari stok akhir dari hari sebelumnya untuk digunakan sebagai stok awal default
     */
    @Query("SELECT l FROM LaporanGudangHarian l WHERE l.account.id = :accountId AND l.tanggalLaporan < :tanggal ORDER BY l.tanggalLaporan DESC LIMIT 1")
    Optional<LaporanGudangHarian> findLatestStokAkhirByAccountAndDate(
        @Param("accountId") Integer accountId, 
        @Param("tanggal") LocalDate tanggal
    );

    /**
     * Cari laporan gudang berdasarkan account dan tanggal untuk validasi duplikasi
     */
    @Query("SELECT l FROM LaporanGudangHarian l WHERE l.account.id = :accountId AND l.tanggalLaporan = :tanggal")
    Optional<LaporanGudangHarian> findByAccountIdAndTanggal(
        @Param("accountId") Integer accountId, 
        @Param("tanggal") LocalDate tanggal
    );
}