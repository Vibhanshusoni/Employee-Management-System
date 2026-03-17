package com.example.Employee.Service.ServiceImpl;

import com.example.Employee.Entity.TokenBlacklist;
import com.example.Employee.Repository.TokenBlacklistRepository;
import com.example.Employee.Service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@RequiredArgsConstructor
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistServiceImpl.class);

    private final TokenBlacklistRepository repo;

    private final Set<String> blacklist = new HashSet<>();

    public void add(String token) {
        blacklist.add(token);
    }

    public boolean contains(String token) {
        return blacklist.contains(token);
    }

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