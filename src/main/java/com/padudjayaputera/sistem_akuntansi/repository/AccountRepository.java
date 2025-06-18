package com.padudjayaputera.sistem_akuntansi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    // Mencari akun berdasarkan kode akun yang unik
    Optional<Account> findByAccountCode(String accountCode);

    // Mencari semua akun yang dimiliki oleh satu divisi tertentu
    List<Account> findByDivisionId(Integer divisionId);
}