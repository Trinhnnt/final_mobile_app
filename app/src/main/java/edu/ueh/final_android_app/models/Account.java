package edu.ueh.final_android_app.models;

import com.google.firebase.firestore.PropertyName;

public class Account {
    private String id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private String avatarUrl;
    @PropertyName("isBanned")
    private boolean isBanned = false;

    public Account() {
    }

    public Account(String id, String firstName, String lastName, String username, String password, String avatarUrl, boolean isBanned) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.avatarUrl = avatarUrl;
        this.isBanned = isBanned;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setIsBanned(boolean isBanned) {
        this.isBanned = isBanned;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @PropertyName("isBanned")
    public boolean isBanned() {
        return isBanned;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
