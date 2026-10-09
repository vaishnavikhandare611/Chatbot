package com.vaishnavi.chatbot;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    RecyclerView chatRecyclerView;
    RecyclerView chatHistoryRecyclerView;
    View welcomeContainer;

    EditText messageInput;
    Button sendButton;
    ImageButton attachMenuButton;
    ImageButton micButton;
    ImageButton hamburgerButton;
    ImageButton closeDrawerButton;
    ImageButton removeAttachmentButton;
    ImageView attachmentThumbnail;
    LinearLayout attachmentPreviewContainer;
    TextView attachmentNameText;
    TextView profileNameText;

    View newChatDrawerButton;
    View clearChatDrawerButton;
    View chatHistoryDrawerButton;
    View settingsDrawerButton;
    View logoutDrawerButton;
    TextView seeAllHistoryButton;

    NlpChatbot nlpChatbot;
    OfflineOCRProcessor ocrProcessor;
    Handler mainHandler;

    private String currentUsername = "User";
    private List<ChatSession> allChatSessions = new ArrayList<>();
    private ChatSession currentSession;
    private List<ChatMessage> currentMessages = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private ChatHistoryAdapter historyAdapter;

    private Uri selectedImageUri;
    private Uri selectedFileUri;
    private String selectedFileName;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    selectedFileUri = null;
                    showImagePreview(uri);
                }
            });

    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    selectedImageUri = null;
                    selectedFileName = getFileName(uri);
                    showFilePreview(selectedFileName);
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null && extras.get("data") instanceof Bitmap) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        try {
                            File file = File.createTempFile("cam_", ".jpg", getCacheDir());
                            FileOutputStream fos = new FileOutputStream(file);
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.close();
                            selectedImageUri = Uri.fromFile(file);
                            selectedFileUri = null;
                            showImagePreview(selectedImageUri);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<Intent> speechRecognizerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String spokenText = matches.get(0);
                        String currentText = messageInput != null ? messageInput.getText().toString() : "";
                        if (messageInput != null) {
                            if (!currentText.isEmpty()) {
                                messageInput.setText(currentText + " " + spokenText);
                            } else {
                                messageInput.setText(spokenText);
                            }
                            messageInput.setSelection(messageInput.getText().length());
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check authentication state
        SharedPreferences authPrefs = getSharedPreferences(LoginActivity.PREF_NAME, MODE_PRIVATE);
        if (!authPrefs.contains(LoginActivity.KEY_IS_LOGGED_IN)) {
            authPrefs.edit()
                    .putBoolean(LoginActivity.KEY_IS_LOGGED_IN, true)
                    .putString(LoginActivity.KEY_USERNAME, "User")
                    .apply();
        } else if (!authPrefs.getBoolean(LoginActivity.KEY_IS_LOGGED_IN, false)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUsername = authPrefs.getString(LoginActivity.KEY_USERNAME, "User");

        setContentView(R.layout.activity_main);

        // Views
        drawerLayout = findViewById(R.id.drawerLayout);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatHistoryRecyclerView = findViewById(R.id.chatHistoryRecyclerView);
        welcomeContainer = findViewById(R.id.welcomeContainer);

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        attachMenuButton = findViewById(R.id.attachMenuButton);
        micButton = findViewById(R.id.micButton);
        hamburgerButton = findViewById(R.id.hamburgerButton);
        closeDrawerButton = findViewById(R.id.closeDrawerButton);
        removeAttachmentButton = findViewById(R.id.removeAttachmentButton);
        attachmentThumbnail = findViewById(R.id.attachmentThumbnail);
        attachmentPreviewContainer = findViewById(R.id.attachmentPreviewContainer);
        attachmentNameText = findViewById(R.id.attachmentNameText);
        profileNameText = findViewById(R.id.profileNameText);

        newChatDrawerButton = findViewById(R.id.newChatDrawerButton);
        clearChatDrawerButton = findViewById(R.id.clearChatDrawerButton);
        chatHistoryDrawerButton = findViewById(R.id.chatHistoryDrawerButton);
        settingsDrawerButton = findViewById(R.id.settingsDrawerButton);
        logoutDrawerButton = findViewById(R.id.logoutDrawerButton);
        seeAllHistoryButton = findViewById(R.id.seeAllHistoryButton);

        if (messageInput != null) {
            messageInput.setTextColor(Color.parseColor("#111827"));
            messageInput.setHintTextColor(Color.parseColor("#9CA3AF"));
            messageInput.setCursorVisible(true);
        }

        if (profileNameText != null) {
            profileNameText.setText(currentUsername);
        }

        nlpChatbot = new NlpChatbot(this);
        ocrProcessor = new OfflineOCRProcessor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Load sessions for current user
        allChatSessions = ChatStorage.loadSessions(this, currentUsername);
        if (allChatSessions == null) {
            allChatSessions = new ArrayList<>();
        }

        // On fresh app launch, start with a fresh New Chat screen (no active session selected automatically)
        currentSession = null;
        currentMessages = new ArrayList<>();

        // Setup chat RecyclerView
        if (chatRecyclerView != null) {
            chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            chatAdapter = new ChatAdapter(this, currentMessages);
            chatRecyclerView.setAdapter(chatAdapter);
        }

        updateUIState();

        // Setup history RecyclerView in Sidebar
        if (chatHistoryRecyclerView != null) {
            chatHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            historyAdapter = new ChatHistoryAdapter(allChatSessions, new ChatHistoryAdapter.OnChatActionListener() {
                @Override
                public void onChatSelected(ChatSession session) {
                    if (session == null) return;
                    currentSession = session;
                    currentMessages = session.getMessages() != null ? session.getMessages() : new ArrayList<>();
                    chatAdapter = new ChatAdapter(MainActivity.this, currentMessages);
                    if (chatRecyclerView != null) {
                        chatRecyclerView.setAdapter(chatAdapter);
                        if (!currentMessages.isEmpty()) {
                            chatRecyclerView.scrollToPosition(currentMessages.size() - 1);
                        }
                    }
                    updateUIState();
                    if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawers();
                    }
                }

                @Override
                public void onChatRenamed(ChatSession session, String newTitle) {
                    if (session == null) return;
                    session.setTitle(newTitle);
                    ChatStorage.saveSessions(MainActivity.this, currentUsername, allChatSessions);
                    refreshHistory();
                }

                @Override
                public void onChatDeleted(ChatSession session) {
                    if (session == null) return;
                    allChatSessions.remove(session);
                    if (allChatSessions.isEmpty()) {
                        createNewChat();
                    } else if (currentSession == session) {
                        currentSession = allChatSessions.get(0);
                        currentMessages = currentSession != null && currentSession.getMessages() != null ? currentSession.getMessages() : new ArrayList<>();
                        chatAdapter = new ChatAdapter(MainActivity.this, currentMessages);
                        if (chatRecyclerView != null) {
                            chatRecyclerView.setAdapter(chatAdapter);
                        }
                    }
                    updateUIState();
                    ChatStorage.saveSessions(MainActivity.this, currentUsername, allChatSessions);
                    refreshHistory();
                }
            });
            chatHistoryRecyclerView.setAdapter(historyAdapter);
        }

        EditText chatSearchInput = findViewById(R.id.chatSearchInput);
        ImageButton clearSearchButton = findViewById(R.id.clearSearchButton);

        if (chatSearchInput != null) {
            chatSearchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString();
                    if (clearSearchButton != null) {
                        clearSearchButton.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    if (historyAdapter != null) {
                        historyAdapter.filterSessions(query, allChatSessions);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (clearSearchButton != null) {
            clearSearchButton.setOnClickListener(v -> {
                if (chatSearchInput != null) {
                    chatSearchInput.setText("");
                }
                clearSearchButton.setVisibility(View.GONE);
                if (historyAdapter != null) {
                    historyAdapter.filterSessions("", allChatSessions);
                }
            });
        }

        // Click listeners
        if (hamburgerButton != null && drawerLayout != null) {
            hamburgerButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }
        if (closeDrawerButton != null && drawerLayout != null) {
            closeDrawerButton.setOnClickListener(v -> drawerLayout.closeDrawers());
        }

        if (newChatDrawerButton != null) {
            newChatDrawerButton.setOnClickListener(v -> createNewChat());
        }

        if (clearChatDrawerButton != null) {
            clearChatDrawerButton.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Clear Chat")
                        .setMessage("Are you sure you want to clear the current conversation?")
                        .setPositiveButton("Clear", (dialog, which) -> {
                            if (currentSession != null) {
                                currentSession.getMessages().clear();
                                currentMessages.clear();
                                if (chatAdapter != null) {
                                    chatAdapter.notifyDataSetChanged();
                                }
                                updateUIState();
                                ChatStorage.saveSessions(MainActivity.this, currentUsername, allChatSessions);
                            }
                            if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                                drawerLayout.closeDrawers();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        if (chatHistoryDrawerButton != null || seeAllHistoryButton != null) {
            View.OnClickListener showHistoryAction = v -> showFullHistoryDialog();
            if (chatHistoryDrawerButton != null) chatHistoryDrawerButton.setOnClickListener(showHistoryAction);
            if (seeAllHistoryButton != null) seeAllHistoryButton.setOnClickListener(showHistoryAction);
        }

        if (settingsDrawerButton != null) {
            settingsDrawerButton.setOnClickListener(v -> {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawers();
                }
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            });
        }

        if (logoutDrawerButton != null) {
            logoutDrawerButton.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Log Out")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Log Out", (dialog, which) -> {
                            authPrefs.edit().putBoolean(LoginActivity.KEY_IS_LOGGED_IN, false).apply();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        if (attachMenuButton != null) {
            attachMenuButton.setOnClickListener(v -> showAttachmentBottomSheet());
        }
        if (micButton != null) {
            micButton.setOnClickListener(v -> startSpeechRecognition());
        }
        if (removeAttachmentButton != null) {
            removeAttachmentButton.setOnClickListener(v -> clearAttachment());
        }

        if (sendButton != null) {
            sendButton.setOnClickListener(v -> {
                String message = messageInput != null ? messageInput.getText().toString().trim() : "";
                if (!message.isEmpty() || selectedImageUri != null || selectedFileUri != null) {
                    sendButton.setEnabled(false);
                    sendButton.setText("...");

                    final Uri imageToProcess = selectedImageUri;
                    final Uri fileToProcess = selectedFileUri;
                    final String userPrompt = message;

                    if (messageInput != null) messageInput.setText("");
                    clearAttachment();

                    if (currentSession == null) {
                        currentSession = new ChatSession("New Chat");
                        if (allChatSessions == null) {
                            allChatSessions = new ArrayList<>();
                        }
                        allChatSessions.add(0, currentSession);
                        currentMessages = currentSession.getMessages();
                        if (chatRecyclerView != null) {
                            chatAdapter = new ChatAdapter(MainActivity.this, currentMessages);
                            chatRecyclerView.setAdapter(chatAdapter);
                        }
                    }

                    // If first message in chat, set session title and refresh history
                    if (currentSession != null && currentSession.getMessages() != null && currentSession.getMessages().isEmpty()) {
                        String titleText = userPrompt;
                        if (titleText.isEmpty()) {
                            if (imageToProcess != null) titleText = "Image Search";
                            else if (fileToProcess != null) titleText = "File Query";
                            else titleText = "New Chat";
                        }
                        String title = titleText.length() > 25 ? titleText.substring(0, 25) + "..." : titleText;
                        currentSession.setTitle(title);
                        refreshHistory();
                    }

                    if (imageToProcess != null) {
                        String displayMsg = userPrompt.isEmpty() ? "[Attached Image]" : "[Attached Image] " + userPrompt;
                        addMessageToCurrentSession(ChatMessage.Sender.USER, displayMsg, imageToProcess.toString());
                        addMessageToCurrentSession(ChatMessage.Sender.BOT, "Processing image with offline OCR...", null);

                        ocrProcessor.processImage(MainActivity.this, imageToProcess, new OfflineOCRProcessor.OcrCallback() {
                            @Override
                            public void onSuccess(String extractedText) {
                                nlpChatbot.getResponseForImageAsync(extractedText, userPrompt, response -> {
                                    mainHandler.post(() -> {
                                        updateLastBotMessage(response);
                                        enableSendButton();
                                    });
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                String errorMsg = "Failed to extract text from image: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
                                updateLastBotMessage(errorMsg);
                                enableSendButton();
                            }
                        });

                    } else if (fileToProcess != null) {
                        String fileText = readTextFromUri(fileToProcess);
                        String displayMsg = userPrompt.isEmpty() ? "[Attached File: " + selectedFileName + "]" : "[Attached File: " + selectedFileName + "] " + userPrompt;
                        addMessageToCurrentSession(ChatMessage.Sender.USER, displayMsg, fileToProcess.toString());
                        addMessageToCurrentSession(ChatMessage.Sender.BOT, "Analyzing file...", null);

                        String fullPrompt = fileText != null && !fileText.isEmpty() ?
                                userPrompt + "\n\nFile content from " + selectedFileName + ":\n" + fileText :
                                userPrompt + " (Attached file: " + selectedFileName + ")";

                        nlpChatbot.getResponseAsync(fullPrompt, answer -> {
                            mainHandler.post(() -> {
                                updateLastBotMessage(answer);
                                enableSendButton();
                            });
                        });

                    } else {
                        addMessageToCurrentSession(ChatMessage.Sender.USER, userPrompt, null);
                        addMessageToCurrentSession(ChatMessage.Sender.BOT, "Thinking...", null);

                        nlpChatbot.getResponseAsync(userPrompt, answer -> {
                            mainHandler.post(() -> {
                                updateLastBotMessage(answer);
                                enableSendButton();
                            });
                        });
                    }
                }
            });
        }
    }

    private void updateUIState() {
        if (currentMessages == null || currentMessages.isEmpty()) {
            if (welcomeContainer != null) welcomeContainer.setVisibility(View.VISIBLE);
            if (chatRecyclerView != null) chatRecyclerView.setVisibility(View.GONE);
        } else {
            if (welcomeContainer != null) welcomeContainer.setVisibility(View.GONE);
            if (chatRecyclerView != null) chatRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAttachmentBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_attachment_bottom_sheet, null);
        bottomSheetDialog.setContentView(dialogView);

        View optionCamera = dialogView.findViewById(R.id.optionCamera);
        View optionPhotos = dialogView.findViewById(R.id.optionPhotos);
        View optionFiles = dialogView.findViewById(R.id.optionFiles);

        if (optionCamera != null) {
            optionCamera.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                checkAndOpenCamera();
            });
        }

        if (optionPhotos != null) {
            optionPhotos.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                galleryLauncher.launch("image/*");
            });
        }

        if (optionFiles != null) {
            optionFiles.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                filePickerLauncher.launch("*/*");
            });
        }

        bottomSheetDialog.show();
    }

    private void showFullHistoryDialog() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        }

        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setPadding(32, 24, 32, 24);

        ChatHistoryAdapter fullAdapter = new ChatHistoryAdapter(allChatSessions, new ChatHistoryAdapter.OnChatActionListener() {
            @Override
            public void onChatSelected(ChatSession session) {
                if (session == null) return;
                currentSession = session;
                currentMessages = session.getMessages() != null ? session.getMessages() : new ArrayList<>();
                chatAdapter = new ChatAdapter(MainActivity.this, currentMessages);
                if (chatRecyclerView != null) {
                    chatRecyclerView.setAdapter(chatAdapter);
                    if (!currentMessages.isEmpty()) {
                        chatRecyclerView.scrollToPosition(currentMessages.size() - 1);
                    }
                }
                updateUIState();
            }

            @Override
            public void onChatRenamed(ChatSession session, String newTitle) {
                if (session == null) return;
                session.setTitle(newTitle);
                ChatStorage.saveSessions(MainActivity.this, currentUsername, allChatSessions);
                refreshHistory();
            }

            @Override
            public void onChatDeleted(ChatSession session) {
                if (session == null) return;
                allChatSessions.remove(session);
                if (allChatSessions.isEmpty()) {
                    createNewChat();
                } else if (currentSession == session) {
                    currentSession = allChatSessions.get(0);
                    currentMessages = currentSession != null && currentSession.getMessages() != null ? currentSession.getMessages() : new ArrayList<>();
                    chatAdapter = new ChatAdapter(MainActivity.this, currentMessages);
                    if (chatRecyclerView != null) {
                        chatRecyclerView.setAdapter(chatAdapter);
                    }
                }
                updateUIState();
                ChatStorage.saveSessions(MainActivity.this, currentUsername, allChatSessions);
                refreshHistory();
            }
        });
        recyclerView.setAdapter(fullAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Complete Chat History")
                .setView(recyclerView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void createNewChat() {
        // If current session is already empty and has no messages, just reset inputs and close drawer
        if (currentSession != null && currentSession.getMessages() != null && currentSession.getMessages().isEmpty()) {
            if (messageInput != null) messageInput.setText("");
            clearAttachment();
            updateUIState();
            if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
            }
            return;
        }

        ChatSession newSession = new ChatSession("New Chat");
        if (allChatSessions == null) {
            allChatSessions = new ArrayList<>();
        }
        allChatSessions.add(0, newSession);

        currentSession = newSession;
        currentMessages = currentSession.getMessages();
        chatAdapter = new ChatAdapter(this, currentMessages);
        if (chatRecyclerView != null) {
            chatRecyclerView.setAdapter(chatAdapter);
        }

        updateUIState();
        refreshHistory();

        if (messageInput != null) messageInput.setText("");
        clearAttachment();

        ChatStorage.saveSessions(this, currentUsername, allChatSessions);
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        }
    }

    private void refreshHistory() {
        String query = "";
        EditText chatSearchInput = findViewById(R.id.chatSearchInput);
        if (chatSearchInput != null) {
            query = chatSearchInput.getText().toString();
        }
        if (historyAdapter != null) {
            historyAdapter.filterSessions(query, allChatSessions);
        }
    }

    private void addMessageToCurrentSession(ChatMessage.Sender sender, String text, String attachPath) {
        ChatMessage msg = new ChatMessage(sender, text, attachPath);
        if (currentMessages == null) {
            currentMessages = new ArrayList<>();
        }
        currentMessages.add(msg);
        if (chatAdapter != null) {
            chatAdapter.notifyItemInserted(currentMessages.size() - 1);
        }
        if (chatRecyclerView != null) {
            chatRecyclerView.scrollToPosition(currentMessages.size() - 1);
        }
        updateUIState();
        ChatStorage.saveSessions(this, currentUsername, allChatSessions);
    }

    private void updateLastBotMessage(String response) {
        if (currentMessages != null && !currentMessages.isEmpty()) {
            int lastIdx = currentMessages.size() - 1;
            ChatMessage last = currentMessages.get(lastIdx);
            if (last != null && last.getSender() == ChatMessage.Sender.BOT) {
                // Update bot placeholder message in-place and notify adapter
                ChatMessage updatedMsg = new ChatMessage(ChatMessage.Sender.BOT, response, null);
                currentMessages.set(lastIdx, updatedMsg);
                if (chatAdapter != null) {
                    chatAdapter.notifyItemChanged(lastIdx);
                }
                if (chatRecyclerView != null) {
                    chatRecyclerView.scrollToPosition(lastIdx);
                }
                updateUIState();
                ChatStorage.saveSessions(this, currentUsername, allChatSessions);
                return;
            }
        }
        addMessageToCurrentSession(ChatMessage.Sender.BOT, response, null);
    }

    private void checkAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraLauncher.launch(takePictureIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        try {
            speechRecognizerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImagePreview(Uri uri) {
        if (attachmentThumbnail != null && attachmentPreviewContainer != null && attachmentNameText != null) {
            attachmentThumbnail.setImageURI(uri);
            attachmentNameText.setText("Image: " + getFileName(uri));
            attachmentPreviewContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showFilePreview(String fileName) {
        if (attachmentThumbnail != null && attachmentPreviewContainer != null && attachmentNameText != null) {
            attachmentThumbnail.setImageResource(android.R.drawable.ic_menu_agenda);
            attachmentNameText.setText("File: " + fileName);
            attachmentPreviewContainer.setVisibility(View.VISIBLE);
        }
    }

    private void clearAttachment() {
        selectedImageUri = null;
        selectedFileUri = null;
        selectedFileName = null;
        if (attachmentPreviewContainer != null) {
            attachmentPreviewContainer.setVisibility(View.GONE);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri != null && uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx != -1) {
                        result = cursor.getString(idx);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null && uri != null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "Attached File";
    }

    private String readTextFromUri(Uri uri) {
        if (uri == null) return "";
        StringBuilder sb = new StringBuilder();
        try (InputStream is = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private void enableSendButton() {
        if (sendButton != null) {
            sendButton.setEnabled(true);
            sendButton.setText("Send");
        }
    }
}
