package com.mihalypapp.app.models;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private String role;
    private int imageResource;

    public User() {

    }

    public User(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(int id, int imageResource, String name, String email) {
        this.id = id;
        this.imageResource = imageResource;
        this.name = name;
        this.email = email;
    }

    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getImageResource() {
        return imageResource;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}
