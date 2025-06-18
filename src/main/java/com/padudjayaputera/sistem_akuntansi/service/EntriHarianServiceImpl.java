package com.padudjayaputera.sistem_akuntansi.service;

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

@Service
@RequiredArgsConstructor
public class EntriHarianServiceImpl implements EntriHarianService {

    private final EntriHarianRepository entriHarianRepository;
    private final AccountRepository accountRepository;

    @Override
    public List<EntriHarian> saveBatchEntries(List<EntriHarianRequest> requests) {
        // Dapatkan user yang sedang login
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<EntriHarian> entriesToSave = new ArrayList<>();

        for (EntriHarianRequest req : requests) {
            // Ambil data Akun dari database
            Account account = accountRepository.findById(req.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Akun dengan ID " + req.getAccountId() + " tidak ditemukan."));

            // LOGIKA OTORISASI: Pastikan admin divisi hanya mengisi data untuk divisinya sendiri
            if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
                if (!account.getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                    throw new AccessDeniedException("Anda tidak diizinkan mencatat entri untuk akun di luar divisi Anda.");
                }
            }
            // Jika SUPER_ADMIN, dia bisa mengisi untuk divisi manapun

            EntriHarian newEntry = new EntriHarian();
            newEntry.setAccount(account);
            newEntry.setTanggalLaporan(req.getTanggal());
            newEntry.setNilai(req.getNilai());
            newEntry.setUser(loggedInUser); // Catat siapa yang membuat entri

            entriesToSave.add(newEntry);
        }

        // Simpan semua entri sekaligus ke database untuk efisiensi
        return entriHarianRepository.saveAll(entriesToSave);
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

        // Update nilainya
        existingEntry.setNilai(request.getNilai());
        // Tanggal dan akun biasanya tidak diubah, tapi bisa ditambahkan jika perlu

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