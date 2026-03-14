package com.example.Employee.Repository;


import com.example.Employee.Entity.TokenBlacklist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    boolean existsByToken(String token);

}
