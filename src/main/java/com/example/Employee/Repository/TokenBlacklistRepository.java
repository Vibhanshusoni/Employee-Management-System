package com.example.Employee.Repository;


import com.example.Employee.Entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    boolean existsByToken(String token);

}
