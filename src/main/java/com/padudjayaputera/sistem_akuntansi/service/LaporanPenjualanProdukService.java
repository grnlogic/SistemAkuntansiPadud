package com.padudjayaputera.sistem_akuntansi.service;

import java.time.LocalDate;
import java.util.List;

import com.padudjayaputera.sistem_akuntansi.dto.LaporanPenjualanProdukRequest;
import com.padudjayaputera.sistem_akuntansi.model.LaporanPenjualanProduk;

public interface LaporanPenjualanProdukService {
    LaporanPenjualanProduk createOrUpdateReport(LaporanPenjualanProdukRequest request);
    List<LaporanPenjualanProduk> getAllReports();
    void deleteReport(Integer id);
    
    // Method tambahan untuk query berdasarkan filter
    List<LaporanPenjualanProduk> getReportsBySalesperson(Integer salespersonId);
    List<LaporanPenjualanProduk> getReportsByDateRange(LocalDate startDate, LocalDate endDate);
    List<LaporanPenjualanProduk> getReportsBySalespersonAndDateRange(Integer salespersonId, LocalDate startDate, LocalDate endDate);
}