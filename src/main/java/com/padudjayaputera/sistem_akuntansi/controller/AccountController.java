package com.padudjayaputera.sistem_akuntansi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.service.AccountService;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    // Suntikkan (Inject) AccountService yang sudah kita buat
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    
    /**
     * Endpoint untuk membuat akun baru.
     * Method: POST
     * URL: http://localhost:8080/api/v1/accounts
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()") // Ubah menjadi isAuthenticated()
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        Account createdAccount = accountService.createAccount(account);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    /**
     * Endpoint untuk mendapatkan semua akun berdasarkan ID Divisi.
     * Method: GET
     * URL: Contoh: http://localhost:8080/api/v1/accounts/by-division/3
     */
    @GetMapping("/by-division/{divisionId}")
    public ResponseEntity<List<Account>> getAccountsByDivisionId(@PathVariable Integer divisionId) {
        List<Account> accounts = accountService.getAccountsByDivisionId(divisionId);
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Account> updateAccount(@PathVariable Integer id, @RequestBody Account accountDetails) {
        Account updatedAccount = accountService.updateAccount(id, accountDetails);
        return ResponseEntity.ok(updatedAccount);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAccount(@PathVariable Integer id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build(); // Standard response untuk DELETE sukses
    }
}