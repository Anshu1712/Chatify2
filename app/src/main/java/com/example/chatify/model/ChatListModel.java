package com.example.chatify.model;

public class ChatListModel {
    private String userID;        // Stores the unique ID of the user involved in the chat.
    private String userName;      // Stores the name of the user.
    private String description;   // Stores the description or last message in the chat.
    private String date;          // Stores the date and time when the last message was sent/received.
    private String urlProfile;    // Stores the URL to the user's profile image.
    private String userPhone;// Stores the user's phone number.
    private String UserBio;
    private String seen;
    private String mCount;
    private String lastM;

    // Default constructor - an empty constructor required by certain frameworks like Firebase or when no parameters are passed during object creation.
    public ChatListModel() {
    }

    // Parameterized constructor - Allows creating an instance of ChatListModel by providing values for all fields.
    public ChatListModel(String userID, String userName, String description, String date, String urlProfile, String userPhone, String bio, String seen, String mCount, String lastM) {
        this.userID = userID;          // Initialize the userID with the provided userID.
        this.userName = userName;      // Initialize the userName with the provided userName.
        this.description = description; // Initialize the description with the provided description.
        this.date = date;              // Initialize the date with the provided date.
        this.urlProfile = urlProfile;  // Initialize the urlProfile with the provided URL of the profile image.
        this.userPhone = userPhone;    // Initialize the userPhone with the provided phone number.
        this.UserBio = bio;
        this.seen = seen;
        this.mCount = mCount;
        this.lastM = lastM;
    }


    // Getter method for userID - Retrieves the userID value.
    public String getUserID() {
        return userID;  // Returns the userID associated with the chat.
    }

    // Setter method for userID - Allows setting the userID value.
    public void setUserID(String userID) {
        this.userID = userID;  // Sets the userID field to the provided value.
    }


    // Getter method for userName - Retrieves the userName value.
    public String getUserName() {
        return userName;  // Returns the name of the user associated with the chat.
    }

    // Setter method for userName - Allows setting the userName value.
    public void setUserName(String userName) {
        this.userName = userName;  // Sets the userName field to the provided value.
    }

    // Getter method for description - Retrieves the description of the chat (e.g., the last message).
    public String getDescription() {
        return description;  // Returns the last message or description associated with the chat.
    }

    // Setter method for description - Allows setting the description value.
    public void setDescription(String description) {
        this.description = description;  // Sets the description field to the provided value.
    }

    // Getter method for date - Retrieves the date value.
    public String getDate() {
        return date;  // Returns the date of the chat (usually the time of the last message).
    }

    // Setter method for date - Allows setting the date value.
    public void setDate(String date) {
        this.date = date;  // Sets the date field to the provided value.
    }

    // Getter method for urlProfile - Retrieves the URL to the user's profile image.
    public String getUrlProfile() {
        return urlProfile;  // Returns the URL of the user's profile image.
    }

    // Setter method for urlProfile - Allows setting the URL to the profile image.
    public void setUrlProfile(String urlProfile) {
        this.urlProfile = urlProfile;  // Sets the urlProfile field to the provided value.
    }

    // Getter method for userPhone - Retrieves the phone number of the user.
    public String getUserPhone() {
        return userPhone;  // Returns the phone number of the user.
    }

    // Setter method for userPhone - Allows setting the userPhone value.
    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;  // Sets the userPhone field to the provided value.
    }

    public String getUserBio() {
        return UserBio;
    }

    public void setUserBio(String userBio) {
        UserBio = userBio;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getmCount() {
        return mCount;
    }

    public void setmCount(String mCount) {
        this.mCount = mCount;
    }

    public String getLastM() {
        return lastM;
    }

    public void setLastM(String lastM) {
        this.lastM = lastM;
    }

}
