package com.example.chatify.model;

public class Users {

    // Declare private instance variables (fields) for the user's data.
    private String userID;       // Unique ID for the user.
    private String username;     // The username of the user.
    private String userPhone;    // The phone number of the user.
    private String imageProfile; // URL or path to the user's profile image.
    private String imageCover;   // URL or path to the user's cover image.
    private String email;        // Email address of the user.
    private String dateOfBirth;  // The user's date of birth.
    private String gender;       // The user's gender (e.g., Male, Female).
    private String status;       // The current status message of the user (e.g., "Available", "Busy").
    private String bio;          // A short bio or description of the user.

    // Default constructor - Required for Firestore or other frameworks to create instances.
    public Users() {
    }

    // Parameterized constructor to initialize the user's fields with given values.
    public Users(String userID, String username,
                 String userPhone, String imageProfile,
                 String imageCover, String email,
                 String dateOfBirth, String gender,
                 String status, String bio) {
        this.userID = userID;            // Initialize userID with the provided userID value.
        this.username = username;        // Initialize username with the provided username value.
        this.userPhone = userPhone;      // Initialize userPhone with the provided phone number.
        this.imageProfile = imageProfile;// Initialize imageProfile with the URL or path to the profile image.
        this.imageCover = imageCover;    // Initialize imageCover with the URL or path to the cover image.
        this.email = email;              // Initialize email with the user's email address.
        this.dateOfBirth = dateOfBirth;  // Initialize dateOfBirth with the user's birth date.
        this.gender = gender;            // Initialize gender with the user's gender.
        this.status = status;            // Initialize status with the user's current status message.
        this.bio = bio;                  // Initialize bio with the user's bio or description.
    }

    // Getter and Setter methods for each field. These methods allow access and modification of the private fields.

    public String getUserID() {
        return userID; // Return the userID.
    }

    public void setUserID(String userID) {
        this.userID = userID; // Set the userID to the provided value.
    }

    public String getUsername() {
        return username; // Return the username.
    }

    public void setUsername(String username) {
        this.username = username; // Set the username to the provided value.
    }

    public String getUserPhone() {
        return userPhone; // Return the user's phone number.
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone; // Set the user's phone number to the provided value.
    }

    public String getImageProfile() {
        return imageProfile; // Return the URL or path to the profile image.
    }

    public void setImageProfile(String imageProfile) {
        this.imageProfile = imageProfile; // Set the URL or path to the profile image.
    }

    public String getImageCover() {
        return imageCover; // Return the URL or path to the cover image.
    }

    public void setImageCover(String imageCover) {
        this.imageCover = imageCover; // Set the URL or path to the cover image.
    }

    public String getEmail() {
        return email; // Return the user's email address.
    }

    public void setEmail(String email) {
        this.email = email; // Set the email address to the provided value.
    }

    public String getDateOfBirth() {
        return dateOfBirth; // Return the user's date of birth.
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth; // Set the date of birth to the provided value.
    }

    public String getGender() {
        return gender; // Return the user's gender.
    }

    public void setGender(String gender) {
        this.gender = gender; // Set the user's gender to the provided value.
    }

    public String getStatus() {
        return status; // Return the user's current status.
    }

    public void setStatus(String status) {
        this.status = status; // Set the user's current status to the provided value.
    }

    public String getBio() {
        return bio; // Return the user's bio.
    }

    public void setBio(String bio) {
        this.bio = bio; // Set the user's bio to the provided value.
    }
}
