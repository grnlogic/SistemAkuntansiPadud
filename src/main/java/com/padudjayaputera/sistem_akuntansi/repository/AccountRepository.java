package com.padudjayaputera.sistem_akuntansi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.Account;
import com.padudjayaputera.sistem_akuntansi.model.ValueType;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    // Mencari akun berdasarkan kode akun yang unik
    Optional<Account> findByAccountCode(String accountCode);

    // Mencari semua akun yang dimiliki oleh satu divisi tertentu
    List<Account> findByDivisionId(Integer divisionId);
    
    // Mencari akun berdasarkan valueType - untuk produk biasanya KUANTITAS
    List<Account> findByValueType(ValueType valueType);
    
    // Mencari akun berdasarkan divisi dan valueType
    List<Account> findByDivisionIdAndValueType(Integer divisionId, ValueType valueType);
}