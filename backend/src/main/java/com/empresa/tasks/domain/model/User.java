package com.empresa.tasks.domain.model;

public class User {

    private Long id;
    private String email;
    private String passwordHash;
    private String name;

    public User() {}

    public User(Long id, String email, String passwordHash, String name) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}