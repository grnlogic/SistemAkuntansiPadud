package com.padudjayaputera.sistem_akuntansi.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "pemasaran_performance")
@Data
public class PemasaranPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entri_harian_id", nullable = false)
    private EntriHarian entriHarian;

    @Column(name = "target_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "realisasi_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal realisasiAmount;

    @Column(name = "tanggal_laporan", nullable = false)
    private LocalDate tanggalLaporan;

    @Column(name = "sales_person", length = 100)
    private String salesPerson;

    @Column(name = "produk_kategori", length = 100)
    private String produkKategori;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ✅ Helper method untuk menghitung performance percentage
    public Double getPerformancePercentage() {
        if (targetAmount != null && realisasiAmount != null && targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            return realisasiAmount.divide(targetAmount, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        return 0.0;
    }

    // ✅ Helper method untuk kategori performance
    public String getPerformanceCategory() {
        Double percentage = getPerformancePercentage();
        if (percentage >= 100) return "TARGET_TERCAPAI";
        if (percentage >= 80) return "MENDEKATI_TARGET";
        if (percentage >= 50) return "SETENGAH_TARGET";
        return "JAUH_DARI_TARGET";
    }
}
