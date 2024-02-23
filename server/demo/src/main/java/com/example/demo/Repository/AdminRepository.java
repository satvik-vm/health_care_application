package com.example.demo.Repository;

import com.example.demo.Entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    public Optional<Admin> findByEmail(String email);
}
