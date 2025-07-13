package com.padudjayaputera.sistem_akuntansi.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PerusahaanRequest {

    @NotBlank(message = "Nama perusahaan tidak boleh kosong")
    @Size(max = 255, message = "Nama perusahaan maksimal 255 karakter")
    private final String nama;

    @JsonCreator
    public PerusahaanRequest(@JsonProperty("nama") String nama) {
        this.nama = nama;
    }
}
