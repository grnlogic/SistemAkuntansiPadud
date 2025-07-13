package com.padudjayaputera.sistem_akuntansi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.padudjayaputera.sistem_akuntansi.model.LaporanPenjualanProduk;

import lombok.Getter;

@Getter
public class LaporanPenjualanProdukResponse {
    
    private final Integer id;
    private final LocalDate tanggalLaporan;
    private final String namaSalesperson;
    private final Integer salespersonId;
    private final String namaAccount;
    private final Integer productAccountId;
    private final BigDecimal targetKuantitas;
    private final BigDecimal realisasiKuantitas;
    private final String keteranganKendala;
    private final String createdByUsername;
    private final LocalDateTime createdAt;

    public LaporanPenjualanProdukResponse(LaporanPenjualanProduk laporan) {
        this.id = laporan.getId();
        this.tanggalLaporan = laporan.getTanggalLaporan();
        this.namaSalesperson = laporan.getSalesperson().getNama();
        this.salespersonId = laporan.getSalesperson().getId();
        this.namaAccount = laporan.getProductAccount().getAccountName();
        this.productAccountId = laporan.getProductAccount().getId();
        this.targetKuantitas = laporan.getTargetKuantitas();
        this.realisasiKuantitas = laporan.getRealisasiKuantitas();
        this.keteranganKendala = laporan.getKeteranganKendala();
        this.createdByUsername = laporan.getCreatedBy().getUsername();
        this.createdAt = laporan.getCreatedAt();
    }
}
