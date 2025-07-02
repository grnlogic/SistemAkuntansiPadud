package com.padudjayaputera.sistem_akuntansi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LaporanProduksiRequest {

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate tanggalLaporan;

    @NotNull
    private final Integer accountId; // ID dari produk di tabel COA

    private final BigDecimal hasilProduksi;
    private final BigDecimal barangGagal;
    private final BigDecimal stockBarangJadi;
    private final BigDecimal hpBarangJadi;
    private final String keteranganKendala;

    @JsonCreator
    public LaporanProduksiRequest(
            @JsonProperty("tanggalLaporan") LocalDate tanggalLaporan,
            @JsonProperty("accountId") Integer accountId,
            @JsonProperty("hasilProduksi") BigDecimal hasilProduksi,
            @JsonProperty("barangGagal") BigDecimal barangGagal,
            @JsonProperty("stockBarangJadi") BigDecimal stockBarangJadi,
            @JsonProperty("hpBarangJadi") BigDecimal hpBarangJadi,
            @JsonProperty("keteranganKendala") String keteranganKendala) {
        this.tanggalLaporan = tanggalLaporan;
        this.accountId = accountId;
        this.hasilProduksi = hasilProduksi;
        this.barangGagal = barangGagal;
        this.stockBarangJadi = stockBarangJadi;
        this.hpBarangJadi = hpBarangJadi;
        this.keteranganKendala = keteranganKendala;
    }
}