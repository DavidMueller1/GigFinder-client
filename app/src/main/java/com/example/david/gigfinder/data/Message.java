package com.example.david.gigfinder.data;

public class Message {
    String message;
    long createdAt;
    String name;
    boolean me;

    public Message(String message, String name, boolean me, long createdAt) {
        this.message = message;
        this.name = name;
        this.me = me;
        this.createdAt = createdAt;
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
}
