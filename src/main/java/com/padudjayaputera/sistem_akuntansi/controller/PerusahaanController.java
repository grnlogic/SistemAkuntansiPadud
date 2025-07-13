package com.padudjayaputera.sistem_akuntansi.controller;

import java.util.List;
import java.util.stream.Collectors;

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

import com.padudjayaputera.sistem_akuntansi.dto.PerusahaanRequest;
import com.padudjayaputera.sistem_akuntansi.dto.PerusahaanResponse;
import com.padudjayaputera.sistem_akuntansi.model.Perusahaan;
import com.padudjayaputera.sistem_akuntansi.service.PerusahaanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/perusahaan")
@RequiredArgsConstructor
public class PerusahaanController {

    private final PerusahaanService perusahaanService;

    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<PerusahaanResponse> createPerusahaan(@Valid @RequestBody PerusahaanRequest request) {
        Perusahaan perusahaan = perusahaanService.createPerusahaan(request);
        return new ResponseEntity<>(new PerusahaanResponse(perusahaan), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PerusahaanResponse>> getAllPerusahaan() {
        List<Perusahaan> perusahaanList = perusahaanService.getAllPerusahaan();
        List<PerusahaanResponse> responses = perusahaanList.stream()
            .map(PerusahaanResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PerusahaanResponse> getPerusahaanById(@PathVariable Integer id) {
        Perusahaan perusahaan = perusahaanService.getPerusahaanById(id);
        return ResponseEntity.ok(new PerusahaanResponse(perusahaan));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<PerusahaanResponse> updatePerusahaan(@PathVariable Integer id, @Valid @RequestBody PerusahaanRequest request) {
        Perusahaan perusahaan = perusahaanService.updatePerusahaan(id, request);
        return ResponseEntity.ok(new PerusahaanResponse(perusahaan));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Void> deletePerusahaan(@PathVariable Integer id) {
        perusahaanService.deletePerusahaan(id);
        return ResponseEntity.noContent().build();
    }
}