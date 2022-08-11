package com.fyp.iShare.ui.messages;

class UserMessage {
    String message;
    User sender;
    int createdAt;

    public UserMessage(String message, User sender, int createdAt) {
        this.message = message;
        this.sender = sender;
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public int getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(int createdAt) {
        this.createdAt = createdAt;
    }
}

class User {
    String nickname;
    String profileUrl;

    public User(String nickname, String profileUrl) {
        this.nickname = nickname;
        this.profileUrl = profileUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}