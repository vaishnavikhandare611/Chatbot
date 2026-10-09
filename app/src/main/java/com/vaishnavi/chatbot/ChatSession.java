package com.vaishnavi.chatbot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChatSession implements Serializable {
    private String id;
    private String title;
    private long timestamp;
    private List<ChatMessage> messages;

    public ChatSession(String title) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.title = title != null && !title.trim().isEmpty() ? title : "New Chat";
        this.timestamp = System.currentTimeMillis();
        this.messages = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) {
        if (id != null && !id.trim().isEmpty()) {
            this.id = id;
        }
    }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) {
        if (timestamp > 0) {
            this.timestamp = timestamp;
        }
    }
    public List<ChatMessage> getMessages() { return messages; }
    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        this.timestamp = System.currentTimeMillis();
    }
}
