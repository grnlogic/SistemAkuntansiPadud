package com.padudjayaputera.sistem_akuntansi.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "entri_harian")
@Data
public class EntriHarian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Integer id;

    @Column(name = "tanggal_laporan", nullable = false)
    @JsonProperty("tanggalLaporan")
    private LocalDate tanggalLaporan;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @JsonProperty("account")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonProperty("user")
    private User user;

    @Column(name = "nilai", nullable = false, precision = 15, scale = 2)
    @JsonProperty("nilai")
    private BigDecimal nilai;

    @Column(name = "description", length = 500)
    @JsonProperty("description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    // ✅ FIXED: Add proper JSON annotations for specialized fields
    @Column(name = "target_amount", precision = 15, scale = 2)
    @JsonProperty("targetAmount")
    private BigDecimal targetAmount;

    @Column(name = "realisasi_amount", precision = 15, scale = 2)
    @JsonProperty("realisasiAmount")
    private BigDecimal realisasiAmount;

    @Column(name = "hpp_amount", precision = 15, scale = 2)
    @JsonProperty("hppAmount")
    private BigDecimal hppAmount;

    @Column(name = "pemakaian_amount", precision = 15, scale = 2)
    @JsonProperty("pemakaianAmount")
    private BigDecimal pemakaianAmount;

    @Column(name = "stok_akhir", precision = 15, scale = 2)
    @JsonProperty("stokAkhir")
    private BigDecimal stokAkhir;

    @Column(name = "saldo_akhir", precision = 15, scale = 2)
    @JsonProperty("saldoAkhir")
    private BigDecimal saldoAkhir;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ✅ Helper methods untuk menentukan jenis divisi data
    public boolean isKeuanganData() {
        return transactionType != null || saldoAkhir != null;
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
    
    // ✅ Helper method untuk performance calculation (Pemasaran)
    public Double getPerformancePercentage() {
        if (targetAmount != null && realisasiAmount != null && targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            return realisasiAmount.divide(targetAmount, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        return null;
    }
    
    // ✅ Helper method untuk HPP per unit (Produksi)
    public BigDecimal getHppPerUnit() {
        if (hppAmount != null && nilai != null && nilai.compareTo(BigDecimal.ZERO) > 0) {
            return hppAmount.divide(nilai, 2, java.math.RoundingMode.HALF_UP);
        }
        return null;
    }
}