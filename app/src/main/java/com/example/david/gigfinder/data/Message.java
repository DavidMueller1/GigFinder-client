package com.example.david.gigfinder.data;

public class Message {
    String message;
    long createdAt;
    String name;
    boolean me;
    private byte[] picture;

    public Message(String message, String name, boolean me, long createdAt, byte[] picture) {
        this.message = message;
        this.name = name;
        this.me = me;
        this.createdAt = createdAt;
        this.picture = picture;
    }

    public String getMessage() {
        return message;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public boolean isMe() {
        return me;
    }

    public byte[] getPicture() {
        return picture;
    }
}
