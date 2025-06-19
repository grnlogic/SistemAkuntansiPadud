package com.padudjayaputera.sistem_akuntansi.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.KeuanganSaldo;

@Repository
public interface KeuanganSaldoRepository extends JpaRepository<KeuanganSaldo, Integer> {
    
    List<KeuanganSaldo> findByTanggalTransaksi(LocalDate tanggal);
    
    List<KeuanganSaldo> findByAccountId(Integer accountId);
    
    @Query("SELECT ks FROM KeuanganSaldo ks WHERE ks.tanggalTransaksi BETWEEN :startDate AND :endDate")
    List<KeuanganSaldo> findByTanggalTransaksiBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT ks FROM KeuanganSaldo ks WHERE ks.account.division.id = :divisionId")
    List<KeuanganSaldo> findByDivisionId(@Param("divisionId") Integer divisionId);
    
    /**
     * ✅ IMPROVED: Find latest saldo across ALL dates for an account
     */
    @Query("SELECT k FROM KeuanganSaldo k WHERE k.account.id = :accountId ORDER BY k.tanggalTransaksi DESC, k.createdAt DESC LIMIT 1")
    Optional<KeuanganSaldo> findLatestByAccountId(@Param("accountId") Integer accountId);
    
    /**
     * ✅ NEW: Find all saldo records for specific account and date
     */
    @Query("SELECT k FROM KeuanganSaldo k WHERE k.account.id = :accountId AND k.tanggalTransaksi = :tanggal ORDER BY k.createdAt DESC")
    List<KeuanganSaldo> findByAccountIdAndTanggal(@Param("accountId") Integer accountId, @Param("tanggal") LocalDate tanggal);
    
    /**
     * ✅ NEW: Get running balance for an account up to a specific date
     */
    @Query("SELECT COALESCE(SUM(k.penerimaan) - SUM(k.pengeluaran), 0) FROM KeuanganSaldo k WHERE k.account.id = :accountId AND k.tanggalTransaksi <= :tanggal")
    BigDecimal getRunningBalanceUpToDate(@Param("accountId") Integer accountId, @Param("tanggal") LocalDate tanggal);
    
    // ✅ Unique constraint check
    Optional<KeuanganSaldo> findByAccountIdAndTanggalTransaksi(Integer accountId, LocalDate tanggalTransaksi);
}
