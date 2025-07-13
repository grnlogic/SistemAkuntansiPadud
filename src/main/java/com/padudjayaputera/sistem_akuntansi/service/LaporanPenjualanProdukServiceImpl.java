package com.padudjayaputera.sistem_akuntansi.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.padudjayaputera.sistem_akuntansi.dto.LaporanPenjualanProdukRequest;
import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.model.LaporanPenjualanProduk;
import com.padudjayaputera.sistem_akuntansi.model.Salesperson;
import com.padudjayaputera.sistem_akuntansi.model.User;
import com.padudjayaputera.sistem_akuntansi.repository.AccountRepository;
import com.padudjayaputera.sistem_akuntansi.repository.LaporanPenjualanProdukRepository;
import com.padudjayaputera.sistem_akuntansi.repository.SalespersonRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LaporanPenjualanProdukServiceImpl implements LaporanPenjualanProdukService {

    private final LaporanPenjualanProdukRepository laporanRepository;
    private final SalespersonRepository salespersonRepository;
    private final AccountRepository accountRepository;

    @Override
    public LaporanPenjualanProduk createOrUpdateReport(LaporanPenjualanProdukRequest request) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Salesperson salesperson = salespersonRepository.findById(request.getSalespersonId())
            .orElseThrow(() -> new RuntimeException("Salesperson dengan ID " + request.getSalespersonId() + " tidak ditemukan."));
        Account productAccount = accountRepository.findById(request.getProductAccountId())
            .orElseThrow(() -> new RuntimeException("Akun Produk dengan ID " + request.getProductAccountId() + " tidak ditemukan."));

        Optional<LaporanPenjualanProduk> existingReportOpt = laporanRepository.findByTanggalLaporanAndSalespersonAndProductAccount(
            request.getTanggalLaporan(), salesperson, productAccount
        );

        LaporanPenjualanProduk report;
        if (existingReportOpt.isPresent()) {
            report = existingReportOpt.get();
        } else {
            report = new LaporanPenjualanProduk();
            report.setTanggalLaporan(request.getTanggalLaporan());
            report.setSalesperson(salesperson);
            report.setProductAccount(productAccount);
        }

        report.setTargetKuantitas(request.getTargetKuantitas());
        report.setRealisasiKuantitas(request.getRealisasiKuantitas());
        report.setKeteranganKendala(request.getKeteranganKendala());
        report.setCreatedBy(loggedInUser);

        return laporanRepository.save(report);
    }

    @Override
    public List<LaporanPenjualanProduk> getAllReports() {
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
    
    @Override
    public List<LaporanPenjualanProduk> getReportsBySalesperson(Integer salespersonId) {
        return laporanRepository.findBySalespersonId(salespersonId);
    }
    
    @Override
    public List<LaporanPenjualanProduk> getReportsByDateRange(LocalDate startDate, LocalDate endDate) {
        return laporanRepository.findByTanggalLaporanBetween(startDate, endDate);
    }
    
    @Override
    public List<LaporanPenjualanProduk> getReportsBySalespersonAndDateRange(Integer salespersonId, LocalDate startDate, LocalDate endDate) {
        return laporanRepository.findBySalespersonIdAndTanggalLaporanBetween(salespersonId, startDate, endDate);
    }
}