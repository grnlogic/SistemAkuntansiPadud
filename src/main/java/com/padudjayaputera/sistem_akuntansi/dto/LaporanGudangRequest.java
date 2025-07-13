package com.padudjayaputera.sistem_akuntansi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LaporanGudangRequest {

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate tanggalLaporan;

    @NotNull
    private final Integer accountId; // ID dari bahan baku di tabel COA

    private final BigDecimal barangMasuk;

    private final BigDecimal pemakaian;
    private final BigDecimal stokAkhir;
    private final String keterangan;

    @JsonCreator
    public LaporanGudangRequest(
            @JsonProperty("tanggalLaporan") LocalDate tanggalLaporan,
            @JsonProperty("accountId") Integer accountId,
            @JsonProperty("barangMasuk") BigDecimal barangMasuk,
            @JsonProperty("pemakaian") BigDecimal pemakaian,
            @JsonProperty("stokAkhir") BigDecimal stokAkhir,
            @JsonProperty("keterangan") String keterangan) {
        this.tanggalLaporan = tanggalLaporan;
        this.accountId = accountId;
        this.barangMasuk = barangMasuk;
        this.pemakaian = pemakaian;
        this.stokAkhir = stokAkhir;
        this.keterangan = keterangan;
    }
}