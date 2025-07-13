package com.padudjayaputera.sistem_akuntansi.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.padudjayaputera.sistem_akuntansi.dto.LaporanPenjualanProdukRequest;
import com.padudjayaputera.sistem_akuntansi.dto.LaporanPenjualanProdukResponse;
import com.padudjayaputera.sistem_akuntansi.model.LaporanPenjualanProduk;
import com.padudjayaputera.sistem_akuntansi.service.LaporanPenjualanProdukService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/laporan-penjualan-produk")
@RequiredArgsConstructor
public class LaporanPenjualanProdukController {

    private final LaporanPenjualanProdukService laporanService;

    private static final String HAS_ACCESS_TO_PEMASARAN =
        "hasAuthority('SUPER_ADMIN') or (hasAuthority('ADMIN_DIVISI') and authentication.principal.division.name == 'DIVISI PEMASARAN & PENJUALAN')";

    @PostMapping
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<LaporanPenjualanProdukResponse> createReport(@Valid @RequestBody LaporanPenjualanProdukRequest request) {
        LaporanPenjualanProduk laporan = laporanService.createOrUpdateReport(request);
        return new ResponseEntity<>(new LaporanPenjualanProdukResponse(laporan), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<List<LaporanPenjualanProdukResponse>> getAllReports() {
        List<LaporanPenjualanProduk> reports = laporanService.getAllReports();
        List<LaporanPenjualanProdukResponse> responses = reports.stream()
            .map(LaporanPenjualanProdukResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/salesperson/{salespersonId}")
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<List<LaporanPenjualanProdukResponse>> getReportsBySalesperson(@PathVariable Integer salespersonId) {
        List<LaporanPenjualanProduk> reports = laporanService.getReportsBySalesperson(salespersonId);
        List<LaporanPenjualanProdukResponse> responses = reports.stream()
            .map(LaporanPenjualanProdukResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/date-range")
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<List<LaporanPenjualanProdukResponse>> getReportsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<LaporanPenjualanProduk> reports = laporanService.getReportsByDateRange(startDate, endDate);
        List<LaporanPenjualanProdukResponse> responses = reports.stream()
            .map(LaporanPenjualanProdukResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/filter")
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<List<LaporanPenjualanProdukResponse>> getReportsWithFilter(
            @RequestParam(required = false) Integer salespersonId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<LaporanPenjualanProduk> reports;
        if (salespersonId != null && startDate != null && endDate != null) {
            reports = laporanService.getReportsBySalespersonAndDateRange(salespersonId, startDate, endDate);
        } else if (salespersonId != null) {
            reports = laporanService.getReportsBySalesperson(salespersonId);
        } else if (startDate != null && endDate != null) {
            reports = laporanService.getReportsByDateRange(startDate, endDate);
        } else {
            reports = laporanService.getAllReports();
        }
        List<LaporanPenjualanProdukResponse> responses = reports.stream()
            .map(LaporanPenjualanProdukResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<Void> deleteReport(@PathVariable Integer id) {
        laporanService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}