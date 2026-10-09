package com.vaishnavi.chatbot;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    public enum Sender { USER, BOT }

    private String id;
    private Sender sender;
    private String text;
    private String attachmentPath;
    private long timestamp;

    public ChatMessage(Sender sender, String text, String attachmentPath) {
        this.id = String.valueOf(System.currentTimeMillis()) + "_" + Math.random();
        this.sender = sender;
        this.text = text;
        this.attachmentPath = attachmentPath;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public Sender getSender() { return sender; }
    public String getText() { return text; }
    public String getAttachmentPath() { return attachmentPath; }
    public long getTimestamp() { return timestamp; }
}
