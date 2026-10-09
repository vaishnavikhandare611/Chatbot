package com.vaishnavi.chatbot;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class HistoryItem {
        public static final int TYPE_HEADER = 0;
        public static final int TYPE_SESSION = 1;

        private final int type;
        private final String title;
        private final ChatSession session;

        public HistoryItem(String headerTitle) {
            this.type = TYPE_HEADER;
            this.title = headerTitle;
            this.session = null;
        }

        public HistoryItem(ChatSession session) {
            this.type = TYPE_SESSION;
            this.title = session.getTitle();
            this.session = session;
        }

        public int getType() { return type; }
        public String getTitle() { return title; }
        public ChatSession getSession() { return session; }
    }

    private final List<HistoryItem> items = new ArrayList<>();
    private final OnChatActionListener listener;

    public interface OnChatActionListener {
        void onChatSelected(ChatSession session);
        void onChatRenamed(ChatSession session, String newTitle);
        void onChatDeleted(ChatSession session);
    }

    public ChatHistoryAdapter(List<ChatSession> rawSessions, OnChatActionListener listener) {
        this.listener = listener;
        updateSessions(rawSessions);
    }

    public void updateSessions(List<ChatSession> rawSessions) {
        this.items.clear();
        this.items.addAll(buildHistoryItems(rawSessions));
        notifyDataSetChanged();
    }

    public void filterSessions(String query, List<ChatSession> rawSessions) {
        this.items.clear();
        this.items.addAll(buildSearchHistoryItems(query, rawSessions));
        notifyDataSetChanged();
    }

    private static List<HistoryItem> buildSearchHistoryItems(String query, List<ChatSession> rawSessions) {
        List<HistoryItem> result = new ArrayList<>();
        if (rawSessions == null) return result;

        String cleanQuery = query != null ? query.toLowerCase().trim() : "";
        if (cleanQuery.isEmpty()) {
            return buildHistoryItems(rawSessions);
        }

        List<ChatSession> matchingSessions = new ArrayList<>();
        for (ChatSession session : rawSessions) {
            if (session == null) continue;
            boolean matches = false;
            if (session.getTitle() != null && session.getTitle().toLowerCase().contains(cleanQuery)) {
                matches = true;
            } else if (session.getMessages() != null) {
                for (ChatMessage msg : session.getMessages()) {
                    if (msg != null && msg.getText() != null && msg.getText().toLowerCase().contains(cleanQuery)) {
                        matches = true;
                        break;
                    }
                }
            }
            if (matches) {
                matchingSessions.add(session);
            }
        }

        if (matchingSessions.isEmpty()) {
            result.add(new HistoryItem("No chats found."));
        } else {
            result.add(new HistoryItem("Search Results"));
            for (ChatSession s : matchingSessions) {
                result.add(new HistoryItem(s));
            }
        }
        return result;
    }

    private static List<HistoryItem> buildHistoryItems(List<ChatSession> rawSessions) {
        List<HistoryItem> result = new ArrayList<>();
        if (rawSessions == null || rawSessions.isEmpty()) {
            return result;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long todayStart = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_YEAR, -1);
        long yesterdayStart = calendar.getTimeInMillis();

        List<ChatSession> todayList = new ArrayList<>();
        List<ChatSession> yesterdayList = new ArrayList<>();
        List<ChatSession> olderList = new ArrayList<>();

        for (ChatSession session : rawSessions) {
            if (session == null) continue;
            long ts = session.getTimestamp();
            if (ts >= todayStart) {
                todayList.add(session);
            } else if (ts >= yesterdayStart) {
                yesterdayList.add(session);
            } else {
                olderList.add(session);
            }
        }

        if (!todayList.isEmpty()) {
            result.add(new HistoryItem("Today"));
            for (ChatSession s : todayList) {
                result.add(new HistoryItem(s));
            }
        }

        if (!yesterdayList.isEmpty()) {
            result.add(new HistoryItem("Yesterday"));
            for (ChatSession s : yesterdayList) {
                result.add(new HistoryItem(s));
            }
        }

        if (!olderList.isEmpty()) {
            result.add(new HistoryItem("Older"));
            for (ChatSession s : olderList) {
                result.add(new HistoryItem(s));
            }
        }

        return result;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HistoryItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_history_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_history, parent, false);
            return new SessionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HistoryItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).headerTitleText.setText(item.getTitle());
        } else if (holder instanceof SessionViewHolder) {
            SessionViewHolder vh = (SessionViewHolder) holder;
            ChatSession session = item.getSession();
            if (session != null) {
                vh.titleText.setText(session.getTitle());
                if (vh.timeText != null) {
                    vh.timeText.setText(formatSessionTime(session.getTimestamp()));
                }

                vh.itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onChatSelected(session);
                });

                vh.itemView.setOnLongClickListener(v -> {
                    if (listener != null) {
                        EditText input = new EditText(v.getContext());
                        input.setText(session.getTitle());
                        input.setTextColor(Color.WHITE);
                        input.setPadding(32, 16, 32, 16);
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Rename Chat")
                                .setView(input)
                                .setPositiveButton("Save", (dialog, which) -> {
                                    String newTitle = input.getText().toString().trim();
                                    if (!newTitle.isEmpty()) {
                                        listener.onChatRenamed(session, newTitle);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                    return true;
                });

                if (vh.deleteButton != null) {
                    vh.deleteButton.setOnClickListener(v -> {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Delete Conversation")
                                .setMessage("Are you sure you want to delete '" + session.getTitle() + "'?")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    if (listener != null) listener.onChatDeleted(session);
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    });
                }
            }
        }
    }

    private static String formatSessionTime(long timestamp) {
        if (timestamp <= 0) return "";
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long todayStart = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_YEAR, -1);
        long yesterdayStart = calendar.getTimeInMillis();

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String timeStr = timeFormat.format(new Date(timestamp)).toLowerCase();

        if (timestamp >= todayStart) {
            return "Today, " + timeStr;
        } else if (timestamp >= yesterdayStart) {
            return "Yesterday, " + timeStr;
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
            return dateFormat.format(new Date(timestamp));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitleText;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTitleText = itemView.findViewById(R.id.headerTitleText);
        }
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView timeText;
        ImageButton deleteButton;

        SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.chatTitleText);
            timeText = itemView.findViewById(R.id.chatTimeText);
            deleteButton = itemView.findViewById(R.id.deleteChatButton);
        }
    }
}
