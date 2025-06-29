package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;

import com.padudjayaputera.sistem_akuntansi.dto.LaporanPenjualanSalesRequest;
import com.padudjayaputera.sistem_akuntansi.model.LaporanPenjualanSales;

public interface LaporanPenjualanSalesService {
    LaporanPenjualanSales createOrUpdateReport(LaporanPenjualanSalesRequest request);
    List<LaporanPenjualanSales> getAllReports();
    void deleteReport(Integer id);
}