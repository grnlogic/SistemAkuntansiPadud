package com.padudjayaputera.sistem_akuntansi.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.padudjayaputera.sistem_akuntansi.dto.EntriHarianRequest;
import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.model.EntriHarian;
import com.padudjayaputera.sistem_akuntansi.model.User;
import com.padudjayaputera.sistem_akuntansi.model.UserRole;
import com.padudjayaputera.sistem_akuntansi.repository.AccountRepository;
import com.padudjayaputera.sistem_akuntansi.repository.EntriHarianRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntriHarianServiceImpl implements EntriHarianService {

    private final EntriHarianRepository entriHarianRepository;
    private final AccountRepository accountRepository;

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
    public EntriHarian saveEntry(EntriHarianRequest request) {
        // Dapatkan user yang sedang login
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Ambil data Akun dari database
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Akun dengan ID " + request.getAccountId() + " tidak ditemukan."));

        // LOGIKA OTORISASI: Pastikan admin divisi hanya mengisi data untuk divisinya sendiri
        if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
            if (!account.getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                throw new AccessDeniedException("Anda tidak diizinkan mencatat entri untuk akun di luar divisi Anda.");
            }
        }

        EntriHarian newEntry = new EntriHarian();
        newEntry.setAccount(account);
        newEntry.setTanggalLaporan(request.getTanggal());
        newEntry.setNilai(request.getNilai());
        newEntry.setDescription(request.getDescription()); // ✅ Set description
        newEntry.setUser(loggedInUser); // Catat siapa yang membuat entri

        return entriHarianRepository.save(newEntry);
    }

    @Override
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

        List<EntriHarian> entriesToSave = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            EntriHarianRequest req = requests.get(i);
            log.info("Processing request[{}]: {}", i, req);
            
            try {
                // ✅ Validate each request
                if (req.getAccountId() == null) {
                    throw new IllegalArgumentException("Request[" + i + "]: Account ID tidak boleh null");
                }
                if (req.getTanggal() == null) {
                    throw new IllegalArgumentException("Request[" + i + "]: Tanggal tidak boleh null");
                }
                if (req.getNilai() == null || req.getNilai().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Request[" + i + "]: Nilai harus positif");
                }
                
                // Ambil data Akun dari database
                log.info("Looking for account with ID: {}", req.getAccountId());
                Account account = accountRepository.findById(req.getAccountId())
                        .orElseThrow(() -> new RuntimeException("Akun dengan ID " + req.getAccountId() + " tidak ditemukan."));
                
                log.info("Found account: {} (Division: {})", account.getAccountName(), account.getDivision().getName());

                // LOGIKA OTORISASI: Pastikan admin divisi hanya mengisi data untuk divisinya sendiri
                if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
                    log.info("Checking division authorization...");
                    log.info("Account division ID: {}", account.getDivision().getId());
                    log.info("User division ID: {}", loggedInUser.getDivision().getId());
                    
                    if (!account.getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                        throw new AccessDeniedException("Anda tidak diizinkan mencatat entri untuk akun di luar divisi Anda.");
                    }
                }

                log.info("Creating new EntriHarian...");
                EntriHarian newEntry = new EntriHarian();
                newEntry.setAccount(account);
                newEntry.setTanggalLaporan(req.getTanggal());
                newEntry.setNilai(req.getNilai());
                newEntry.setDescription(req.getDescription());
                newEntry.setUser(loggedInUser);

                log.info("Created entry: accountId={}, tanggal={}, nilai={}", 
                        newEntry.getAccount().getId(), 
                        newEntry.getTanggalLaporan(), 
                        newEntry.getNilai());
                entriesToSave.add(newEntry);
                
            } catch (Exception e) {
                log.error("Error processing entry[{}]: {}", i, e.getMessage());
                throw e;
            }
        }

        log.info("Saving {} entries to database...", entriesToSave.size());
        
        try {
            List<EntriHarian> savedEntries = entriHarianRepository.saveAll(entriesToSave);
            log.info("Successfully saved all entries");
            return savedEntries;
        } catch (Exception e) {
            log.error("Error saving to database: {}", e.getMessage(), e);
            throw e;
        }
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

        // Update nilainya dan description
        existingEntry.setNilai(request.getNilai());
        existingEntry.setDescription(request.getDescription()); // ✅ Update description

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