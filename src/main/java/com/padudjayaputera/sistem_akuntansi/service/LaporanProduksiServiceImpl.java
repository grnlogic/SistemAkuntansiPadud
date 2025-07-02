package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.padudjayaputera.sistem_akuntansi.dto.LaporanProduksiRequest;
import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.model.LaporanProduksiHarian;
import com.padudjayaputera.sistem_akuntansi.model.User;
import com.padudjayaputera.sistem_akuntansi.repository.AccountRepository;
import com.padudjayaputera.sistem_akuntansi.repository.LaporanProduksiHarianRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LaporanProduksiServiceImpl implements LaporanProduksiService {

    private final LaporanProduksiHarianRepository laporanRepository;
    private final AccountRepository accountRepository;

    @Override
    public LaporanProduksiHarian createOrUpdateReport(LaporanProduksiRequest request) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Akun Produk dengan ID " + request.getAccountId() + " tidak ditemukan."));

        Optional<LaporanProduksiHarian> existingReportOpt = laporanRepository.findByTanggalLaporanAndAccount(request.getTanggalLaporan(), account);

        LaporanProduksiHarian report;
        if (existingReportOpt.isPresent()) {
            report = existingReportOpt.get();
        } else {
            report = new LaporanProduksiHarian();
            report.setTanggalLaporan(request.getTanggalLaporan());
            report.setAccount(account);
        }

        report.setHasilProduksi(request.getHasilProduksi());
        report.setBarangGagal(request.getBarangGagal());
        report.setStockBarangJadi(request.getStockBarangJadi());
        report.setHpBarangJadi(request.getHpBarangJadi());
        report.setKeteranganKendala(request.getKeteranganKendala());
        report.setCreatedBy(loggedInUser);

        return laporanRepository.save(report);
    }

    @Override
    public List<LaporanProduksiHarian> getAllReports() {
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