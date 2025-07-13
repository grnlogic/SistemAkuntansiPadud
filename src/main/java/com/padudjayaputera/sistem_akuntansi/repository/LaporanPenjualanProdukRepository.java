package com.padudjayaputera.sistem_akuntansi.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.model.LaporanPenjualanProduk;
import com.padudjayaputera.sistem_akuntansi.model.Salesperson;

@Repository
public interface LaporanPenjualanProdukRepository extends JpaRepository<LaporanPenjualanProduk, Integer> {

    // Method untuk logika UPSERT (Update or Insert) nanti di service
    Optional<LaporanPenjualanProduk> findByTanggalLaporanAndSalespersonAndProductAccount(
            LocalDate tanggalLaporan, 
            Salesperson salesperson, 
            Account productAccount
    );
    
    // Find by salesperson
    List<LaporanPenjualanProduk> findBySalespersonId(Integer salespersonId);
    
    // Find by tanggal range
    List<LaporanPenjualanProduk> findByTanggalLaporanBetween(LocalDate startDate, LocalDate endDate);
    
    // Find by salesperson and tanggal range
    List<LaporanPenjualanProduk> findBySalespersonIdAndTanggalLaporanBetween(
        Integer salespersonId, LocalDate startDate, LocalDate endDate);
    

    
    /**
     * Cari laporan penjualan produk berdasarkan user ID
     */
    @Query("SELECT lpp FROM LaporanPenjualanProduk lpp WHERE lpp.createdBy.id = :userId")
    List<LaporanPenjualanProduk> findByUserId(@Param("userId") Integer userId);
}