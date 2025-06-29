package com.padudjayaputera.sistem_akuntansi.repository;

import java.time.LocalDate;
import java.util.Optional; 

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.LaporanPenjualanSales; 
import com.padudjayaputera.sistem_akuntansi.model.Salesperson; 

@Repository
public interface LaporanPenjualanSalesRepository extends JpaRepository<LaporanPenjualanSales, Integer> {

    // TAMBAHKAN METHOD INI
    Optional<LaporanPenjualanSales> findByTanggalLaporanAndSalesperson(LocalDate tanggalLaporan, Salesperson salesperson);

}