package com.vaishnavi.chatbot;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChatStorage {
    private static String getFileName(String username) {
        String safeUser = (username != null && !username.trim().isEmpty()) ? username.replaceAll("[^a-zA-Z0-9_]", "_") : "default_user";
        return "chat_sessions_" + safeUser + ".json";
    }

    public static synchronized void saveSessions(Context context, String username, List<ChatSession> sessions) {
        if (context == null) return;
        try {
            JSONArray jsonArray = new JSONArray();
            if (sessions != null) {
                for (ChatSession session : sessions) {
                    if (session == null) continue;
                    JSONObject sObj = new JSONObject();
                    sObj.put("id", session.getId() != null ? session.getId() : String.valueOf(System.currentTimeMillis()));
                    sObj.put("title", session.getTitle() != null ? session.getTitle() : "New Chat");
                    sObj.put("timestamp", session.getTimestamp());

                    JSONArray mArray = new JSONArray();
                    if (session.getMessages() != null) {
                        for (ChatMessage msg : session.getMessages()) {
                            if (msg == null) continue;
                            JSONObject mObj = new JSONObject();
                            mObj.put("sender", msg.getSender() != null ? msg.getSender().name() : "USER");
                            mObj.put("text", msg.getText() != null ? msg.getText() : "");
                            mObj.put("attachmentPath", msg.getAttachmentPath() != null ? msg.getAttachmentPath() : "");
                            mObj.put("timestamp", msg.getTimestamp());
                            mArray.put(mObj);
                        }
                    }
                    sObj.put("messages", mArray);
                    jsonArray.put(sObj);
                }
            }

            File file = new File(context.getFilesDir(), getFileName(username));
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized List<ChatSession> loadSessions(Context context, String username) {
        List<ChatSession> sessions = new ArrayList<>();
        if (context == null) return sessions;
        try {
            File file = new File(context.getFilesDir(), getFileName(username));
            if (!file.exists()) return sessions;

            String jsonStr;
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[(int) file.length()];
                int read = fis.read(buffer);
                if (read > 0) {
                    jsonStr = new String(buffer, 0, read, StandardCharsets.UTF_8);
                } else {
                    return sessions;
                }
            }

            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject sObj = jsonArray.getJSONObject(i);
                String title = sObj.optString("title", "New Chat");
                ChatSession session = new ChatSession(title);
                if (sObj.has("id")) {
                    session.setId(sObj.optString("id"));
                }
                if (sObj.has("timestamp")) {
                    session.setTimestamp(sObj.optLong("timestamp", System.currentTimeMillis()));
                }
                JSONArray mArray = sObj.optJSONArray("messages");
                if (mArray != null) {
                    for (int j = 0; j < mArray.length(); j++) {
                        JSONObject mObj = mArray.getJSONObject(j);
                        String senderStr = mObj.optString("sender", "USER");
                        ChatMessage.Sender sender = ChatMessage.Sender.USER;
                        try {
                            sender = ChatMessage.Sender.valueOf(senderStr.trim().toUpperCase());
                        } catch (Exception ignored) {
                        }
                        String text = mObj.optString("text", "");
                        String attach = mObj.optString("attachmentPath", "");
                        ChatMessage msg = new ChatMessage(sender, text, attach.isEmpty() ? null : attach);
                        session.getMessages().add(msg);
                    }
                }
                sessions.add(session);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessions;
    }
}
