package com.padudjayaputera.sistem_akuntansi.service;

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

        Optional<LaporanGudangHarian> existingReportOpt = laporanRepository.findByTanggalLaporanAndAccount(request.getTanggalLaporan(), account);

        LaporanGudangHarian report;
        if (existingReportOpt.isPresent()) {
            report = existingReportOpt.get();
        } else {
            report = new LaporanGudangHarian();
            report.setTanggalLaporan(request.getTanggalLaporan());
            report.setAccount(account);
        }

        report.setStokAwal(request.getStokAwal());
        report.setPemakaian(request.getPemakaian());
        report.setStokAkhir(request.getStokAkhir());
        report.setKondisiGudang(request.getKondisiGudang());
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