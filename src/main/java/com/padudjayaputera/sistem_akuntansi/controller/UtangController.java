package com.padudjayaputera.sistem_akuntansi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.padudjayaputera.sistem_akuntansi.dto.UtangRequest;
import com.padudjayaputera.sistem_akuntansi.model.UtangTransaksi;
import com.padudjayaputera.sistem_akuntansi.service.UtangService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller untuk transaksi UTANG.
 * Endpoint ini hanya untuk proses utang (bukan entri harian umum).
 * Semua request harus berupa data utang yang valid.
 *
 * Endpoint:
 * - POST /api/v1/utang : tambah utang
 * - GET /api/v1/utang : list utang
 * - PUT /api/v1/utang/{id} : update utang
 * - DELETE /api/v1/utang/{id} : hapus utang
 */
@RestController
@RequestMapping("/api/v1/utang")
@RequiredArgsConstructor
public class UtangController {

    private final UtangService utangService;

    // Aturan keamanan yang sama dengan modul Piutang
    private static final String HAS_ACCESS_TO_FINANCE = 
        "hasAuthority('SUPER_ADMIN') or (hasAuthority('ADMIN_DIVISI') and authentication.principal.division.name == 'DIVISI KEUANGAN & ADMINISTRASI')";

    /**
     * Tambah transaksi utang baru.
     * Hanya menerima data utang, bukan data entri harian umum.
     */
    @PostMapping
    @PreAuthorize(HAS_ACCESS_TO_FINANCE)
    public ResponseEntity<UtangTransaksi> createUtang(@Valid @RequestBody UtangRequest request) {
        // Validasi: pastikan field wajib utang terisi
        if (request.getTipeTransaksi() == null || request.getKategori() == null) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<>(utangService.createUtang(request), HttpStatus.CREATED);
    }

    /**
     * Ambil semua transaksi utang.
     */
    @GetMapping
    @PreAuthorize(HAS_ACCESS_TO_FINANCE)
    public ResponseEntity<List<UtangTransaksi>> getAllUtang() {
        return ResponseEntity.ok(utangService.getAllUtang());
    }

    /**
     * Update transaksi utang.
     */
    @PutMapping("/{id}")
    @PreAuthorize(HAS_ACCESS_TO_FINANCE)
    public ResponseEntity<UtangTransaksi> updateUtang(@PathVariable Integer id, @Valid @RequestBody UtangRequest request) {
        return ResponseEntity.ok(utangService.updateUtang(id, request));
    }

    /**
     * Hapus transaksi utang.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize(HAS_ACCESS_TO_FINANCE)
    public ResponseEntity<Void> deleteUtang(@PathVariable Integer id) {
        utangService.deleteUtang(id);
        return ResponseEntity.noContent().build();
    }
}