package com.padudjayaputera.sistem_akuntansi.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.padudjayaputera.sistem_akuntansi.dto.LaporanGudangRequest;
import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.model.LaporanGudangHarian;
import com.padudjayaputera.sistem_akuntansi.model.User;
import com.padudjayaputera.sistem_akuntansi.repository.AccountRepository;
import com.padudjayaputera.sistem_akuntansi.repository.LaporanGudangHarianRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LaporanGudangServiceImpl implements LaporanGudangService {

    private final LaporanGudangHarianRepository laporanRepository;
    private final AccountRepository accountRepository;

    @Override
    public LaporanGudangHarian createOrUpdateReport(LaporanGudangRequest request) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Akun Bahan Baku dengan ID " + request.getAccountId() + " tidak ditemukan."));

        // ✅ ULTIMATE FLEXIBLE: Benar-benar respek input user
        BigDecimal barangMasuk = request.getBarangMasuk();
        
        // TIDAK ada auto-calculate sama sekali, simpan apa adanya dari user
        if (barangMasuk == null) {
            System.out.println("INFO: Barang masuk kosong (null) - sesuai input user");
        } else {
            System.out.println("INFO: Menggunakan barang masuk dari input user: " + barangMasuk);
        }

        // ✅ FLEXIBLE VALIDATION: Handle null barang masuk
        BigDecimal finalStokAkhir = request.getStokAkhir();
        
        // Hanya lakukan validasi jika semua data tersedia
        if (request.getPemakaian() != null && barangMasuk != null && finalStokAkhir != null) {
            BigDecimal expectedStokAkhir = barangMasuk.subtract(request.getPemakaian());
            
            // Just give warning for negative stock, don't throw error
            if (expectedStokAkhir.compareTo(BigDecimal.ZERO) < 0) {
                System.out.println("WARNING: Stok akhir akan menjadi negatif. " +
                    "Barang masuk: " + barangMasuk + ", Pemakaian: " + request.getPemakaian() + 
                    ", Stok akhir yang diharapkan: " + expectedStokAkhir);
            }
        } else if (barangMasuk == null) {
            System.out.println("INFO: Validasi stok dilewati karena barang masuk kosong (null)");
        }
        
        // Use user's input as-is
        if (finalStokAkhir == null) {
            System.out.println("INFO: Stok akhir dibiarkan null sesuai input user (tidak dihitung otomatis)");
        }

        Optional<LaporanGudangHarian> existingReportOpt = laporanRepository
            .findByAccountIdAndTanggal(request.getAccountId(), request.getTanggalLaporan());

        LaporanGudangHarian report;
        if (existingReportOpt.isPresent()) {
            report = existingReportOpt.get();
        } else {
            report = new LaporanGudangHarian();
            report.setTanggalLaporan(request.getTanggalLaporan());
            report.setAccount(account);
        }

        report.setBarangMasuk(barangMasuk);
        report.setPemakaian(request.getPemakaian());
        report.setStokAkhir(finalStokAkhir);
        report.setKeterangan(request.getKeterangan());
        report.setCreatedBy(loggedInUser);

        return laporanRepository.save(report);
    }

    @Override
    public List<LaporanGudangHarian> getAllReports() {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (loggedInUser.getRole().name().equals("SUPER_ADMIN")) {
            return laporanRepository.findAll();
        } else {
            return laporanRepository.findByUserId(loggedInUser.getId());
        }
    }

    @Override
    public void deleteReport(Integer id) {
        laporanRepository.deleteById(id);
    }
}