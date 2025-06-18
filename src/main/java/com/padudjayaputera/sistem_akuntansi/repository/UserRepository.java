package com.padudjayaputera.sistem_akuntansi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Spring Data JPA akan otomatis membuat query untuk mencari user berdasarkan username
    Optional<User> findByUsername(String username);
}