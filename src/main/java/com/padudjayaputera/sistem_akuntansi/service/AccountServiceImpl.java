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

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(Account account) {
        try {
            // Log untuk debugging
            System.out.println("=== SERVICE DEBUG: Creating account ===");
            System.out.println("Account data: " + account);
            
            // Validasi input
            if (account.getAccountCode() == null || account.getAccountCode().trim().isEmpty()) {
                throw new IllegalArgumentException("Account code is required");
            }
            if (account.getAccountName() == null || account.getAccountName().trim().isEmpty()) {
                throw new IllegalArgumentException("Account name is required");
            }
            if (account.getValueType() == null) {
                throw new IllegalArgumentException("Value type is required");
            }
            if (account.getDivision() == null || account.getDivision().getId() == null) {
                throw new IllegalArgumentException("Division is required");
            }

            // Cek duplikasi kode akun
            Optional<Account> existingAccount = accountRepository.findByAccountCode(account.getAccountCode().trim());
            if (existingAccount.isPresent()) {
                throw new IllegalArgumentException("Account code already exists: " + account.getAccountCode());
            }

            // Simpan ke database
            Account savedAccount = accountRepository.save(account);
            System.out.println("=== SERVICE DEBUG: Account saved successfully ===");
            System.out.println("Saved account: " + savedAccount);
            
            return savedAccount;
        } catch (Exception e) {
            System.err.println("=== SERVICE ERROR: Error creating account ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<Account> getAllAccounts() {
        try {
            System.out.println("=== SERVICE DEBUG: Getting all accounts ===");
            
            // Cek authorization - hanya SUPER_ADMIN yang bisa akses semua akun
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User currentUser = (User) authentication.getPrincipal();
                System.out.println("Current user: " + currentUser.getUsername() + ", Role: " + currentUser.getRole());
                
                if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
                    throw new AccessDeniedException("Only SUPER_ADMIN can access all accounts");
                }
            }
            
            List<Account> accounts = accountRepository.findAll();
            System.out.println("=== SERVICE DEBUG: Found " + accounts.size() + " accounts ===");
            
            return accounts;
        } catch (Exception e) {
            System.err.println("=== SERVICE ERROR: Error getting all accounts ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<Account> getAccountsByDivisionId(Integer divisionId) {
        try {
            System.out.println("=== SERVICE DEBUG: Getting accounts for division: " + divisionId + " ===");
            
            List<Account> accounts = accountRepository.findByDivisionId(divisionId);
            System.out.println("=== SERVICE DEBUG: Found " + accounts.size() + " accounts for division " + divisionId + " ===");
            
            return accounts;
        } catch (Exception e) {
            System.err.println("=== SERVICE ERROR: Error getting accounts by division ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Account getAccountByCode(String accountCode) {
        try {
            System.out.println("=== SERVICE DEBUG: Getting account by code: " + accountCode + " ===");
            
            Optional<Account> account = accountRepository.findByAccountCode(accountCode);
            if (account.isPresent()) {
                System.out.println("=== SERVICE DEBUG: Account found ===");
                return account.get();
            } else {
                System.out.println("=== SERVICE DEBUG: Account not found ===");
                return null;
            }
        } catch (Exception e) {
            System.err.println("=== SERVICE ERROR: Error getting account by code ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Account updateAccount(Integer id, Account accountDetails) {
        try {
            System.out.println("=== SERVICE DEBUG: Updating account with ID: " + id + " ===");
            
            Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

            // Update fields
            existingAccount.setAccountCode(accountDetails.getAccountCode());
            existingAccount.setAccountName(accountDetails.getAccountName());
            existingAccount.setValueType(accountDetails.getValueType());
            existingAccount.setDivision(accountDetails.getDivision());

            Account updatedAccount = accountRepository.save(existingAccount);
            System.out.println("=== SERVICE DEBUG: Account updated successfully ===");
            
            return updatedAccount;
        } catch (Exception e) {
            System.err.println("=== SERVICE ERROR: Error updating account ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void deleteAccount(Integer id) {
        try {
            System.out.println("=== SERVICE DEBUG: Deleting account with ID: " + id + " ===");
            
            Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
            
            accountRepository.delete(account);
            System.out.println("=== SERVICE DEBUG: Account deleted successfully ===");
        } catch (Exception e) {
            System.err.println("=== SERVICE ERROR: Error deleting account ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}