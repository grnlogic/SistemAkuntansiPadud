package com.padudjayaputera.sistem_akuntansi.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "produksi_hpp")
@Data
public class ProduksiHpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entri_harian_id", nullable = false)
    private EntriHarian entriHarian;

    @Column(name = "produk_name", nullable = false, length = 200)
    private String produkName;

    @Column(name = "jumlah_produksi", nullable = false, precision = 15, scale = 2)
    private BigDecimal jumlahProduksi;

    @Column(name = "hpp_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal hppTotal;

    @Column(name = "tanggal_produksi", nullable = false)
    private LocalDate tanggalProduksi;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_kerja", length = 10)
    private ShiftKerja shiftKerja = ShiftKerja.PAGI;

    @Column(name = "operator", length = 100)
    private String operator;

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

    // ✅ Helper method untuk menghitung HPP per unit
    public BigDecimal getHppPerUnit() {
        if (hppTotal != null && jumlahProduksi != null && jumlahProduksi.compareTo(BigDecimal.ZERO) > 0) {
            return hppTotal.divide(jumlahProduksi, 2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    // ✅ Helper method untuk kategori efficiency
    public String getEfficiencyCategory() {
        BigDecimal hppPerUnit = getHppPerUnit();
        if (hppPerUnit.compareTo(BigDecimal.valueOf(1000)) <= 0) return "EFISIEN";
        if (hppPerUnit.compareTo(BigDecimal.valueOf(2000)) <= 0) return "NORMAL";
        return "KURANG_EFISIEN";
    }

    // ✅ Enum untuk shift kerja
    public enum ShiftKerja {
        PAGI, SIANG, MALAM
    }
}
