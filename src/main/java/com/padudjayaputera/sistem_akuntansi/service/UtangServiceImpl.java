package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.padudjayaputera.sistem_akuntansi.dto.UtangRequest;
import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.model.User;
import com.padudjayaputera.sistem_akuntansi.model.UtangTransaksi;
import com.padudjayaputera.sistem_akuntansi.repository.AccountRepository;
import com.padudjayaputera.sistem_akuntansi.repository.UtangTransaksiRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UtangServiceImpl implements UtangService {

    private final UtangTransaksiRepository utangRepository;
    private final AccountRepository accountRepository;

    @Override
    public UtangTransaksi createUtang(UtangRequest request) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Akun COA dengan ID " + request.getAccountId() + " tidak ditemukan."));

        UtangTransaksi newUtang = new UtangTransaksi();
        newUtang.setAccount(account);
        newUtang.setTanggalTransaksi(request.getTanggalTransaksi());
        newUtang.setTipeTransaksi(request.getTipeTransaksi());
        newUtang.setKategori(request.getKategori());
        newUtang.setNominal(request.getNominal());
        newUtang.setKeterangan(request.getKeterangan());
        newUtang.setUser(loggedInUser);

        return utangRepository.save(newUtang);
    }

    @Override
    public List<UtangTransaksi> getAllUtang() {
        return utangRepository.findAll();
    }

    @Override
    public UtangTransaksi updateUtang(Integer id, UtangRequest request) {
        UtangTransaksi existingUtang = utangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi Utang dengan ID " + id + " tidak ditemukan."));

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Akun COA dengan ID " + request.getAccountId() + " tidak ditemukan."));

        existingUtang.setAccount(account);
        existingUtang.setTanggalTransaksi(request.getTanggalTransaksi());
        existingUtang.setTipeTransaksi(request.getTipeTransaksi());
        existingUtang.setKategori(request.getKategori());
        existingUtang.setNominal(request.getNominal());
        existingUtang.setKeterangan(request.getKeterangan());

        return utangRepository.save(existingUtang);
    }

    @Override
    public void deleteUtang(Integer id) {
        if (!utangRepository.existsById(id)) {
            throw new RuntimeException("Transaksi Utang dengan ID " + id + " tidak ditemukan.");
        }
        utangRepository.deleteById(id);
    }
}