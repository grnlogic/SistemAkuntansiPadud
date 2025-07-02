package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;

import com.padudjayaputera.sistem_akuntansi.dto.LaporanGudangRequest;
import com.padudjayaputera.sistem_akuntansi.model.LaporanGudangHarian;

public interface LaporanGudangService {
    LaporanGudangHarian createOrUpdateReport(LaporanGudangRequest request);
    List<LaporanGudangHarian> getAllReports();
    void deleteReport(Integer id);
}