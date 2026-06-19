package com.mycompany.tokoberkahjayaa.model;

public class User {
    private int idUser;
    private String username;
    private String password;
    private String namaLengkap;
    private int level;

    public User() {}

    public User(int idUser, String username, String password, String namaLengkap, int level) {
        this.idUser = idUser;
        this.username = username;
        this.password = password;
        this.namaLengkap = namaLengkap;
        this.level = level;
    }

    // Getters & Setters
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNamaLengkap() { return namaLengkap; }
    public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}
