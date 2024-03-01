package com.example.demo.Repository;
import com.example.demo.Entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRepository extends JpaRepository<Otp, Integer>{
}