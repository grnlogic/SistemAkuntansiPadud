package com.padudjayaputera.sistem_akuntansi.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.padudjayaputera.sistem_akuntansi.dto.EntriHarianRequest;
import com.padudjayaputera.sistem_akuntansi.model.EntriHarian;
import com.padudjayaputera.sistem_akuntansi.service.EntriHarianService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/entri-harian")
@RequiredArgsConstructor
@Slf4j
public class EntriHarianController {

    private final EntriHarianService entriHarianService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EntriHarian>> getAllEntries() {
        List<EntriHarian> entries = entriHarianService.getAllEntries();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EntriHarian>> getEntriesByDate(@PathVariable String date) {
        log.info("🔍 METHOD CALLED: getEntriesByDate for date: {}", date);
        
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<EntriHarian> entries = entriHarianService.getEntriesByDate(localDate);
            
            // ✅ ADDED: Log response for debugging
            log.info("Returning {} entries for date {}", entries.size(), date);
            
            // ✅ DEBUG: Log response mapping
            for (EntriHarian entry : entries) {
    log.info("🔍 RESPONSE MAP: id={}, transactionType={}, saldoAkhir={}", 
        entry.getId(), entry.getTransactionType(), entry.getSaldoAkhir());
}

            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            log.error("Error getting entries by date: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/division/{divisionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EntriHarian>> getEntriesByDivision(@PathVariable Integer divisionId) {
        List<EntriHarian> entries = entriHarianService.getEntriesByDivision(divisionId);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EntriHarian> getEntryById(@PathVariable Integer id) {
        EntriHarian entry = entriHarianService.getEntryById(id);
        return ResponseEntity.ok(entry);
    }

    @PostMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createBatchEntries(@RequestBody List<EntriHarianRequest> requests, HttpServletRequest httpRequest) {
        try {
            log.info("=== BATCH ENTRY CREATION START ===");
            log.info("Content-Type: {}", httpRequest.getContentType());
            log.info("Request size: {}", requests != null ? requests.size() : "null");
            
            if (requests == null || requests.isEmpty()) {
                log.error("Request is null or empty");
                return ResponseEntity.badRequest().body(createErrorResponse("Request tidak boleh kosong", null));
            }

            // ✅ ENHANCED: Better validation with detailed logging
            List<String> validationErrors = new ArrayList<>();
            for (int i = 0; i < requests.size(); i++) {
                EntriHarianRequest request = requests.get(i);
                log.info("Request[{}]: accountId={}, tanggal={}, nilai={}, targetAmount={}, realisasiAmount={}", 
                        i, request.getAccountId(), request.getTanggal(), request.getNilai(),
                        request.getTargetAmount(), request.getRealisasiAmount());
                
                if (request.getAccountId() == null) {
                    validationErrors.add("Request ke-" + (i+1) + ": AccountId tidak boleh null");
                }
                if (request.getTanggal() == null) {
                    validationErrors.add("Request ke-" + (i+1) + ": Tanggal tidak boleh null");
                }
                if (request.getNilai() == null) {
                    validationErrors.add("Request ke-" + (i+1) + ": Nilai tidak boleh null");
                }
            }

            if (!validationErrors.isEmpty()) {
                log.warn("⚠️ Validation errors found: {}", validationErrors);
                return ResponseEntity.badRequest().body(createErrorResponse("Validation errors", validationErrors));
            }

            List<EntriHarian> savedEntries = entriHarianService.saveBatchEntries(requests);
            
            // ✅ ENHANCED: Create detailed response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", savedEntries);
            response.put("totalRequested", requests.size());
            response.put("totalSaved", savedEntries.size());
            
            if (savedEntries.size() < requests.size()) {
                response.put("message", "Sebagian entri berhasil disimpan. Beberapa mungkin duplikat atau ada masalah validasi.");
                response.put("warnings", List.of("Cek log server untuk detail duplikat atau masalah lainnya"));
            } else {
                response.put("message", "Semua entri berhasil disimpan");
            }
            
            log.info("✅ Successfully processed {}/{} entries", savedEntries.size(), requests.size());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("Validation error: " + e.getMessage(), null));
            
        } catch (AccessDeniedException e) {
            log.warn("⚠️ Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createErrorResponse(e.getMessage(), null));
            
        } catch (Exception e) {
            log.error("❌ Unexpected error in batch creation: {}", e.getMessage(), e);
            
            // ✅ ENHANCED: Don't expose internal errors, but log them properly
            String userMessage = "Terjadi masalah pada server. Tim teknis telah diberitahu.";
            
            if (e.getMessage().contains("constraint") || e.getMessage().contains("duplicate")) {
                userMessage = "Beberapa data sudah ada. Sistem akan mencoba menangani duplikat secara otomatis.";
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(userMessage, List.of("Error ID: " + System.currentTimeMillis())));
        }
    }

    // ✅ HELPER: Create consistent error response
    private Map<String, Object> createErrorResponse(String message, List<String> details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        if (details != null && !details.isEmpty()) {
            response.put("details", details);
        }
        response.put("timestamp", java.time.LocalDateTime.now());
        return response;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EntriHarian> createEntry(@RequestBody EntriHarianRequest request) {
        EntriHarian savedEntry = entriHarianService.saveEntry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntry);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EntriHarian> updateEntry(@PathVariable Integer id, @RequestBody EntriHarianRequest request) {
        EntriHarian updatedEntry = entriHarianService.updateEntry(id, request);
        return ResponseEntity.ok(updatedEntry);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteEntry(@PathVariable Integer id) {
        entriHarianService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }
}