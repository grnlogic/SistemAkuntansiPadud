package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.padudjayaputera.sistem_akuntansi.dto.PiutangRequest;
import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.model.PiutangTransaksi;
import com.padudjayaputera.sistem_akuntansi.model.User;
import com.padudjayaputera.sistem_akuntansi.repository.AccountRepository;
import com.padudjayaputera.sistem_akuntansi.repository.PiutangTransaksiRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PiutangServiceImpl implements PiutangService {

    private final PiutangTransaksiRepository piutangRepository;
    private final AccountRepository accountRepository; // <-- 1. INJECT REPOSITORY BARU

    @Override
    public PiutangTransaksi createPiutang(PiutangRequest request) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2. Cari Akun COA berdasarkan ID dari request
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Akun COA dengan ID " + request.getAccountId() + " tidak ditemukan."));

        // Otorisasi: Pastikan akun yang dipilih adalah akun piutang yang benar (jika perlu)
        // Contoh: if (!account.getAccountName().toLowerCase().contains("piutang")) { ... }

        PiutangTransaksi newPiutang = new PiutangTransaksi();

        // 3. Set Akun COA yang sudah ditemukan ke transaksi piutang
        newPiutang.setAccount(account);
        
        newPiutang.setTanggalTransaksi(request.getTanggalTransaksi());
        newPiutang.setTipeTransaksi(request.getTipeTransaksi());
        newPiutang.setKategori(request.getKategori());
        newPiutang.setNominal(request.getNominal());
        newPiutang.setKeterangan(request.getKeterangan());
        newPiutang.setUser(loggedInUser);

        return piutangRepository.save(newPiutang);
    }

    @Override
    public List<PiutangTransaksi> getAllPiutang() {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (loggedInUser.getRole().name().equals("SUPER_ADMIN")) {
            return piutangRepository.findAll();
        } else {
            return piutangRepository.findByUserId(loggedInUser.getId());
        }
    }


    @Override
    public PiutangTransaksi updatePiutang(Integer id, PiutangRequest request) {
        PiutangTransaksi existingPiutang = piutangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi Piutang dengan ID " + id + " tidak ditemukan."));
        
        // 4. Update juga relasi ke Akun jika ada perubahan
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Akun COA dengan ID " + request.getAccountId() + " tidak ditemukan."));

        existingPiutang.setAccount(account);
        existingPiutang.setTanggalTransaksi(request.getTanggalTransaksi());
        existingPiutang.setTipeTransaksi(request.getTipeTransaksi());
        existingPiutang.setKategori(request.getKategori());
        existingPiutang.setNominal(request.getNominal());
        existingPiutang.setKeterangan(request.getKeterangan());

        return piutangRepository.save(existingPiutang);
    }

    @Override
    public void deletePiutang(Integer id) {
        if (!piutangRepository.existsById(id)) {
            throw new RuntimeException("Transaksi Piutang dengan ID " + id + " tidak ditemukan.");
        }
        piutangRepository.deleteById(id);
    }
    
}