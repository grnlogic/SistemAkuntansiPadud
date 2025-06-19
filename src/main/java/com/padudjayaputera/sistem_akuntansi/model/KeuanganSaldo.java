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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "keuangan_saldo",
       indexes = {
           @Index(name = "idx_keuangan_saldo_account_tanggal", columnList = "account_id, tanggal_transaksi"),
           @Index(name = "idx_keuangan_saldo_latest", columnList = "account_id, tanggal_transaksi DESC, created_at DESC")
       }
       // ✅ REMOVED: uniqueConstraints to allow multiple records per account per day
)
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

    // ✅ Calculate saldo akhir automatically
    public BigDecimal getSaldoAkhir() {
        if (saldoAwal == null) saldoAwal = BigDecimal.ZERO;
        if (penerimaan == null) penerimaan = BigDecimal.ZERO;
        if (pengeluaran == null) pengeluaran = BigDecimal.ZERO;
        
        return saldoAwal.add(penerimaan).subtract(pengeluaran);
    }
    
    // ✅ Helper method untuk display
    public String getTransactionSummary() {
        return String.format("Saldo Awal: %s, Penerimaan: %s, Pengeluaran: %s, Saldo Akhir: %s", 
                saldoAwal, penerimaan, pengeluaran, getSaldoAkhir());
    }
}
