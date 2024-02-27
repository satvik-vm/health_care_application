package com.example.demo.services;

import com.example.demo.Entity.Admin;
import com.example.demo.Entity.Supervisor;
import com.example.demo.Entity.User;
import com.example.demo.Repository.AdminRepository;
import com.example.demo.Repository.SupervisorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    SupervisorRepository supervisorRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public List<Admin> getAdmin()
    {
        return adminRepository.findAll();
    }

    public Admin createAdmin(Admin admin)
    {
        return adminRepository.save(admin);
    }

    public Supervisor createSupervisor(Supervisor supervisor)
    {
        return supervisorRepository.save(supervisor);
    }
}
