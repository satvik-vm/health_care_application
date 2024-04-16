package com.example.demo.security;


public interface TokenBlacklist {
    void addToBlacklist(String token);
    boolean isBlacklisted(String token);
}

