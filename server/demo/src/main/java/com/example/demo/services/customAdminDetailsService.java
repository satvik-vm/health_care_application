package com.example.demo.services;

import com.example.demo.Entity.Admin;
import com.example.demo.Repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class customAdminDetailsService implements UserDetailsService {

    @Autowired
    AdminRepository repository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        Load user by username from database
        return repository.findByEmail(username).orElseThrow(() -> new RuntimeException("User Not Found"));
    }
}
