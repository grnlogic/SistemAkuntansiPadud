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
@Table(name = "gudang_stok")
@Data
public class GudangStok {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entri_harian_id", nullable = false)
    private EntriHarian entriHarian;

    @Column(name = "bahan_baku_name", nullable = false, length = 200)
    private String bahanBakuName;

    @Column(name = "stok_awal", nullable = false, precision = 15, scale = 2)
    private BigDecimal stokAwal = BigDecimal.ZERO;

    @Column(name = "pemakaian_hari_ini", nullable = false, precision = 15, scale = 2)
    private BigDecimal pemakaianHariIni = BigDecimal.ZERO;

    @Column(name = "stok_minimum", precision = 15, scale = 2)
    private BigDecimal stokMinimum = BigDecimal.ZERO;

    @Column(name = "satuan", length = 50)
    private String satuan = "KG";

    @Column(name = "tanggal_update", nullable = false)
    private LocalDate tanggalUpdate;

    @Column(name = "lokasi_gudang", length = 100)
    private String lokasiGudang;

    @Column(name = "pic_gudang", length = 100)
    private String picGudang;

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

    // ✅ Helper method untuk menghitung stok akhir
    public BigDecimal getStokAkhir() {
        return stokAwal.subtract(pemakaianHariIni);
    }

    // ✅ Helper method untuk status stok
    public StatusStok getStatusStok() {
        BigDecimal stokAkhir = getStokAkhir();
        if (stokAkhir.compareTo(BigDecimal.ZERO) <= 0) return StatusStok.HABIS;
        if (stokAkhir.compareTo(stokMinimum) <= 0) return StatusStok.RENDAH;
        return StatusStok.AMAN;
    }

    // ✅ Helper method untuk persentase stok
    public Double getStokPercentage() {
        if (stokMinimum != null && stokMinimum.compareTo(BigDecimal.ZERO) > 0) {
            return getStokAkhir().divide(stokMinimum, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        return null;
    }

    // ✅ Enum untuk status stok
    public enum StatusStok {
        AMAN, RENDAH, HABIS
    }
}
