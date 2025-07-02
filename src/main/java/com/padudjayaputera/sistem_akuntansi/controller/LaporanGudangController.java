package com.padudjayaputera.sistem_akuntansi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.padudjayaputera.sistem_akuntansi.dto.LaporanGudangRequest;
import com.padudjayaputera.sistem_akuntansi.model.LaporanGudangHarian;
import com.padudjayaputera.sistem_akuntansi.service.LaporanGudangService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/laporan-gudang")
@RequiredArgsConstructor
public class LaporanGudangController {

    private final LaporanGudangService laporanService;

    private static final String HAS_ACCESS_TO_GUDANG =
        "hasAuthority('SUPER_ADMIN') or (hasAuthority('ADMIN_DIVISI') and authentication.principal.division.name == 'DIVISI DISTRIBUSI & GUDANG')";

    @PostMapping
    @PreAuthorize(HAS_ACCESS_TO_GUDANG)
    public ResponseEntity<LaporanGudangHarian> createOrUpdateReport(@Valid @RequestBody LaporanGudangRequest request) {
        return new ResponseEntity<>(laporanService.createOrUpdateReport(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize(HAS_ACCESS_TO_GUDANG)
    public ResponseEntity<List<LaporanGudangHarian>> getAllReports() {
        return ResponseEntity.ok(laporanService.getAllReports());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(HAS_ACCESS_TO_GUDANG)
    public ResponseEntity<Void> deleteReport(@PathVariable Integer id) {
        laporanService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}