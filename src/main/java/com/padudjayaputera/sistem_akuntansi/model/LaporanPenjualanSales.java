package com.padudjayaputera.sistem_akuntansi.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "laporan_penjualan_sales")
@Getter
@Setter
public class LaporanPenjualanSales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tanggal_laporan", nullable = false)
    private LocalDate tanggalLaporan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salesperson_id", nullable = false)
    private Salesperson salesperson;

    @Column(name = "target_penjualan")
    private BigDecimal targetPenjualan;

    @Column(name = "realisasi_penjualan")
    private BigDecimal realisasiPenjualan;

    @Column(name = "retur_penjualan")
    private BigDecimal returPenjualan;

    @Column(name = "keterangan_kendala")
    private String keteranganKendala;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}