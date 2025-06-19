package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;
import com.padudjayaputera.sistem_akuntansi.model.User;

public interface UserService {
    User findByUsername(String username);
    User save(User user);
    
    // Tambahkan method baru untuk CRUD operations
    List<User> getAllUsers();
    User getUserById(Integer id);
    User createUser(User user);
    User updateUser(Integer id, User userDetails);
    void deleteUser(Integer id);
}