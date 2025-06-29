package com.padudjayaputera.sistem_akuntansi.controller;

import com.padudjayaputera.sistem_akuntansi.model.Salesperson;
import com.padudjayaputera.sistem_akuntansi.repository.SalespersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/salespeople")
@RequiredArgsConstructor
public class SalespersonController {

    private final SalespersonRepository salespersonRepository;
    private static final String HAS_ACCESS_TO_PEMASARAN = "hasAuthority('SUPER_ADMIN') or (hasAuthority('ADMIN_DIVISI') and authentication.principal.division.name == 'DIVISI PEMASARAN & PENJUALAN')";

    // Membuat Salesperson baru
    @PostMapping
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<Salesperson> createSalesperson(@RequestBody Salesperson salesperson) {
        return new ResponseEntity<>(salespersonRepository.save(salesperson), HttpStatus.CREATED);
    }

    // Mendapatkan semua Salesperson
    @GetMapping
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<List<Salesperson>> getAllSalespeople() {
        return ResponseEntity.ok(salespersonRepository.findAll());
    }
}