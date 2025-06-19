package com.padudjayaputera.sistem_akuntansi.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.EntriHarian;

@Repository
public interface EntriHarianRepository extends JpaRepository<EntriHarian, Integer> {
    
    /**
     * Mencari entri harian berdasarkan tanggal laporan.
     */
    List<EntriHarian> findByTanggalLaporan(LocalDate tanggal);
    
    /**
     * Mencari entri harian berdasarkan divisi dari akun.
     */
    @Query("SELECT e FROM EntriHarian e WHERE e.account.division.id = :divisionId")
    List<EntriHarian> findByAccountDivisionId(@Param("divisionId") Integer divisionId);
    
    /**
     * Mencari entri harian berdasarkan tanggal dan divisi.
     */
    @Query("SELECT e FROM EntriHarian e WHERE e.tanggalLaporan = :tanggal AND e.account.division.id = :divisionId")
    List<EntriHarian> findByTanggalLaporanAndAccountDivisionId(@Param("tanggal") LocalDate tanggal, @Param("divisionId") Integer divisionId);
    
    /**
     * ✅ NEW: Mencari entri berdasarkan tanggal dan account ID untuk handle duplicates.
     */
    @Query("SELECT e FROM EntriHarian e WHERE e.tanggalLaporan = :tanggal AND e.account.id = :accountId")
    Optional<EntriHarian> findByTanggalLaporanAndAccountId(@Param("tanggal") LocalDate tanggal, @Param("accountId") Integer accountId);
    
    /**
     * ✅ NEW: Mencari semua entri untuk account tertentu pada tanggal tertentu (jika ada multiple)
     */
    @Query("SELECT e FROM EntriHarian e WHERE e.tanggalLaporan = :tanggal AND e.account.id = :accountId ORDER BY e.createdAt DESC")
    List<EntriHarian> findAllByTanggalLaporanAndAccountId(@Param("tanggal") LocalDate tanggal, @Param("accountId") Integer accountId);
    
    /**
     * ✅ NEW: Mencari entri berdasarkan tanggal, account ID, dan transaction type (untuk keuangan)
     */
    @Query("SELECT e FROM EntriHarian e WHERE e.tanggalLaporan = :tanggal AND e.account.id = :accountId AND e.transactionType = :transactionType")
    Optional<EntriHarian> findByTanggalLaporanAndAccountIdAndTransactionType(
            @Param("tanggal") LocalDate tanggal, 
            @Param("accountId") Integer accountId,
            @Param("transactionType") com.padudjayaputera.sistem_akuntansi.model.TransactionType transactionType);
    
    /**
     * ✅ NEW: Mencari semua entri keuangan untuk account dan tanggal tertentu
     */
    @Query("SELECT e FROM EntriHarian e WHERE e.tanggalLaporan = :tanggal AND e.account.id = :accountId AND e.transactionType IS NOT NULL ORDER BY e.createdAt DESC")
    List<EntriHarian> findKeuanganEntriesByTanggalAndAccountId(@Param("tanggal") LocalDate tanggal, @Param("accountId") Integer accountId);
    
    /**
     * ✅ NEW: Get daily summary untuk keuangan (total penerimaan dan pengeluaran)
     */
    @Query("SELECT " +
           "COALESCE(SUM(CASE WHEN e.transactionType = 'PENERIMAAN' THEN e.nilai ELSE 0 END), 0) as totalPenerimaan, " +
           "COALESCE(SUM(CASE WHEN e.transactionType = 'PENGELUARAN' THEN e.nilai ELSE 0 END), 0) as totalPengeluaran " +
           "FROM EntriHarian e WHERE e.tanggalLaporan = :tanggal AND e.account.id = :accountId AND e.transactionType IS NOT NULL")
    Object[] getDailyCashSummary(@Param("tanggal") LocalDate tanggal, @Param("accountId") Integer accountId);
}