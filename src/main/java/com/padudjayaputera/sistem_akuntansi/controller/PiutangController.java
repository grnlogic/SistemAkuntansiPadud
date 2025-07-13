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

import com.padudjayaputera.sistem_akuntansi.dto.PiutangRequest;
import com.padudjayaputera.sistem_akuntansi.model.PiutangTransaksi;
import com.padudjayaputera.sistem_akuntansi.service.PiutangService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller untuk transaksi PIUTANG.
 * Endpoint ini hanya untuk proses piutang (bukan entri harian umum).
 * Semua request harus berupa data piutang yang valid.
 *
 * Endpoint:
 * - POST /api/v1/piutang : tambah piutang
 * - GET /api/v1/piutang : list piutang
 * - PUT /api/v1/piutang/{id} : update piutang
 * - DELETE /api/v1/piutang/{id} : hapus piutang
 */
@RestController
@RequestMapping("/api/v1/piutang")
@RequiredArgsConstructor
public class PiutangController {

    private final PiutangService piutangService;

    // Aturan keamanan baru yang lebih spesifik
    private static final String HAS_ACCESS_TO_PIUTANG = 
        "hasAuthority('SUPER_ADMIN') or (hasAuthority('ADMIN_DIVISI') and authentication.principal.division.name == 'DIVISI KEUANGAN & ADMINISTRASI')";

    /**
     * Tambah transaksi piutang baru.
     * Hanya menerima data piutang, bukan data entri harian umum.
     * Kategori piutang diset otomatis oleh sistem.
     */
    @PostMapping
    @PreAuthorize(HAS_ACCESS_TO_PIUTANG)
    public ResponseEntity<PiutangTransaksi> createPiutang(@Valid @RequestBody PiutangRequest request) {
        // Validasi: pastikan field wajib piutang terisi (kategori diset otomatis)
        if (request.getTipeTransaksi() == null) {
            return ResponseEntity.badRequest().build();
        }
        PiutangTransaksi createdPiutang = piutangService.createPiutang(request);
        return new ResponseEntity<>(createdPiutang, HttpStatus.CREATED);
    }

    /**
     * Ambil semua transaksi piutang.
     */
    @GetMapping
    @PreAuthorize(HAS_ACCESS_TO_PIUTANG)
    public ResponseEntity<List<PiutangTransaksi>> getAllPiutang() {
        List<PiutangTransaksi> allPiutang = piutangService.getAllPiutang();
        return ResponseEntity.ok(allPiutang);
    }

    /**
     * Update transaksi piutang.
     */
    @PutMapping("/{id}")
    @PreAuthorize(HAS_ACCESS_TO_PIUTANG)
    public ResponseEntity<PiutangTransaksi> updatePiutang(@PathVariable Integer id, @Valid @RequestBody PiutangRequest request) {
        PiutangTransaksi updatedPiutang = piutangService.updatePiutang(id, request);
        return ResponseEntity.ok(updatedPiutang);
    }

    /**
     * Hapus transaksi piutang.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize(HAS_ACCESS_TO_PIUTANG)
    public ResponseEntity<Void> deletePiutang(@PathVariable Integer id) {
        piutangService.deletePiutang(id);
        return ResponseEntity.noContent().build();
    }
}