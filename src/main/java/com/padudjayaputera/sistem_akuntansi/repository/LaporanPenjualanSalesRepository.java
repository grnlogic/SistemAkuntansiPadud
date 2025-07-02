package com.padudjayaputera.sistem_akuntansi.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; 

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.LaporanPenjualanSales; 
import com.padudjayaputera.sistem_akuntansi.model.Salesperson; 

@Repository
public interface LaporanPenjualanSalesRepository extends JpaRepository<LaporanPenjualanSales, Integer> {

    // TAMBAHKAN METHOD INI
    Optional<LaporanPenjualanSales> findByTanggalLaporanAndSalesperson(LocalDate tanggalLaporan, Salesperson salesperson);

    /**
     * Cari laporan penjualan sales berdasarkan user ID
     */
    @Query("SELECT l FROM LaporanPenjualanSales l WHERE l.createdBy.id = :userId")
    List<LaporanPenjualanSales> findByUserId(@Param("userId") Integer userId);
}