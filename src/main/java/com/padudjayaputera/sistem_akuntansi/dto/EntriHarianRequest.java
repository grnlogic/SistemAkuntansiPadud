package com.padudjayaputera.sistem_akuntansi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class EntriHarianRequest {
    
    @JsonProperty("accountId") // ✅ Explicit JSON mapping
    @NotNull(message = "Account ID tidak boleh null")
    private Integer accountId;
    
    @JsonProperty("tanggal") // ✅ Explicit JSON mapping
    @NotNull(message = "Tanggal tidak boleh null")
    private LocalDate tanggal;
    
    @JsonProperty("nilai") // ✅ Explicit JSON mapping
    @NotNull(message = "Nilai tidak boleh null")
    @Positive(message = "Nilai harus positif")
    private BigDecimal nilai;
    
    @JsonProperty("description") // ✅ Explicit JSON mapping
    private String description;
    
    // ✅ Add toString for debugging
    @Override
    public String toString() {
        return "EntriHarianRequest{" +
                "accountId=" + accountId +
                ", tanggal=" + tanggal +
                ", nilai=" + nilai +
                ", description='" + description + '\'' +
                '}';
    }
}