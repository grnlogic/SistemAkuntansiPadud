package com.padudjayaputera.sistem_akuntansi.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "utang_transaksi")
@Getter
@Setter
public class UtangTransaksi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tanggal_transaksi", nullable = false)
    private LocalDate tanggalTransaksi;

    @ManyToOne(fetch = FetchType.EAGER) // EAGER untuk menghindari error lazy loading
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipe_transaksi", nullable = false)
    private TipeUtang tipeTransaksi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KategoriUtang kategori;

    @Column(nullable = false)
    private BigDecimal nominal;

    private String keterangan;

    @ManyToOne(fetch = FetchType.EAGER) // EAGER untuk menghindari error lazy loading
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}