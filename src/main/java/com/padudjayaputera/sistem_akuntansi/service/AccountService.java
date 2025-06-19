package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;

import com.padudjayaputera.sistem_akuntansi.model.Account;

public interface AccountService {

    /**
     * Membuat akun baru dan menyimpannya ke database.
     * @param account data akun yang akan dibuat.
     * @return Akun yang telah disimpan.
     */
    Account createAccount(Account account);

    /**
     * Mendapatkan semua akun (untuk SUPER_ADMIN).
     * @return Daftar semua akun.
     */
    List<Account> getAllAccounts();

    /**
     * Mendapatkan semua akun yang termasuk dalam satu divisi.
     * @param divisionId ID dari divisi.
     * @return Daftar akun.
     */
    List<Account> getAccountsByDivisionId(Integer divisionId);

    /**
     * Mencari satu akun berdasarkan kode uniknya.
     * @param accountCode kode akun.
     * @return Akun yang ditemukan, atau null jika tidak ada.
     */
    Account getAccountByCode(String accountCode);

    /**
     * Update akun existing.
     * @param id ID akun.
     * @param accountDetails detail akun yang akan diupdate.
     * @return Akun yang telah diupdate.
     */
    Account updateAccount(Integer id, Account accountDetails);

    /**
     * Hapus akun.
     * @param id ID akun yang akan dihapus.
     */
    void deleteAccount(Integer id);
}