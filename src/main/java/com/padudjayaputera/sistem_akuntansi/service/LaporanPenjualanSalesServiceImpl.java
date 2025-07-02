package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.padudjayaputera.sistem_akuntansi.dto.LaporanPenjualanSalesRequest;
import com.padudjayaputera.sistem_akuntansi.model.LaporanPenjualanSales;
import com.padudjayaputera.sistem_akuntansi.model.Salesperson;
import com.padudjayaputera.sistem_akuntansi.model.User;
import com.padudjayaputera.sistem_akuntansi.repository.LaporanPenjualanSalesRepository;
import com.padudjayaputera.sistem_akuntansi.repository.SalespersonRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LaporanPenjualanSalesServiceImpl implements LaporanPenjualanSalesService {

    private final LaporanPenjualanSalesRepository laporanRepository;
    private final SalespersonRepository salespersonRepository;

    @Override
    public LaporanPenjualanSales createOrUpdateReport(LaporanPenjualanSalesRequest request) {
        // 1. Ambil data admin yang sedang login untuk dicatat sebagai 'createdBy'
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2. Cari data master salesperson berdasarkan ID dari request frontend
        Salesperson salesperson = salespersonRepository.findById(request.getSalespersonId())
                .orElseThrow(() -> new RuntimeException("Salesperson dengan ID " + request.getSalespersonId() + " tidak ditemukan."));

        // 3. Logika "UPSERT": Cek apakah sudah ada laporan untuk salesperson dan tanggal yang sama
        Optional<LaporanPenjualanSales> existingReportOpt = laporanRepository.findByTanggalLaporanAndSalesperson(request.getTanggalLaporan(), salesperson);

        LaporanPenjualanSales report;
        if (existingReportOpt.isPresent()) {
            // Jika sudah ada, kita akan PERBARUI (UPDATE) data yang ada
            report = existingReportOpt.get();
        } else {
            // Jika belum ada, kita BUAT (CREATE) laporan baru
            report = new LaporanPenjualanSales();
            report.setTanggalLaporan(request.getTanggalLaporan());
            report.setSalesperson(salesperson);
        }

        // 4. Set atau perbarui nilai-nilainya dari request
        report.setTargetPenjualan(request.getTargetPenjualan());
        report.setRealisasiPenjualan(request.getRealisasiPenjualan());
        report.setReturPenjualan(request.getReturPenjualan());
        report.setKeteranganKendala(request.getKeteranganKendala());
        report.setCreatedBy(loggedInUser); // Selalu catat siapa yang terakhir mengubah/membuat

        // 5. Simpan ke database (bisa INSERT atau UPDATE tergantung kondisi di atas)
        return laporanRepository.save(report);
    }

    @Override
    public List<LaporanPenjualanSales> getAllReports() {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (loggedInUser.getRole().name().equals("SUPER_ADMIN")) {
            return laporanRepository.findAll();
        } else {
            return laporanRepository.findByUserId(loggedInUser.getId());
        }
    }

    @Override
    public void deleteReport(Integer id) {
        if (!laporanRepository.existsById(id)) {
            throw new RuntimeException("Laporan Penjualan dengan ID " + id + " tidak ditemukan.");
        }
        laporanRepository.deleteById(id);
    }
}