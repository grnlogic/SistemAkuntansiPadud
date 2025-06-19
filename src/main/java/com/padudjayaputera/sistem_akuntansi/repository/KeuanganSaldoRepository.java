package com.padudjayaputera.sistem_akuntansi.repository;

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
    
    // ✅ Helper untuk mendapatkan saldo terakhir per account
    @Query("SELECT ks FROM KeuanganSaldo ks WHERE ks.account.id = :accountId ORDER BY ks.tanggalTransaksi DESC LIMIT 1")
    Optional<KeuanganSaldo> findLatestByAccountId(@Param("accountId") Integer accountId);
    
    // ✅ Unique constraint check
    Optional<KeuanganSaldo> findByAccountIdAndTanggalTransaksi(Integer accountId, LocalDate tanggalTransaksi);
}
