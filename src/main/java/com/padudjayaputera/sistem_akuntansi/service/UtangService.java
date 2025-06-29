package com.padudjayaputera.sistem_akuntansi.service;

import com.padudjayaputera.sistem_akuntansi.dto.UtangRequest;
import com.padudjayaputera.sistem_akuntansi.model.UtangTransaksi;
import java.util.List;

public interface UtangService {
    // Create
    UtangTransaksi createUtang(UtangRequest request);

    // Read
    List<UtangTransaksi> getAllUtang();

    // Update
    UtangTransaksi updateUtang(Integer id, UtangRequest request);

    // Delete
    void deleteUtang(Integer id);
}