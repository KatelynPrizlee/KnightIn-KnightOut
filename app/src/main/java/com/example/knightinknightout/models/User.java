package com.example.knightinknightout.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Firestore data model representing a user document in the {@code users} collection.
 * Passwords are managed exclusively by Firebase Authentication and are never stored here.
 */
public class User {

    private String uid;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String major;
    private String year;
    private List<String> interests;
    private String socialStyle;
    private String profilePicture;
    private boolean ucfVerified;
    private int hostedCount;
    private Timestamp createdAt;

    /**
     * Required empty constructor for Cloud Firestore automatic deserialization.
     */
    public User() {
        this.interests = new ArrayList<>();
    }

    /**
     * Creates a new user profile with initial registration fields and sensible defaults.
     */
    public User(String uid,
                String firstName,
                String lastName,
                String username,
                String email,
                Timestamp createdAt) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.major = "";
        this.year = "";
        this.interests = new ArrayList<>();
        this.socialStyle = "";
        this.profilePicture = "";
        this.ucfVerified = false;
        this.hostedCount = 0;
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public List<String> getInterests() {
        if (interests == null) {
            interests = new ArrayList<>();
        }
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests != null ? interests : new ArrayList<>();
    }

    public String getSocialStyle() {
        return socialStyle;
    }

    public void setSocialStyle(String socialStyle) {
        this.socialStyle = socialStyle;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean isUcfVerified() {
        return ucfVerified;
    }

    public void setUcfVerified(boolean ucfVerified) {
        this.ucfVerified = ucfVerified;
    }

    public int getHostedCount() {
        return hostedCount;
    }

    public void setHostedCount(int hostedCount) {
        this.hostedCount = hostedCount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Exclude
    public String getFullName() {
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";
        if (first.isEmpty()) {
            return last;
        }
        if (last.isEmpty()) {
            return first;
        }
        return first + " " + last;
    }
}
