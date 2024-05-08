package com.example.demo.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class InMemoryTokenBlacklist implements TokenBlacklist {
    private Set<String> blacklist = new HashSet<>();

    @Override
    public void addToBlacklist(String token) {
        blacklist.add(token);
    }

    @Scheduled(fixedRate = 5*60*60*1000)
    public void clearBlacklist() {
        blacklist.clear();
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
