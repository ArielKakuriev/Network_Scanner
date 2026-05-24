package com.networkscanner.models;

/**
 * Local representation of the authenticated user.
 * Actual credentials are stored in Firebase Auth; this is a transport/display object.
 */
public class User {

    private String uid;
    private String username;
    private String email;

    public User() {}

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
    }

    // ---- Getters & Setters ----

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "User{uid=" + uid + ", username=" + username + ", email=" + email + "}";
    }
}
