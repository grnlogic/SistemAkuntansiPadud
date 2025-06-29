package com.padudjayaputera.sistem_akuntansi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LaporanPenjualanSalesRequest {

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate tanggalLaporan;

    @NotNull
    private Integer salespersonId;

    private final BigDecimal targetPenjualan;
    private final BigDecimal realisasiPenjualan;
    private final BigDecimal returPenjualan;
    private final String keteranganKendala;

    // Constructor ini memberitahu Jackson cara membuat objek dari JSON secara eksplisit
    @JsonCreator
    public LaporanPenjualanSalesRequest(
            @JsonProperty("tanggalLaporan") LocalDate tanggalLaporan,
            @JsonProperty("salespersonId") Integer salespersonId,
            @JsonProperty("targetPenjualan") BigDecimal targetPenjualan,
            @JsonProperty("realisasiPenjualan") BigDecimal realisasiPenjualan,
            @JsonProperty("returPenjualan") BigDecimal returPenjualan,
            @JsonProperty("keteranganKendala") String keteranganKendala) {
        
        this.tanggalLaporan = tanggalLaporan;
        this.salespersonId = salespersonId;
        this.targetPenjualan = targetPenjualan;
        this.realisasiPenjualan = realisasiPenjualan;
        this.returPenjualan = returPenjualan;
        this.keteranganKendala = keteranganKendala;
    }
}