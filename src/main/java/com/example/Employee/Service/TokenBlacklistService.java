package com.example.Employee.Service;

public interface TokenBlacklistService {
    public void add(String token);

    boolean contains(String token);

    void blacklist(String token);

    boolean isBlacklisted(String token);
}