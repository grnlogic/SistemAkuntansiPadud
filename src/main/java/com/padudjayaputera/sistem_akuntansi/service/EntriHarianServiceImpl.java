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
    
    // ✅ TAMBAHAN BARU: Repository untuk tabel khusus divisi
    private final PemasaranPerformanceRepository pemasaranPerformanceRepository;
    private final ProduksiHppRepository produksiHppRepository;
    private final GudangStokRepository gudangStokRepository;
    private final KeuanganSaldoRepository keuanganSaldoRepository;

    @Override
    public List<EntriHarian> getAllEntries() {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Jika SUPER_ADMIN, bisa lihat semua entri
        if (loggedInUser.getRole() == UserRole.SUPER_ADMIN) {
            return entriHarianRepository.findAll();
        }
        
        // Jika ADMIN_DIVISI, hanya bisa lihat entri divisinya
        return entriHarianRepository.findByAccountDivisionId(loggedInUser.getDivision().getId());
    }

    @Override
    public List<EntriHarian> getEntriesByDate(LocalDate date) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Jika SUPER_ADMIN, bisa lihat semua entri untuk tanggal tersebut
        if (loggedInUser.getRole() == UserRole.SUPER_ADMIN) {
            return entriHarianRepository.findByTanggalLaporan(date);
        }
        
        // Jika ADMIN_DIVISI, hanya bisa lihat entri divisinya untuk tanggal tersebut
        return entriHarianRepository.findByTanggalLaporanAndAccountDivisionId(date, loggedInUser.getDivision().getId());
    }

    @Override
    public List<EntriHarian> getEntriesByDivision(Integer divisionId) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Jika ADMIN_DIVISI, pastikan dia hanya mengakses divisinya sendiri
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
        
        // Otorisasi: pastikan user hanya bisa melihat entri divisinya
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
        // Dapatkan user yang sedang login
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Ambil data Akun dari database
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Akun dengan ID " + request.getAccountId() + " tidak ditemukan."));

        // LOGIKA OTORISASI: Pastikan admin divisi hanya mengisi data untuk divisinya sendiri
        if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
            if (!account.getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                throw new AccessDeniedException("Anda tidak memiliki akses untuk mengisi data akun divisi lain.");
            }
        }

        // ✅ Buat entri baru dengan data khusus divisi
        EntriHarian newEntry = createEntriHarianFromRequest(request, account, loggedInUser);

        // ✅ Save entri utama
        EntriHarian savedEntry = entriHarianRepository.save(newEntry);
        
        // ✅ Save ke tabel khusus divisi jika diperlukan
        saveDivisionSpecificData(savedEntry, request);

        return savedEntry;
    }

    @Override
    @Transactional
    public List<EntriHarian> saveBatchEntries(List<EntriHarianRequest> requests) {
        log.info("=== SERVICE: saveBatchEntries START ===");
        
        // ✅ Validate input
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Request list tidak boleh kosong");
        }
        
        // Dapatkan user yang sedang login
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Logged in user: {} (ID: {})", loggedInUser.getUsername(), loggedInUser.getId());
        log.info("User role: {}", loggedInUser.getRole());
        log.info("User division: {}", loggedInUser.getDivision() != null ? loggedInUser.getDivision().getName() : "null");

        List<EntriHarian> savedEntries = new ArrayList<>();
        
        for (EntriHarianRequest request : requests) {
            log.info("Processing request: {}", request);
            
            // Validasi
            if (request.getAccountId() == null || request.getTanggal() == null || request.getNilai() == null) {
                throw new IllegalArgumentException("AccountId, tanggal, dan nilai tidak boleh null");
            }
            
            // Ambil data Akun
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Akun dengan ID " + request.getAccountId() + " tidak ditemukan."));

            // Otorisasi divisi
            if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
                if (!account.getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                    throw new AccessDeniedException("Anda tidak memiliki akses untuk mengisi data akun divisi lain.");
                }
            }

            // ✅ NEW: Check if entry already exists for this account and date
            Optional<EntriHarian> existingEntry = entriHarianRepository
                    .findByTanggalLaporanAndAccountId(request.getTanggal(), request.getAccountId());

            EntriHarian entryToSave;
            if (existingEntry.isPresent()) {
                // ✅ UPDATE existing entry
                log.info("Found existing entry for account {} on date {}, updating...", 
                        request.getAccountId(), request.getTanggal());
                
                entryToSave = existingEntry.get();
                
                // Update nilai - untuk divisi khusus, aggregate atau replace
                if (shouldAggregateValues(account.getDivision().getName(), request)) {
                    // Aggregate (tambahkan ke nilai existing)
                    entryToSave.setNilai(entryToSave.getNilai().add(request.getNilai()));
                    log.info("Aggregating nilai: {} + {} = {}", 
                            existingEntry.get().getNilai(), request.getNilai(), entryToSave.getNilai());
                } else {
                    // Replace (ganti dengan nilai baru)
                    entryToSave.setNilai(request.getNilai());
                    log.info("Replacing nilai: {} -> {}", existingEntry.get().getNilai(), request.getNilai());
                }
                
                // Update other fields
                entryToSave.setDescription(request.getDescription());
                updateSpecializedFields(entryToSave, request);
                
            } else {
                // ✅ CREATE new entry
                log.info("Creating new entry for account {} on date {}", 
                        request.getAccountId(), request.getTanggal());
                
                entryToSave = createEntriHarianFromRequest(request, account, loggedInUser);
            }

            // ✅ Save entry (update or insert)
            EntriHarian savedEntry = entriHarianRepository.save(entryToSave);
            
            // ✅ Save ke tabel khusus divisi jika diperlukan
            saveDivisionSpecificData(savedEntry, request);
            
            savedEntries.add(savedEntry);
        }
        
        log.info("Successfully saved {} entries", savedEntries.size());
        return savedEntries;
    }

    // ✅ NEW: Helper method to determine if values should be aggregated or replaced
    private boolean shouldAggregateValues(String divisionName, EntriHarianRequest request) {
        String name = divisionName.toLowerCase();
        
        // For keuangan with transaction type, aggregate different transaction types
        if (name.contains("keuangan") && request.getTransactionType() != null) {
            return true; // Allow multiple transactions per day
        }
        
        // For other divisions, replace existing values
        return false;
    }

    // ✅ NEW: Helper method to update specialized fields
    private void updateSpecializedFields(EntriHarian entry, EntriHarianRequest request) {
        // Update specialized fields based on request
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

    // ✅ Helper method untuk membuat EntriHarian dari request
    private EntriHarian createEntriHarianFromRequest(EntriHarianRequest request, Account account, User user) {
        EntriHarian newEntry = new EntriHarian();
        newEntry.setAccount(account);
        newEntry.setTanggalLaporan(request.getTanggal());
        newEntry.setNilai(request.getNilai());
        newEntry.setDescription(request.getDescription());
        newEntry.setUser(user);
        
        // ✅ Set data khusus divisi
        newEntry.setTransactionType(request.getTransactionType());
        newEntry.setTargetAmount(request.getTargetAmount());
        newEntry.setRealisasiAmount(request.getRealisasiAmount());
        newEntry.setHppAmount(request.getHppAmount());
        newEntry.setPemakaianAmount(request.getPemakaianAmount());
        newEntry.setStokAkhir(request.getStokAkhir());
        
        return newEntry;
    }

    // ✅ Helper method untuk save data ke tabel khusus divisi
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
            log.warn("Failed to save division specific data: {}", e.getMessage());
            // Don't throw exception, just log warning so main entry still saves
        }
    }

    // ✅ IMPLEMENTASI: Save data pemasaran performance
    private void savePemasaranPerformance(EntriHarian entry, EntriHarianRequest request) {
        log.info("Saving pemasaran performance data for entry ID: {}", entry.getId());
        
        try {
            PemasaranPerformance performance = new PemasaranPerformance();
            performance.setEntriHarian(entry);
            performance.setTargetAmount(request.getTargetAmount() != null ? request.getTargetAmount() : BigDecimal.ZERO);
            performance.setRealisasiAmount(request.getRealisasiAmount() != null ? request.getRealisasiAmount() : BigDecimal.ZERO);
            performance.setTanggalLaporan(entry.getTanggalLaporan());
            
            // ✅ Extract additional info from account or description
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

    // ✅ IMPLEMENTASI: Save data produksi HPP
    private void saveProduksiHpp(EntriHarian entry, EntriHarianRequest request) {
        log.info("Saving produksi HPP data for entry ID: {}", entry.getId());
        
        try {
            ProduksiHpp produksi = new ProduksiHpp();
            produksi.setEntriHarian(entry);
            produksi.setProdukName(extractProductNameFromAccount(entry.getAccount()));
            produksi.setJumlahProduksi(entry.getNilai()); // Nilai = jumlah produksi
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

    // ✅ IMPLEMENTASI: Save data gudang stok
    private void saveGudangStok(EntriHarian entry, EntriHarianRequest request) {
        log.info("Saving gudang stok data for entry ID: {}", entry.getId());
        
        try {
            GudangStok stok = new GudangStok();
            stok.setEntriHarian(entry);
            stok.setBahanBakuName(extractMaterialNameFromAccount(entry.getAccount()));
            stok.setTanggalUpdate(entry.getTanggalLaporan());
            stok.setPemakaianHariIni(request.getPemakaianAmount() != null ? request.getPemakaianAmount() : BigDecimal.ZERO);
            stok.setPicGudang(entry.getUser().getUsername());
            
            // ✅ Auto-set stok awal dari data sebelumnya
            String bahanBakuName = stok.getBahanBakuName();
            Optional<GudangStok> latestStok = gudangStokRepository.findLatestByBahanBakuName(bahanBakuName);
            if (latestStok.isPresent()) {
                stok.setStokAwal(latestStok.get().getStokAkhir());
            } else {
                // Jika tidak ada data sebelumnya, gunakan stokAkhir dari request atau nilai default
                stok.setStokAwal(request.getStokAkhir() != null ? request.getStokAkhir() : BigDecimal.valueOf(1000));
            }
            
            // ✅ Set stok minimum dan lokasi gudang
            stok.setStokMinimum(BigDecimal.valueOf(100)); // Default minimum
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

    // ✅ IMPLEMENTASI: Save data keuangan saldo
    private void saveKeuanganSaldo(EntriHarian entry, EntriHarianRequest request) {
        log.info("Saving keuangan saldo data for entry ID: {}", entry.getId());
        
        try {
            // ✅ Check if already exists for this account and date
            Optional<KeuanganSaldo> existing = keuanganSaldoRepository
                    .findByAccountIdAndTanggalTransaksi(entry.getAccount().getId(), entry.getTanggalLaporan());
            
            KeuanganSaldo saldo;
            if (existing.isPresent()) {
                saldo = existing.get();
                log.info("Updating existing keuangan saldo with ID: {}", saldo.getId());
            } else {
                saldo = new KeuanganSaldo();
                saldo.setEntriHarian(entry);
                saldo.setAccount(entry.getAccount());
                saldo.setTanggalTransaksi(entry.getTanggalLaporan());
                
                // ✅ Get saldo awal from previous day
                Optional<KeuanganSaldo> latestSaldo = keuanganSaldoRepository
                        .findLatestByAccountId(entry.getAccount().getId());
                if (latestSaldo.isPresent()) {
                    saldo.setSaldoAwal(latestSaldo.get().getSaldoAkhir());
                } else {
                    saldo.setSaldoAwal(BigDecimal.ZERO);
                }
            }
            
            // ✅ Set penerimaan atau pengeluaran based on transaction type
            if (request.getTransactionType() != null) {
                switch (request.getTransactionType()) {
                    case PENERIMAAN:
                        saldo.setPenerimaan(entry.getNilai());
                        saldo.setPengeluaran(BigDecimal.ZERO);
                        break;
                    case PENGELUARAN:
                        saldo.setPenerimaan(BigDecimal.ZERO);
                        saldo.setPengeluaran(entry.getNilai());
                        break;
                }
            }
            
            saldo.setKeterangan(request.getDescription());
            
            KeuanganSaldo saved = keuanganSaldoRepository.save(saldo);
            log.info("Successfully saved keuangan saldo with ID: {}, Status: {}, Saldo akhir: {}", 
                    saved.getId(), saved.getCashStatus(), saved.getSaldoAkhir());
            
        } catch (Exception e) {
            log.error("Failed to save keuangan saldo: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal menyimpan data saldo keuangan: " + e.getMessage());
        }
    }

    // ✅ Helper methods untuk extract information
    private String extractSalesPersonFromDescription(String description) {
        if (description != null && description.toLowerCase().contains("sales")) {
            // Try to extract sales person name from description
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
        // Extract product name from account name
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
        return ProduksiHpp.ShiftKerja.PAGI; // Default
    }
    
    private String extractMaterialNameFromAccount(Account account) {
        // Extract material name from account name
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

        // Otorisasi: Pastikan user hanya mengubah entri milik divisinya
        if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
            if (!existingEntry.getAccount().getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                throw new AccessDeniedException("Anda tidak diizinkan mengubah entri harian di luar divisi Anda.");
            }
        }

        // ✅ Update dengan data baru termasuk data khusus divisi
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

        // Otorisasi sebelum hapus
        if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
            if (!existingEntry.getAccount().getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                throw new AccessDeniedException("Anda tidak diizinkan menghapus entri harian di luar divisi Anda.");
            }
        }

        entriHarianRepository.deleteById(id);
    }
}