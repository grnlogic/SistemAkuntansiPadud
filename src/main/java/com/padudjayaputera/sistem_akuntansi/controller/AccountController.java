package com.padudjayaputera.sistem_akuntansi.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
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

import com.padudjayaputera.sistem_akuntansi.dto.AccountCreateRequest;
import com.padudjayaputera.sistem_akuntansi.dto.AccountResponse;
import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.model.Division;
import com.padudjayaputera.sistem_akuntansi.service.AccountService;
import com.padudjayaputera.sistem_akuntansi.service.DivisionService;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final DivisionService divisionService;

    public AccountController(AccountService accountService, DivisionService divisionService) {
        this.accountService = accountService;
        this.divisionService = divisionService;
    }

    /**
     * Endpoint untuk mendapatkan semua akun (SUPER_ADMIN only).
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        try {
            System.out.println("=== CONTROLLER DEBUG: GET /accounts called ===");
            
            List<Account> accounts = accountService.getAllAccounts();
            
            // Convert ke DTO menggunakan builder pattern
            List<AccountResponse> response = accounts.stream()
                .map(AccountResponse::fromAccount)
                .collect(Collectors.toList());
            
            System.out.println("=== CONTROLLER DEBUG: Returning " + response.size() + " accounts ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== CONTROLLER ERROR: Error getting all accounts ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint untuk mendapatkan akun berdasarkan divisi.
     */
    @GetMapping("/by-division/{divisionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AccountResponse>> getAccountsByDivision(@PathVariable Integer divisionId) {
        try {
            System.out.println("=== CONTROLLER DEBUG: GET /accounts/by-division/" + divisionId + " called ===");
            
            List<Account> accounts = accountService.getAccountsByDivisionId(divisionId);
            
            // Convert ke DTO
            List<AccountResponse> response = accounts.stream()
                .map(AccountResponse::fromAccount)
                .collect(Collectors.toList());
            
            System.out.println("=== CONTROLLER DEBUG: Returning " + response.size() + " accounts for division " + divisionId + " ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== CONTROLLER ERROR: Error getting accounts by division ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint untuk membuat akun baru.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountCreateRequest request) {
        try {
            System.out.println("=== CONTROLLER DEBUG: POST /accounts called ===");
            System.out.println("Request: " + request);
            System.out.println("Account code: '" + request.getAccountCode() + "'");
            System.out.println("Account name: '" + request.getAccountName() + "'");
            System.out.println("Value type: " + request.getValueType());
            System.out.println("Division: " + request.getDivision());
            
            // Validasi input
            if (request.getAccountCode() == null || request.getAccountCode().trim().isEmpty()) {
                System.err.println("Account code is missing");
                return ResponseEntity.badRequest().build();
            }
            if (request.getAccountName() == null || request.getAccountName().trim().isEmpty()) {
                System.err.println("Account name is missing");
                return ResponseEntity.badRequest().build();
            }
            if (request.getValueType() == null) {
                System.err.println("Value type is missing");
                return ResponseEntity.badRequest().build();
            }
            if (request.getDivision() == null || request.getDivision().getId() == null) {
                System.err.println("Division is missing");
                return ResponseEntity.badRequest().build();
            }
            
            // Convert DTO ke Entity
            Account account = new Account();
            account.setAccountCode(request.getAccountCode().trim());
            account.setAccountName(request.getAccountName().trim());
            account.setValueType(request.getValueType());
            
            // Get division from database
            Division division = divisionService.getDivisionById(request.getDivision().getId());
            if (division == null) {
                System.err.println("Division not found with ID: " + request.getDivision().getId());
                return ResponseEntity.badRequest().build();
            }
            account.setDivision(division);
            
            Account savedAccount = accountService.createAccount(account);
            
            // Convert response ke DTO
            AccountResponse response = AccountResponse.fromAccount(savedAccount);
            
            System.out.println("=== CONTROLLER DEBUG: Account created successfully ===");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            System.err.println("=== CONTROLLER ERROR: Validation error ===");
            System.err.println("Error message: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("=== CONTROLLER ERROR: Unexpected error ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint untuk update akun.
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable Integer id, @RequestBody AccountCreateRequest request) {
        try {
            System.out.println("=== CONTROLLER DEBUG: PUT /accounts/" + id + " called ===");
            
            // Convert DTO ke Entity
            Account accountDetails = new Account();
            accountDetails.setAccountCode(request.getAccountCode().trim());
            accountDetails.setAccountName(request.getAccountName().trim());
            accountDetails.setValueType(request.getValueType());
            
            // Get division from database
            Division division = divisionService.getDivisionById(request.getDivision().getId());
            if (division == null) {
                return ResponseEntity.badRequest().build();
            }
            accountDetails.setDivision(division);
            
            Account updatedAccount = accountService.updateAccount(id, accountDetails);
            
            // Convert response ke DTO
            AccountResponse response = AccountResponse.fromAccount(updatedAccount);
            
            System.out.println("=== CONTROLLER DEBUG: Account updated successfully ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== CONTROLLER ERROR: Error updating account ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint untuk delete akun.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteAccount(@PathVariable Integer id) {
        try {
            System.out.println("=== CONTROLLER DEBUG: DELETE /accounts/" + id + " called ===");
            
            accountService.deleteAccount(id);
            
            System.out.println("=== CONTROLLER DEBUG: Account deleted successfully ===");
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException e) {
            System.err.println("=== CONTROLLER ERROR: Foreign key constraint violation ===");
            
            // Return user-friendly error message
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "CONSTRAINT_VIOLATION");
            errorResponse.put("message", "Tidak dapat menghapus akun karena masih digunakan dalam entri harian");
            errorResponse.put("details", "Hapus terlebih dahulu semua entri harian yang menggunakan akun ini");
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            System.err.println("=== CONTROLLER ERROR: Error deleting account ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "INTERNAL_ERROR");
            errorResponse.put("message", "Terjadi kesalahan internal server");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}