package com.padudjayaputera.sistem_akuntansi.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.padudjayaputera.sistem_akuntansi.dto.EntriHarianRequest;
import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.model.EntriHarian;
import com.padudjayaputera.sistem_akuntansi.model.GudangStok;
import com.padudjayaputera.sistem_akuntansi.model.KeuanganSaldo;
import com.padudjayaputera.sistem_akuntansi.model.PemasaranPerformance;
import com.padudjayaputera.sistem_akuntansi.model.ProduksiHpp;
import com.padudjayaputera.sistem_akuntansi.model.User;
import com.padudjayaputera.sistem_akuntansi.model.UserRole;
import com.padudjayaputera.sistem_akuntansi.repository.AccountRepository;
import com.padudjayaputera.sistem_akuntansi.repository.EntriHarianRepository;
import com.padudjayaputera.sistem_akuntansi.repository.GudangStokRepository;
import com.padudjayaputera.sistem_akuntansi.repository.KeuanganSaldoRepository;
import com.padudjayaputera.sistem_akuntansi.repository.PemasaranPerformanceRepository;
import com.padudjayaputera.sistem_akuntansi.repository.ProduksiHppRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EntriHarianServiceImpl implements EntriHarianService {

    @PersistenceContext
    private EntityManager entityManager;

    private final EntriHarianRepository entriHarianRepository;
    private final AccountRepository accountRepository;
    private final PemasaranPerformanceRepository pemasaranPerformanceRepository;
    private final ProduksiHppRepository produksiHppRepository;
    private final GudangStokRepository gudangStokRepository;
    private final KeuanganSaldoRepository keuanganSaldoRepository;

    @Override
    public List<EntriHarian> getAllEntries() {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        List<EntriHarian> entries;
        if (loggedInUser.getRole() == UserRole.SUPER_ADMIN) {
            entries = entriHarianRepository.findAll();
        } else {
            entries = entriHarianRepository.findByAccountDivisionId(loggedInUser.getDivision().getId());
        }
        
        // ✅ ENHANCED DEBUG: Log entries being returned with specialized data
        log.info("Returning {} entries to frontend", entries.size());
        for (EntriHarian entry : entries) {
            if (entry.getTargetAmount() != null || entry.getRealisasiAmount() != null) {
                log.info("Entry {} has marketing data: target={}, realisasi={}", 
                        entry.getId(), entry.getTargetAmount(), entry.getRealisasiAmount());
            }
        }
        
        return entries;
    }

    @Override
    public List<EntriHarian> getEntriesByDate(LocalDate date) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (loggedInUser.getRole() == UserRole.SUPER_ADMIN) {
            return entriHarianRepository.findByTanggalLaporan(date);
        }
        
        return entriHarianRepository.findByTanggalLaporanAndAccountDivisionId(date, loggedInUser.getDivision().getId());
    }

    @Override
    public List<EntriHarian> getEntriesByDivision(Integer divisionId) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
            if (!divisionId.equals(loggedInUser.getDivision().getId())) {
                throw new AccessDeniedException("Anda tidak diizinkan mengakses data divisi lain.");
            }
        }
        
        return entriHarianRepository.findByAccountDivisionId(divisionId);
    }

    @Override
    public EntriHarian getEntryById(Integer id) {
        EntriHarian entry = entriHarianRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entri Harian dengan ID " + id + " tidak ditemukan."));

        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
            if (!entry.getAccount().getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                throw new AccessDeniedException("Anda tidak diizinkan mengakses entri di luar divisi Anda.");
            }
        }
        
        return entry;
    }

    @Override
    @Transactional
    public EntriHarian saveEntry(EntriHarianRequest request) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Akun dengan ID " + request.getAccountId() + " tidak ditemukan."));

        if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
            if (!account.getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                throw new AccessDeniedException("Anda tidak memiliki akses untuk mengisi data akun divisi lain.");
            }
        }

        EntriHarian newEntry = createEntriHarianFromRequest(request, account, loggedInUser);            EntriHarian savedEntry = entriHarianRepository.save(newEntry);
            
            // ✅ FORCE FLUSH to ensure all related records are created
            entityManager.flush();
            
            log.info("✅ Entry saved with ID: {}", savedEntry.getId());
            log.info("🔍 DEBUG: Transaction type = {}, Account = {}", 
                request.getTransactionType(), account.getAccountName());
            
            // ✅ CRITICAL DEBUG: About to call updateKeuanganSaldoWithSaldoAkhir
            log.info("🚀 CALLING updateKeuanganSaldoWithSaldoAkhir with entryId={}", savedEntry.getId());
            
            // ✅ SIMPLE DIRECT UPDATE for SALDO_AKHIR
            if (request.getTransactionType() == com.padudjayaputera.sistem_akuntansi.model.TransactionType.SALDO_AKHIR) {
                log.info("🔥 DIRECT SALDO_AKHIR UPDATE ATTEMPT");
                try {
                    BigDecimal saldoValue = request.getSaldoAkhir() != null ? 
                        request.getSaldoAkhir() : request.getNilai();
                    
                    String sql = "UPDATE keuangan_saldo SET saldo_akhir = ? WHERE entri_harian_id = ?";
                    int rows = entityManager.createNativeQuery(sql)
                        .setParameter(1, saldoValue)
                        .setParameter(2, savedEntry.getId())
                        .executeUpdate();
                    
                    log.info("🔥 DIRECT UPDATE RESULT: {} rows updated, value={}", rows, saldoValue);
                } catch (Exception e) {
                    log.error("🔥 DIRECT UPDATE ERROR: ", e);
                }
            }
            
            try {
                updateKeuanganSaldoWithSaldoAkhir(request, account, savedEntry);
                log.info("✅ updateKeuanganSaldoWithSaldoAkhir completed successfully");
            } catch (Exception e) {
                log.error("❌ ERROR in updateKeuanganSaldoWithSaldoAkhir: ", e);
            }
            
            return savedEntry;
    }

    @Override
    @Transactional
    public List<EntriHarian> saveBatchEntries(List<EntriHarianRequest> requests) {
        log.info("=== SERVICE: saveBatchEntries START ===");
        
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Request list tidak boleh kosong");
        }
        
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Logged in user: {} (ID: {})", loggedInUser.getUsername(), loggedInUser.getId());

        List<EntriHarian> savedEntries = new ArrayList<>();
        List<String> duplicateWarnings = new ArrayList<>();
        List<String> successMessages = new ArrayList<>();
        
        for (int i = 0; i < requests.size(); i++) {
            EntriHarianRequest request = requests.get(i);
            log.info("Processing request {}/{}: {}", i+1, requests.size(), request);
            
            try {
                if (request.getAccountId() == null || request.getTanggal() == null || request.getNilai() == null) {
                    log.warn("⚠️ Request {}/{} has null required fields - skipping", i+1, requests.size());
                    continue;
                }
                
                Account account = accountRepository.findById(request.getAccountId())
                        .orElseThrow(() -> new RuntimeException("Akun dengan ID " + request.getAccountId() + " tidak ditemukan."));

                if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
                    if (!account.getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                        log.warn("⚠️ Access denied for request {}/{}: user division {} != account division {}", 
                                i+1, requests.size(), loggedInUser.getDivision().getId(), account.getDivision().getId());
                        continue;
                    }
                }

                boolean isKeuanganDivision = account.getDivision().getName().toLowerCase().contains("keuangan");
                boolean isPemasaranDivision = account.getDivision().getName().toLowerCase().contains("pemasaran");
                
                EntriHarian entryToSave;
                boolean isNewEntry = false;
                boolean isDuplicate = false;
                
                // ✅ ENHANCED: Better duplicate detection and handling
                if (isKeuanganDivision && request.getTransactionType() != null) {
                    // For keuangan, check for similar transactions on same date
                    List<EntriHarian> existingKeuanganEntries = entriHarianRepository
                            .findKeuanganEntriesByTanggalAndAccountId(request.getTanggal(), request.getAccountId());
                    
                    // Check for exact duplicate (same type, amount, description)
                    boolean exactDuplicate = existingKeuanganEntries.stream().anyMatch(existing -> 
                        existing.getTransactionType() == request.getTransactionType() &&
                        existing.getNilai().compareTo(request.getNilai()) == 0 &&
                        Objects.equals(existing.getDescription(), request.getDescription())
                    );
                    
                    if (exactDuplicate) {
                        log.info("🔄 KEUANGAN DUPLICATE DETECTED: Similar transaction found for account {} on date {} with type {}", 
                                request.getAccountId(), request.getTanggal(), request.getTransactionType());
                        duplicateWarnings.add(String.format("Transaksi serupa sudah ada untuk akun %s (%s) pada %s", 
                                account.getAccountCode(), request.getTransactionType(), request.getTanggal()));
                        isDuplicate = true;
                    }
                    
                    // Always create new entry for keuangan (even if similar exists)
                    entryToSave = createEntriHarianFromRequest(request, account, loggedInUser);
                    isNewEntry = true;
                    
                } else if (isPemasaranDivision && request.isPemasaranData()) {
                    // For pemasaran, check for similar data on same date
                    List<EntriHarian> existingPemasaranEntries = entriHarianRepository
                            .findAllByTanggalLaporanAndAccountId(request.getTanggal(), request.getAccountId());
                    
                    // Check for very similar pemasaran data
                    boolean similarData = existingPemasaranEntries.stream().anyMatch(existing -> 
                        Objects.equals(existing.getTargetAmount(), request.getTargetAmount()) &&
                        Objects.equals(existing.getRealisasiAmount(), request.getRealisasiAmount()) &&
                        Objects.equals(existing.getDescription(), request.getDescription())
                    );
                    
                    if (similarData) {
                        log.info("🔄 PEMASARAN DUPLICATE DETECTED: Similar sales data found for account {} on date {}", 
                                request.getAccountId(), request.getTanggal());
                        duplicateWarnings.add(String.format("Data penjualan serupa sudah ada untuk akun %s pada %s", 
                                account.getAccountCode(), request.getTanggal()));
                        isDuplicate = true;
                    }
                    
                    // Always create new entry for pemasaran (multiple sales records allowed)
                    entryToSave = createEntriHarianFromRequest(request, account, loggedInUser);
                    isNewEntry = true;
                    
                } else {
                    // For other divisions, check for existing entries and update if found
                    Optional<EntriHarian> existingEntry = entriHarianRepository
                            .findByTanggalLaporanAndAccountId(request.getTanggal(), request.getAccountId());

                    if (existingEntry.isPresent()) {
                        log.info("🔄 UPDATING EXISTING: Found existing entry for account {} on date {}", 
                                request.getAccountId(), request.getTanggal());
                        
                        entryToSave = existingEntry.get();
                        
                        // Check if the update is significantly different
                        boolean significantChange = 
                            !entryToSave.getNilai().equals(request.getNilai()) ||
                            !Objects.equals(entryToSave.getDescription(), request.getDescription());
                        
                        if (!significantChange) {
                            log.info("📝 MINOR UPDATE: Data hampir sama untuk account {} pada {}", 
                                    account.getAccountCode(), request.getTanggal());
                            duplicateWarnings.add(String.format("Data hampir sama untuk akun %s pada %s - tetap diupdate", 
                                    account.getAccountCode(), request.getTanggal()));
                            isDuplicate = true;
                        }
                        
                        entryToSave.setNilai(request.getNilai());
                        entryToSave.setDescription(request.getDescription());
                        updateSpecializedFields(entryToSave, request);
                        
                    } else {
                        log.info("✨ CREATING NEW: Creating new entry for account {} on date {}", 
                                request.getAccountId(), request.getTanggal());
                        
                        entryToSave = createEntriHarianFromRequest(request, account, loggedInUser);
                        isNewEntry = true;
                    }
                }

                EntriHarian savedEntry = entriHarianRepository.save(entryToSave);
                
                // ✅ ENHANCED: Better success logging
                String action = isNewEntry ? "CREATED" : "UPDATED";
                String warningFlag = isDuplicate ? " (DUPLICATE DETECTED)" : "";
                
                log.info("✅ {} entry: ID={}, Account={} ({}), Amount={}, Target={}, Realisasi={}{}", 
                        action, savedEntry.getId(), savedEntry.getAccount().getAccountCode(), 
                        savedEntry.getAccount().getAccountName(), savedEntry.getNilai(),
                        savedEntry.getTargetAmount(), savedEntry.getRealisasiAmount(), warningFlag);
                
                successMessages.add(String.format("%s %s untuk akun %s%s", 
                        action, 
                        isNewEntry ? "entry baru" : "data existing",
                        account.getAccountCode(),
                        warningFlag));
                
                saveDivisionSpecificData(savedEntry, request);
                savedEntries.add(savedEntry);
                
            } catch (Exception e) {
                log.error("❌ ERROR processing request {}/{}: {}", i+1, requests.size(), e.getMessage(), e);
                // Continue processing other requests instead of failing entirely
                continue;
            }
        }
        
        // ✅ ENHANCED: Comprehensive result logging
        log.info("=== BATCH SAVE SUMMARY ===");
        log.info("📊 Total requests: {}", requests.size());
        log.info("✅ Successfully saved: {}", savedEntries.size());
        log.info("⚠️ Duplicate warnings: {}", duplicateWarnings.size());
        
        if (!duplicateWarnings.isEmpty()) {
            log.info("🔄 DUPLICATE DETAILS:");
            duplicateWarnings.forEach(warning -> log.info("   - {}", warning));
        }
        
        if (!successMessages.isEmpty()) {
            log.info("✅ SUCCESS DETAILS:");
            successMessages.forEach(message -> log.info("   - {}", message));
        }
        
        log.info("=== END BATCH SAVE ===");
        
        return savedEntries;
    }

    private void updateSpecializedFields(EntriHarian entry, EntriHarianRequest request) {
        if (request.getTransactionType() != null) {
            entry.setTransactionType(request.getTransactionType());
        }
        if (request.getTargetAmount() != null) {
            entry.setTargetAmount(request.getTargetAmount());
        }
        if (request.getRealisasiAmount() != null) {
            entry.setRealisasiAmount(request.getRealisasiAmount());
        }
        if (request.getHppAmount() != null) {
            entry.setHppAmount(request.getHppAmount());
        }
        if (request.getPemakaianAmount() != null) {
            entry.setPemakaianAmount(request.getPemakaianAmount());
        }
        if (request.getStokAkhir() != null) {
            entry.setStokAkhir(request.getStokAkhir());
        }
    }

    private EntriHarian createEntriHarianFromRequest(EntriHarianRequest request, Account account, User user) {
        EntriHarian newEntry = new EntriHarian();
        newEntry.setAccount(account);
        newEntry.setTanggalLaporan(request.getTanggal());
        newEntry.setNilai(request.getNilai());
        newEntry.setDescription(request.getDescription());
        newEntry.setUser(user);
        
        // ✅ ENHANCED: Explicit logging and assignment of specialized fields
        log.info("Setting specialized fields for entry:");
        log.info("  - transactionType: {}", request.getTransactionType());
        log.info("  - targetAmount: {}", request.getTargetAmount());
        log.info("  - realisasiAmount: {}", request.getRealisasiAmount());
        log.info("  - hppAmount: {}", request.getHppAmount());
        log.info("  - pemakaianAmount: {}", request.getPemakaianAmount());
        log.info("  - stokAkhir: {}", request.getStokAkhir());
        log.info("  - saldoAkhir: {}", request.getSaldoAkhir());
        
        newEntry.setTransactionType(request.getTransactionType());
        newEntry.setTargetAmount(request.getTargetAmount());
        newEntry.setRealisasiAmount(request.getRealisasiAmount());
        newEntry.setHppAmount(request.getHppAmount());
        newEntry.setPemakaianAmount(request.getPemakaianAmount());
        newEntry.setStokAkhir(request.getStokAkhir());
        newEntry.setSaldoAkhir(request.getSaldoAkhir());
        
        // ✅ SPECIAL HANDLING: Jika transaction type adalah SALDO_AKHIR, set saldoAkhir dengan nilai dari nilai field
        handleSaldoAkhirTransaction(newEntry, request);

        // ✅ VERIFY: Log what was actually set
        log.info("Entry created with values:");
        log.info("  - targetAmount set to: {}", newEntry.getTargetAmount());
        log.info("  - realisasiAmount set to: {}", newEntry.getRealisasiAmount());
        log.info("  - saldoAkhir set to: {}", newEntry.getSaldoAkhir());
        
        return newEntry;
    }

    private void saveDivisionSpecificData(EntriHarian savedEntry, EntriHarianRequest request) {
        String divisionName = savedEntry.getAccount().getDivision().getName().toLowerCase();
        
        log.info("Saving division specific data for division: {}", divisionName);
        
        try {
            if (divisionName.contains("pemasaran") && request.isPemasaranData()) {
                savePemasaranPerformance(savedEntry, request);
            }
            
            if (divisionName.contains("produksi") && request.isProduksiData()) {
                saveProduksiHpp(savedEntry, request);
            }
            
            if (divisionName.contains("gudang") && request.isGudangData()) {
                saveGudangStok(savedEntry, request);
            }
            
            if (divisionName.contains("keuangan") && request.isKeuanganData()) {
                saveKeuanganSaldo(savedEntry, request);
            }
            
        } catch (Exception e) {
            log.warn("Failed to save division specific data for {}: {}", divisionName, e.getMessage());
        }
    }

    private void savePemasaranPerformance(EntriHarian entry, EntriHarianRequest request) {
        log.info("Saving pemasaran performance data for entry ID: {}", entry.getId());
        log.info("Target: {}, Realisasi: {}", request.getTargetAmount(), request.getRealisasiAmount());
        
        try {
            // ✅ ENHANCED: Only create performance record if we have meaningful data
            if (request.getTargetAmount() != null || request.getRealisasiAmount() != null) {
                PemasaranPerformance performance = new PemasaranPerformance();
                performance.setEntriHarian(entry);
                performance.setTargetAmount(request.getTargetAmount() != null ? request.getTargetAmount() : BigDecimal.ZERO);
                performance.setRealisasiAmount(request.getRealisasiAmount() != null ? request.getRealisasiAmount() : BigDecimal.ZERO);
                performance.setTanggalLaporan(entry.getTanggalLaporan());
                
                performance.setSalesPerson(extractSalesPersonFromDescription(request.getDescription()));
                performance.setProdukKategori(extractProductCategoryFromAccount(entry.getAccount()));
                
                PemasaranPerformance saved = pemasaranPerformanceRepository.save(performance);
                log.info("Successfully saved pemasaran performance with ID: {}, Performance: {}%", 
                        saved.getId(), saved.getPerformancePercentage());
            } else {
                log.warn("No meaningful pemasaran data to save for entry ID: {}", entry.getId());
            }
            
        } catch (Exception e) {
            log.error("Failed to save pemasaran performance: {}", e.getMessage(), e);
            // Don't throw exception, just log warning
            log.warn("Continuing without pemasaran performance record due to error: {}", e.getMessage());
        }
    }

    private void saveProduksiHpp(EntriHarian entry, EntriHarianRequest request) {
        log.info("Saving produksi HPP data for entry ID: {}", entry.getId());
        
        try {
            ProduksiHpp produksi = new ProduksiHpp();
            produksi.setEntriHarian(entry);
            produksi.setProdukName(extractProductNameFromAccount(entry.getAccount()));
            produksi.setJumlahProduksi(entry.getNilai());
            produksi.setHppTotal(request.getHppAmount() != null ? request.getHppAmount() : BigDecimal.ZERO);
            produksi.setTanggalProduksi(entry.getTanggalLaporan());
            produksi.setShiftKerja(extractShiftFromDescription(request.getDescription()));
            produksi.setOperator(entry.getUser().getUsername());
            
            ProduksiHpp saved = produksiHppRepository.save(produksi);
            log.info("Successfully saved produksi HPP with ID: {}, HPP per unit: {}", 
                    saved.getId(), saved.getHppPerUnit());
            
        } catch (Exception e) {
            log.error("Failed to save produksi HPP: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal menyimpan data HPP produksi: " + e.getMessage());
        }
    }

    private void saveGudangStok(EntriHarian entry, EntriHarianRequest request) {
        log.info("Saving gudang stok data for entry ID: {}", entry.getId());
        
        try {
            GudangStok stok = new GudangStok();
            stok.setEntriHarian(entry);
            stok.setBahanBakuName(extractMaterialNameFromAccount(entry.getAccount()));
            stok.setTanggalUpdate(entry.getTanggalLaporan());
            stok.setPemakaianHariIni(request.getPemakaianAmount() != null ? request.getPemakaianAmount() : BigDecimal.ZERO);
            stok.setPicGudang(entry.getUser().getUsername());
            
            String bahanBakuName = stok.getBahanBakuName();
            Optional<GudangStok> latestStok = gudangStokRepository.findLatestByBahanBakuName(bahanBakuName);
            if (latestStok.isPresent()) {
                stok.setStokAwal(latestStok.get().getStokAkhir());
            } else {
                stok.setStokAwal(request.getStokAkhir() != null ? request.getStokAkhir() : BigDecimal.valueOf(1000));
            }
            
            stok.setStokMinimum(BigDecimal.valueOf(100));
            stok.setLokasiGudang(extractLocationFromDescription(request.getDescription()));
            stok.setSatuan(extractUnitFromAccount(entry.getAccount()));
            
            GudangStok saved = gudangStokRepository.save(stok);
            log.info("Successfully saved gudang stok with ID: {}, Status: {}, Stok akhir: {}", 
                    saved.getId(), saved.getStatusStok(), saved.getStokAkhir());
            
        } catch (Exception e) {
            log.error("Failed to save gudang stok: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal menyimpan data stok gudang: " + e.getMessage());
        }
    }

    private void saveKeuanganSaldo(EntriHarian entry, EntriHarianRequest request) {
        log.info("Saving keuangan saldo data for entry ID: {}", entry.getId());
        
        try {
            KeuanganSaldo saldo = new KeuanganSaldo();
            saldo.setEntriHarian(entry);
            saldo.setAccount(entry.getAccount());
            saldo.setTanggalTransaksi(entry.getTanggalLaporan());
            
            Optional<KeuanganSaldo> latestSaldo = keuanganSaldoRepository
                    .findLatestByAccountId(entry.getAccount().getId());
            
            if (latestSaldo.isPresent()) {
                saldo.setSaldoAwal(latestSaldo.get().getSaldoAkhir());
                log.info("Found latest saldo for account {}: {}", entry.getAccount().getId(), latestSaldo.get().getSaldoAkhir());
            } else {
                saldo.setSaldoAwal(BigDecimal.ZERO);
                log.info("No previous saldo found for account {}, starting with 0", entry.getAccount().getId());
            }
            
            if (request.getTransactionType() != null) {
                switch (request.getTransactionType()) {
                    case PENERIMAAN -> {
                        saldo.setPenerimaan(entry.getNilai());
                        saldo.setPengeluaran(BigDecimal.ZERO);
                        log.info("Setting PENERIMAAN: {}", entry.getNilai());
                    }
                    case PENGELUARAN -> {
                        saldo.setPenerimaan(BigDecimal.ZERO);
                        saldo.setPengeluaran(entry.getNilai());
                        log.info("Setting PENGELUARAN: {}", entry.getNilai());
                    }
                }
            } else {
                saldo.setPenerimaan(BigDecimal.ZERO);
                saldo.setPengeluaran(BigDecimal.ZERO);
                log.warn("No transaction type specified for keuangan entry");
            }
            
            saldo.setKeterangan(request.getDescription());
            
            // Save the keuangan saldo data
            KeuanganSaldo savedKeuanganSaldo = keuanganSaldoRepository.save(saldo);
            
            // Log yang sudah ada
            log.info("Successfully saved keuangan saldo with ID: {}, Transaction type: {}, Amount: {}, Saldo akhir: {}", 
                savedKeuanganSaldo.getId(), request.getTransactionType(), request.getNilai(), 
                savedKeuanganSaldo.getSaldoAkhir());
            
            // ✅ CRITICAL FIX: Update saldo_akhir manually for SALDO_AKHIR transactions  
            if (request.getTransactionType() == com.padudjayaputera.sistem_akuntansi.model.TransactionType.SALDO_AKHIR) {
                log.info("🔥 EXECUTING SALDO_AKHIR MANUAL UPDATE for KeuanganSaldo ID: {}", savedKeuanganSaldo.getId());
                try {
                    BigDecimal saldoValue = request.getSaldoAkhir() != null ? 
                        request.getSaldoAkhir() : request.getNilai();
                    
                    String sql = "UPDATE keuangan_saldo SET saldo_akhir = ? WHERE id = ?";
                    int rows = entityManager.createNativeQuery(sql)
                        .setParameter(1, saldoValue)
                        .setParameter(2, savedKeuanganSaldo.getId())
                        .executeUpdate();
                    
                    log.info("🔥 MANUAL UPDATE SUCCESS: {} rows updated, saldo_akhir={}, keuanganSaldoId={}", 
                        rows, saldoValue, savedKeuanganSaldo.getId());
                        
                    if (rows > 0) {
                        entityManager.flush(); // Force commit
                        log.info("✅ EntityManager flushed - saldo_akhir should be updated in database");
                    }
                } catch (Exception e) {
                    log.error("🔥 MANUAL UPDATE ERROR: ", e);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to save keuangan saldo: {}", e.getMessage(), e);
            log.warn("Continuing without keuangan saldo record due to error: {}", e.getMessage());
        }
    }

    // Helper methods
    private String extractSalesPersonFromDescription(String description) {
        if (description != null && description.toLowerCase().contains("sales")) {
            String[] parts = description.split("\\s+");
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i].toLowerCase().contains("sales")) {
                    return parts[i + 1];
                }
            }
        }
        return "Unknown";
    }
    
    private String extractProductCategoryFromAccount(Account account) {
        String accountName = account.getAccountName().toLowerCase();
        if (accountName.contains("elektronik")) return "Elektronik";
        if (accountName.contains("makanan")) return "Makanan";
        if (accountName.contains("pakaian")) return "Pakaian";
        if (accountName.contains("otomotif")) return "Otomotif";
        return "General";
    }
    
    private String extractProductNameFromAccount(Account account) {
        String name = account.getAccountName();
        if (name.contains("-")) {
            return name.split("-")[0].trim();
        }
        return name;
    }
    
    private ProduksiHpp.ShiftKerja extractShiftFromDescription(String description) {
        if (description != null) {
            String desc = description.toLowerCase();
            if (desc.contains("siang")) return ProduksiHpp.ShiftKerja.SIANG;
            if (desc.contains("malam")) return ProduksiHpp.ShiftKerja.MALAM;
        }
        return ProduksiHpp.ShiftKerja.PAGI;
    }
    
    private String extractMaterialNameFromAccount(Account account) {
        String name = account.getAccountName();
        if (name.contains("Bahan Baku")) {
            return name.replace("Bahan Baku", "").trim();
        }
        return name;
    }
    
    private String extractLocationFromDescription(String description) {
        if (description != null && description.toLowerCase().contains("gudang")) {
            String[] parts = description.split("\\s+");
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i].toLowerCase().contains("gudang")) {
                    return "Gudang " + parts[i + 1];
                }
            }
        }
        return "Gudang Utama";
    }
    
    private String extractUnitFromAccount(Account account) {
        String name = account.getAccountName().toLowerCase();
        if (name.contains("kg") || name.contains("kilogram")) return "KG";
        if (name.contains("liter") || name.contains("l")) return "LITER";
        if (name.contains("pcs") || name.contains("pieces")) return "PCS";
        if (name.contains("box") || name.contains("kardus")) return "BOX";
        return "UNIT";
    }

    @Override
    public EntriHarian updateEntry(Integer id, EntriHarianRequest request) {
        EntriHarian existingEntry = entriHarianRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entri Harian dengan ID " + id + " tidak ditemukan."));

        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
            if (!existingEntry.getAccount().getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                throw new AccessDeniedException("Anda tidak diizinkan mengubah entri harian di luar divisi Anda.");
            }
        }

        existingEntry.setNilai(request.getNilai());
        existingEntry.setDescription(request.getDescription());
        existingEntry.setTransactionType(request.getTransactionType());
        existingEntry.setTargetAmount(request.getTargetAmount());
        existingEntry.setRealisasiAmount(request.getRealisasiAmount());
        existingEntry.setHppAmount(request.getHppAmount());
        existingEntry.setPemakaianAmount(request.getPemakaianAmount());
        existingEntry.setStokAkhir(request.getStokAkhir());
        existingEntry.setSaldoAkhir(request.getSaldoAkhir());

        return entriHarianRepository.save(existingEntry);
    }

    @Override
    public void deleteEntry(Integer id) {
        EntriHarian existingEntry = entriHarianRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entri Harian dengan ID " + id + " tidak ditemukan."));

        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
            if (!existingEntry.getAccount().getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                throw new AccessDeniedException("Anda tidak diizinkan menghapus entri harian di luar divisi Anda.");
            }
        }

        entriHarianRepository.deleteById(id);
    }

    // ✅ NEW: Helper method to detect and log duplicates nicely
    private boolean isDuplicateEntry(EntriHarian existing, EntriHarianRequest request) {
        boolean isDup = false;
        List<String> differences = new ArrayList<>();
        List<String> similarities = new ArrayList<>();
        
        // Check nilai
        if (existing.getNilai().compareTo(request.getNilai()) == 0) {
            similarities.add("nilai sama (" + existing.getNilai() + ")");
        } else {
            differences.add(String.format("nilai berbeda (%s -> %s)", existing.getNilai(), request.getNilai()));
        }
        
        // Check description
        if (Objects.equals(existing.getDescription(), request.getDescription())) {
            similarities.add("deskripsi sama");
        } else {
            differences.add("deskripsi berbeda");
        }
        
        // Check specialized fields
        if (request.getTargetAmount() != null && Objects.equals(existing.getTargetAmount(), request.getTargetAmount())) {
            similarities.add("target amount sama");
        }
        
        if (request.getRealisasiAmount() != null && Objects.equals(existing.getRealisasiAmount(), request.getRealisasiAmount())) {
            similarities.add("realisasi amount sama");
        }
        
        // Consider duplicate if most fields are the same
        isDup = similarities.size() >= 2 && differences.size() <= 1;
        
        if (isDup) {
            log.info("🔍 DUPLICATE ANALYSIS - Entry ID {}: SIMILAR FOUND", existing.getId());
            log.info("   ✅ Similarities: {}", String.join(", ", similarities));
            if (!differences.isEmpty()) {
                log.info("   ⚠️ Differences: {}", String.join(", ", differences));
            }
        } else {
            log.info("🔍 DUPLICATE ANALYSIS - Entry ID {}: SIGNIFICANT DIFFERENCES", existing.getId());
            log.info("   ⚠️ Differences: {}", String.join(", ", differences));
            log.info("   ✅ Similarities: {}", String.join(", ", similarities));
        }
        
        return isDup;
    }

    // ✅ NEW: Helper to log save operations nicely
    private void logSaveOperation(String operation, EntriHarian entry, boolean isDuplicate, String context) {
        String duplicateFlag = isDuplicate ? " [DUPLICATE DETECTED]" : "";
        String divisionName = entry.getAccount().getDivision().getName();
        
        log.info("💾 {} {}: Account={} ({}), Division={}, Amount={}, Date={}{}",
                operation,
                context,
                entry.getAccount().getAccountCode(),
                entry.getAccount().getAccountName(),
                divisionName,
                entry.getNilai(),
                entry.getTanggalLaporan(),
                duplicateFlag);
                
        // Log specialized data if present
        if (entry.getTargetAmount() != null || entry.getRealisasiAmount() != null) {
            log.info("   📊 Marketing Data: Target={}, Realisasi={}, Achievement={}%",
                    entry.getTargetAmount(),
                    entry.getRealisasiAmount(),
                    entry.getPerformancePercentage());
        }
        
        if (entry.getHppAmount() != null) {
            log.info("   🏭 Production Data: HPP={}, HPP per Unit={}",
                    entry.getHppAmount(),
                    entry.getHppPerUnit());
        }
        
        if (entry.getTransactionType() != null) {
            log.info("   💰 Financial Data: Type={}", entry.getTransactionType());
        }
    }
    
    // ✅ NEW: Helper method untuk handle SALDO_AKHIR transaction type
    private void handleSaldoAkhirTransaction(EntriHarian entry, EntriHarianRequest request) {
        if (request.getTransactionType() == com.padudjayaputera.sistem_akuntansi.model.TransactionType.SALDO_AKHIR) {
            // Untuk SALDO_AKHIR, simpan nilai ke field saldoAkhir
            BigDecimal saldoAkhirValue = request.getSaldoAkhir() != null ? 
                request.getSaldoAkhir() : request.getNilai();
            entry.setSaldoAkhir(saldoAkhirValue);
            entry.setNilai(BigDecimal.ZERO); // Set nilai transaksi ke 0
            
            log.info("SALDO_AKHIR transaction: saldoAkhir = {}, nilai = 0", saldoAkhirValue);
        }
    }

    // ✅ ENHANCED DEBUG: Update KeuanganSaldo to handle manual saldo_akhir using EntityManager
    private void updateKeuanganSaldoWithSaldoAkhir(EntriHarianRequest request, Account account, EntriHarian savedEntry) {
        log.info("🔍 DEBUG updateKeuanganSaldoWithSaldoAkhir: Called with entryId={}, transactionType={}", 
            savedEntry.getId(), request.getTransactionType());
        
        if (account.getDivision().getName().equalsIgnoreCase("KEUANGAN")) {
            log.info("🔍 DEBUG: Division is KEUANGAN");
            
            if (account.getAccountName().toLowerCase().contains("kas")) {
                log.info("🔍 DEBUG: Account contains 'kas'");
                
                if (request.getTransactionType() == com.padudjayaputera.sistem_akuntansi.model.TransactionType.SALDO_AKHIR) {
                    log.info("🔍 DEBUG: Transaction type is SALDO_AKHIR");
                    
                    try {
                        BigDecimal saldoAkhirValue = request.getSaldoAkhir() != null ? 
                            request.getSaldoAkhir() : request.getNilai();
                        
                        log.info("🔍 DEBUG: About to update saldo_akhir with value: {}", saldoAkhirValue);
                        
                        // Add delay to ensure KeuanganSaldo is created first
                        Thread.sleep(100);
                        
                        // Use EntityManager to update
                        int rowsUpdated = entityManager.createNativeQuery(
                            "UPDATE keuangan_saldo SET saldo_akhir = :saldoAkhir WHERE entri_harian_id = :entryId")
                            .setParameter("saldoAkhir", saldoAkhirValue)
                            .setParameter("entryId", savedEntry.getId())
                            .executeUpdate();
                        
                        log.info("✅ SUCCESS: Manual saldo_akhir updated: {} rows affected, value = {}, entryId = {}", 
                            rowsUpdated, saldoAkhirValue, savedEntry.getId());
                        
                        if (rowsUpdated == 0) {
                            log.warn("⚠️ WARNING: No rows updated - KeuanganSaldo might not exist yet");
                        }
                        
                    } catch (Exception e) {
                        log.error("❌ ERROR updating manual saldo_akhir: ", e);
                    }
                } else {
                    log.info("🔍 DEBUG: Transaction type is NOT SALDO_AKHIR: {}", request.getTransactionType());
                }
            } else {
                log.info("🔍 DEBUG: Account does NOT contain 'kas': {}", account.getAccountName());
            }
        } else {
            log.info("🔍 DEBUG: Division is NOT KEUANGAN: {}", account.getDivision().getName());
        }
    }
}