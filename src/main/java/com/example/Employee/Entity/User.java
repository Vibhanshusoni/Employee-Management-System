package com.example.Employee.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String role;
    private String email;
    private String department;

    @OneToOne(mappedBy = "user")
    private Employee employee;

    @Column(nullable = false)
    private int failedAttempts = 0;

    @Column(nullable = false)
    private Boolean blocked = false;

    @Enumerated(EnumType.STRING)
    private BlockType blockType; // TEMPORARY or PERMANENT

    @Column(nullable = false)
    private boolean enabled = true;// Permanent block

    private LocalDateTime lockoutTime; // for temporary block

    public boolean isBlocked() {
        return blocked;
    }
}