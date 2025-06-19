package com.padudjayaputera.sistem_akuntansi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.padudjayaputera.sistem_akuntansi.model.TransactionType;

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
    
    // ✅ TAMBAHAN BARU: Transaction Type untuk divisi keuangan
    @JsonProperty("transactionType")
    private TransactionType transactionType;
    
    // ✅ TAMBAHAN BARU: Field untuk divisi khusus
    @JsonProperty("targetAmount")
    private BigDecimal targetAmount;
    
    @JsonProperty("realisasiAmount")
    private BigDecimal realisasiAmount;
    
    // ✅ Field untuk Produksi
    @JsonProperty("hppAmount")
    private BigDecimal hppAmount;
    
    // ✅ Field untuk Gudang
    @JsonProperty("pemakaianAmount")
    private BigDecimal pemakaianAmount;
    
    @JsonProperty("stokAkhir")
    private BigDecimal stokAkhir;
    
    // ✅ Helper methods untuk validasi divisi-specific
    public boolean isKeuanganData() {
        return transactionType != null;
    }
    
    public boolean isPemasaranData() {
        return targetAmount != null || realisasiAmount != null;
    }
    
    public boolean isProduksiData() {
        return hppAmount != null;
    }
    
    public boolean isGudangData() {
        return pemakaianAmount != null || stokAkhir != null;
    }
    
    // ✅ Add toString for debugging
    @Override
    public String toString() {
        return "EntriHarianRequest{" +
                "accountId=" + accountId +
                ", tanggal=" + tanggal +
                ", nilai=" + nilai +
                ", description='" + description + '\'' +
                ", transactionType=" + transactionType +
                ", targetAmount=" + targetAmount +
                ", realisasiAmount=" + realisasiAmount +
                ", hppAmount=" + hppAmount +
                ", pemakaianAmount=" + pemakaianAmount +
                ", stokAkhir=" + stokAkhir +
                '}';
    }
}