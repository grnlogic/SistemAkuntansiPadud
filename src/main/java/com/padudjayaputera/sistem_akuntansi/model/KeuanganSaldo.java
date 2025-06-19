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
@Table(name = "keuangan_saldo")
@Data
public class KeuanganSaldo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entri_harian_id", nullable = false)
    private EntriHarian entriHarian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "saldo_awal", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoAwal = BigDecimal.ZERO;

    @Column(name = "penerimaan", precision = 15, scale = 2)
    private BigDecimal penerimaan = BigDecimal.ZERO;

    @Column(name = "pengeluaran", precision = 15, scale = 2)
    private BigDecimal pengeluaran = BigDecimal.ZERO;

    @Column(name = "tanggal_transaksi", nullable = false)
    private LocalDate tanggalTransaksi;

    @Column(name = "keterangan", columnDefinition = "TEXT")
    private String keterangan;

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

    // ✅ Helper method untuk menghitung saldo akhir
    public BigDecimal getSaldoAkhir() {
        BigDecimal penerimaan = this.penerimaan != null ? this.penerimaan : BigDecimal.ZERO;
        BigDecimal pengeluaran = this.pengeluaran != null ? this.pengeluaran : BigDecimal.ZERO;
        return saldoAwal.add(penerimaan).subtract(pengeluaran);
    }

    // ✅ Helper method untuk status cash
    public CashStatus getCashStatus() {
        BigDecimal saldoAkhir = getSaldoAkhir();
        if (saldoAkhir.compareTo(BigDecimal.ZERO) < 0) return CashStatus.DEFICIT;
        if (saldoAkhir.compareTo(BigDecimal.valueOf(1000000)) < 0) return CashStatus.LOW_CASH;
        if (saldoAkhir.compareTo(BigDecimal.valueOf(10000000)) < 0) return CashStatus.NORMAL_CASH;
        return CashStatus.HIGH_CASH;
    }

    // ✅ Enum untuk status cash
    public enum CashStatus {
        DEFICIT, LOW_CASH, NORMAL_CASH, HIGH_CASH
    }
}
