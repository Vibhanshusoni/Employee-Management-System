package com.example.Employee.Service;

import com.example.Employee.Entity.TokenBlacklist;
import com.example.Employee.Repository.TokenBlacklistRepository;
import com.example.Employee.config.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@RequiredArgsConstructor
@Service
public class TokenBlacklistService {
    private static final Logger logger =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final Set<String> blacklist = new HashSet<>();

    public void add(String token) {
        blacklist.add(token);
    }

    public boolean contains(String token) {
        return blacklist.contains(token);
    }

    private final TokenBlacklistRepository repo;

    public void blacklist(String token) {

        TokenBlacklist blacklist = new TokenBlacklist();

        blacklist.setToken(token);
        blacklist.setBlacklistedAt(LocalDateTime.now());

        repo.save(blacklist);

        logger.info("Token added to blacklist");
    }

    public boolean isBlacklisted(String token) {

        return repo.existsByToken(token);
    }
}