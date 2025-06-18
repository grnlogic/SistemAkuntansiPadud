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
     * Memperbarui data akun berdasarkan ID.
     * @param id ID dari akun yang akan diperbarui.
     * @param accountDetails data akun yang baru.
     * @return Akun yang telah diperbarui.
     */
    Account updateAccount(Integer id, Account accountDetails);

    /**
     * Menghapus akun berdasarkan ID.
     * @param id ID dari akun yang akan dihapus.
     */
    void deleteAccount(Integer id);
}