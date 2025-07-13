package com.padudjayaputera.sistem_akuntansi.dto;

import com.padudjayaputera.sistem_akuntansi.model.Perusahaan;

import lombok.Getter;

@Getter
public class PerusahaanResponse {
    
    private final Integer id;
    private final String nama;

    public PerusahaanResponse(Perusahaan perusahaan) {
        this.id = perusahaan.getId();
        this.nama = perusahaan.getNama();
    }
}
