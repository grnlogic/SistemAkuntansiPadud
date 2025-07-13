package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;

import com.padudjayaputera.sistem_akuntansi.dto.PerusahaanRequest;
import com.padudjayaputera.sistem_akuntansi.model.Perusahaan;

public interface PerusahaanService {
    Perusahaan createPerusahaan(PerusahaanRequest request);
    List<Perusahaan> getAllPerusahaan();
    Perusahaan getPerusahaanById(Integer id);
    Perusahaan updatePerusahaan(Integer id, PerusahaanRequest request);
    void deletePerusahaan(Integer id);
}
