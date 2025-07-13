package com.padudjayaputera.sistem_akuntansi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LaporanPenjualanProdukRequest {

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate tanggalLaporan;

    @NotNull
    private final Integer salespersonId;

    @NotNull
    private final Integer productAccountId;

    private final BigDecimal targetKuantitas;

    private final BigDecimal realisasiKuantitas;

    private final String keteranganKendala;

    @JsonCreator
    public LaporanPenjualanProdukRequest(
            @JsonProperty("tanggalLaporan") LocalDate tanggalLaporan,
            @JsonProperty("salespersonId") Integer salespersonId,
            @JsonProperty("productAccountId") Integer productAccountId,
            @JsonProperty("targetKuantitas") BigDecimal targetKuantitas,
            @JsonProperty("realisasiKuantitas") BigDecimal realisasiKuantitas,
            @JsonProperty("keteranganKendala") String keteranganKendala) {
        this.tanggalLaporan = tanggalLaporan;
        this.salespersonId = salespersonId;
        this.productAccountId = productAccountId;
        this.targetKuantitas = targetKuantitas;
        this.realisasiKuantitas = realisasiKuantitas;
        this.keteranganKendala = keteranganKendala;
    }
}