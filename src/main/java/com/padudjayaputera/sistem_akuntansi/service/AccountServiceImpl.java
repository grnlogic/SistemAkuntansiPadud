package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.model.User;
import com.padudjayaputera.sistem_akuntansi.model.UserRole;
import com.padudjayaputera.sistem_akuntansi.repository.AccountRepository;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    // Menggunakan Constructor Injection untuk memasukkan AccountRepository
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(Account account) {
        // Dapatkan user yang sedang login
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // LOGIKA OTORISASI BARU
        // Jika yang login adalah ADMIN_DIVISI
        if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
            // Dia hanya boleh membuat akun untuk divisinya sendiri.
            // Cek apakah ID divisi di data akun yang mau dibuat sama dengan ID divisi miliknya.
            if (!account.getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                throw new AccessDeniedException("Admin Divisi hanya boleh membuat akun untuk divisinya sendiri.");
            }
        }
        // Jika yang login adalah SUPER_ADMIN, dia bisa membuat akun untuk divisi mana pun, jadi tidak perlu ada pengecekan.

        // Logika pengecekan kode akun duplikat (tetap ada)
        Optional<Account> existingAccount = accountRepository.findByAccountCode(account.getAccountCode());
        if (existingAccount.isPresent()) {
            throw new IllegalStateException("Akun dengan kode " + account.getAccountCode() + " sudah ada.");
        }
        // Jika tidak ada, simpan akun baru
        return accountRepository.save(account);
    }

    @Override
    public List<Account> getAccountsByDivisionId(Integer divisionId) {
        // 1. Dapatkan informasi user yang sedang login
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new AccessDeniedException("User tidak terautentikasi dengan benar.");
        }
        
        User loggedInUser = (User) authentication.getPrincipal();

        // 2. Terapkan logika otorisasi
        // Jika rolenya ADMIN_DIVISI
        if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
            // Periksa apakah ID divisi yang diminta sama dengan ID divisi miliknya
            if (loggedInUser.getDivision() == null) {
                throw new AccessDeniedException("User tidak memiliki divisi yang terdaftar.");
            }
            
            if (!loggedInUser.getDivision().getId().equals(divisionId)) {
                // Jika tidak sama, tolak akses!
                throw new AccessDeniedException("Anda tidak memiliki akses ke data divisi ini.");
            }
        }
        // Jika rolenya SUPER_ADMIN, tidak perlu pengecekan, dia boleh lanjut

        // 3. Jika lolos pengecekan, baru panggil repository
        return accountRepository.findByDivisionId(divisionId);
    }

    @Override
    public Account getAccountByCode(String accountCode) {
        // Cari berdasarkan kode, jika tidak ada kembalikan null (atau throw exception)
        return accountRepository.findByAccountCode(accountCode).orElse(null);
    }

    @Override
    public Account updateAccount(Integer id, Account accountDetails) {
        // Ambil akun yang ada dari DB
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Akun dengan ID " + id + " tidak ditemukan."));

        // Dapatkan user yang sedang login untuk otorisasi
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Otorisasi: Hanya SUPER_ADMIN atau ADMIN_DIVISI pemilik akun yang boleh mengubah
        if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
            if (!existingAccount.getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                throw new AccessDeniedException("Anda tidak diizinkan mengubah akun di luar divisi Anda.");
            }
        }

        // Update data
        existingAccount.setAccountName(accountDetails.getAccountName());
        existingAccount.setAccountCode(accountDetails.getAccountCode());
        existingAccount.setValueType(accountDetails.getValueType());
        // Divisi tidak diubah untuk menjaga integritas, jika perlu bisa ditambahkan

        return accountRepository.save(existingAccount);
    }

    @Override
    public void deleteAccount(Integer id) {
        // Ambil akun yang ada dari DB untuk otorisasi sebelum dihapus
         Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Akun dengan ID " + id + " tidak ditemukan."));

        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Otorisasi: Sama seperti update
        if (loggedInUser.getRole() == UserRole.ADMIN_DIVISI) {
            if (!existingAccount.getDivision().getId().equals(loggedInUser.getDivision().getId())) {
                throw new AccessDeniedException("Anda tidak diizinkan menghapus akun di luar divisi Anda.");
            }
        }

        accountRepository.deleteById(id);
    }
}