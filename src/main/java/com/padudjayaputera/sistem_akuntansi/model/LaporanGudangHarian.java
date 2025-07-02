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
@Table(name = "laporan_gudang_harian")
@Getter
@Setter
public class LaporanGudangHarian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tanggal_laporan", nullable = false)
    private LocalDate tanggalLaporan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account; // Tautan ke Akun COA bahan baku

    @Column(name = "stok_awal")
    private BigDecimal stokAwal;

    @Column(name = "pemakaian")
    private BigDecimal pemakaian;

    @Column(name = "stok_akhir")
    private BigDecimal stokAkhir;

    @Column(name = "kondisi_gudang")
    private String kondisiGudang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}