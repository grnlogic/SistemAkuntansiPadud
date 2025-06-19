package com.padudjayaputera.sistem_akuntansi.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntriHarianServiceImpl implements EntriHarianService {

    private final EntriHarianRepository entriHarianRepository;
    private final AccountRepository accountRepository;
    private final PemasaranPerformanceRepository pemasaranPerformanceRepository;
    private final ProduksiHppRepository produksiHppRepository;
    private final GudangStokRepository gudangStokRepository;
    private final KeuanganSaldoRepository keuanganSaldoRepository;

    @Override
    public List<EntriHarian> getAllEntries() {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (loggedInUser.getRole() == UserRole.SUPER_ADMIN) {
            return entriHarianRepository.findAll();
        }
        
        return entriHarianRepository.findByAccountDivisionId(loggedInUser.getDivision().getId());
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

        EntriHarian newEntry = createEntriHarianFromRequest(request, account, loggedInUser);
        EntriHarian savedEntry = entriHarianRepository.save(newEntry);
        saveDivisionSpecificData(savedEntry, request);

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
        
        for (EntriHarianRequest request : requests) {
            log.info("Processing request: {}", request);
            
            if (request.getAccountId() == null || request.getTanggal() == null || request.getNilai() == null) {
                throw new IllegalArgumentException("AccountId, tanggal, dan nilai tidak boleh null");
            }
            
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Akun dengan ID " + request.getAccountId() + " tidak ditemukan."));

            if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
                if (!account.getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                    throw new AccessDeniedException("Anda tidak memiliki akses untuk mengisi data akun divisi lain.");
                }
            }

            boolean isKeuanganDivision = account.getDivision().getName().toLowerCase().contains("keuangan");
            
            EntriHarian entryToSave;
            
            if (isKeuanganDivision && request.getTransactionType() != null) {
                log.info("KEUANGAN DIVISION: Creating NEW transaction entry for account {} on date {} with type {}", 
                        request.getAccountId(), request.getTanggal(), request.getTransactionType());
                
                entryToSave = createEntriHarianFromRequest(request, account, loggedInUser);
                
            } else {
                Optional<EntriHarian> existingEntry = entriHarianRepository
                        .findByTanggalLaporanAndAccountId(request.getTanggal(), request.getAccountId());

                if (existingEntry.isPresent()) {
                    log.info("NON-KEUANGAN DIVISION: Found existing entry for account {} on date {}, UPDATING...", 
                            request.getAccountId(), request.getTanggal());
                    
                    entryToSave = existingEntry.get();
                    entryToSave.setNilai(request.getNilai());
                    entryToSave.setDescription(request.getDescription());
                    updateSpecializedFields(entryToSave, request);
                    
                } else {
                    log.info("NON-KEUANGAN DIVISION: Creating NEW entry for account {} on date {}", 
                            request.getAccountId(), request.getTanggal());
                    
                    entryToSave = createEntriHarianFromRequest(request, account, loggedInUser);
                }
            }

            EntriHarian savedEntry = entriHarianRepository.save(entryToSave);
            log.info("Saved entry: ID={}, Account={}, Type={}, Amount={}", 
                    savedEntry.getId(), savedEntry.getAccount().getAccountName(), 
                    savedEntry.getTransactionType(), savedEntry.getNilai());
            
            saveDivisionSpecificData(savedEntry, request);
            savedEntries.add(savedEntry);
        }
        
        log.info("Successfully saved {} entries", savedEntries.size());
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
        
        newEntry.setTransactionType(request.getTransactionType());
        newEntry.setTargetAmount(request.getTargetAmount());
        newEntry.setRealisasiAmount(request.getRealisasiAmount());
        newEntry.setHppAmount(request.getHppAmount());
        newEntry.setPemakaianAmount(request.getPemakaianAmount());
        newEntry.setStokAkhir(request.getStokAkhir());
        
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
        
        try {
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
            
        } catch (Exception e) {
            log.error("Failed to save pemasaran performance: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal menyimpan data performance pemasaran: " + e.getMessage());
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
            
            KeuanganSaldo saved = keuanganSaldoRepository.save(saldo);
            log.info("Successfully saved keuangan saldo with ID: {}, Transaction type: {}, Amount: {}, Saldo akhir: {}", 
                    saved.getId(), request.getTransactionType(), entry.getNilai(), saved.getSaldoAkhir());
            
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
}