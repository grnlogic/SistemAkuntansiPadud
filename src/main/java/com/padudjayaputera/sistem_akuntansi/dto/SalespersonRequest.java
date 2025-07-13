package com.padudjayaputera.sistem_akuntansi.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SalespersonRequest {

    @NotBlank(message = "Nama salesperson tidak boleh kosong")
    @Size(max = 255, message = "Nama salesperson maksimal 255 karakter")
    private final String nama;

    @NotNull(message = "Division ID tidak boleh kosong")
    private final Integer divisionId;

    private final String status;

    @JsonCreator
    public SalespersonRequest(
            @JsonProperty("nama") String nama,
            @JsonProperty("divisionId") Integer divisionId,
            @JsonProperty("status") String status) {
        this.nama = nama;
        this.divisionId = divisionId;
        this.status = status != null ? status : "AKTIF";
    }
}
