package com.padudjayaputera.sistem_akuntansi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.padudjayaputera.sistem_akuntansi.model.TipeUtang;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class UtangRequest {
    @NotNull(message = "Akun tidak boleh kosong")
    private final Integer accountId;
    @NotNull(message = "Tanggal transaksi tidak boleh kosong")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate tanggalTransaksi;
    @NotNull(message = "Tipe transaksi tidak boleh kosong")
    private final TipeUtang tipeTransaksi;
    @NotNull(message = "Nominal tidak boleh kosong")
    @Positive(message = "Nominal harus lebih dari nol")
    private final BigDecimal nominal;
    private final String keterangan;

    @JsonCreator
    public UtangRequest(
            @JsonProperty("accountId") Integer accountId,
            @JsonProperty("tanggalTransaksi") LocalDate tanggalTransaksi,
            @JsonProperty("tipeTransaksi") TipeUtang tipeTransaksi,
            @JsonProperty("nominal") BigDecimal nominal,
            @JsonProperty("keterangan") String keterangan) {
        this.accountId = accountId;
        this.tanggalTransaksi = tanggalTransaksi;
        this.tipeTransaksi = tipeTransaksi;
        this.nominal = nominal;
        this.keterangan = keterangan;
    }
}