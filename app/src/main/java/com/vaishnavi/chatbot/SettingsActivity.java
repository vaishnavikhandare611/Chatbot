package com.vaishnavi.chatbot;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        TextView clearChatsButton = findViewById(R.id.clearChatsButton);
        clearChatsButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear All Chats")
                    .setMessage("Are you sure you want to delete all chat history?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        File file = new File(getFilesDir(), "chat_sessions.json");
                        if (file.exists()) {
                            file.delete();
                        }
                        Toast.makeText(this, "All chat history cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
