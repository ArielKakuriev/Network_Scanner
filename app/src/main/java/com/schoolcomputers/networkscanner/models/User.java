package com.schoolcomputers.networkscanner.models;

/**
 * Local representation of an authenticated Firebase user.
 * This is a plain data class — NOT a Room entity.
 * The actual data lives in Firebase Auth + Firestore.
 */
public class User {

    private String uid;
    private String username;
    private String email;

    public User() {}

    public User(String uid, String username, String email) {
        this.uid      = uid;
        this.username = username;
        this.email    = email;
    }

    public String getUid()                      { return uid; }
    public void   setUid(String uid)            { this.uid = uid; }

    public String getUsername()                 { return username; }
    public void   setUsername(String username)  { this.username = username; }

    public String getEmail()                    { return email; }
    public void   setEmail(String email)        { this.email = email; }

    @Override
    public String toString() {
        return "User{uid=" + uid + ", username=" + username + ", email=" + email + "}";
    }
}
