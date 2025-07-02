package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;

import com.padudjayaputera.sistem_akuntansi.dto.LaporanProduksiRequest;
import com.padudjayaputera.sistem_akuntansi.model.LaporanProduksiHarian;

public interface LaporanProduksiService {
    LaporanProduksiHarian createOrUpdateReport(LaporanProduksiRequest request);
    List<LaporanProduksiHarian> getAllReports();
    void deleteReport(Integer id);
}