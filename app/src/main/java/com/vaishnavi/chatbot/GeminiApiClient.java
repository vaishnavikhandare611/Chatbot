package com.vaishnavi.chatbot;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Communicates with Google Gemini Generative AI API to answer open-ended ML queries,
 * code requests, math problems, Hinglish/Marathi questions, and multi-turn follow-ups.
 */
public class GeminiApiClient {

    public interface AiCallback {
        void onSuccess(String responseText);
        void onFailure(Exception e);
    }

    private static final String API_KEY = "AIzaSyD3F_40TlNsKXcu8qDT6Zybd2V_s6w46Uo";
    private static final String MODEL_URL_2_5 = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
    private static final String MODEL_URL_1_5 = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler;

    public GeminiApiClient() {
        Handler h;
        try {
            h = new Handler(Looper.getMainLooper());
        } catch (Throwable t) {
            h = null; // Unmocked Android framework in JVM local tests
        }
        this.mainHandler = h;
    }

    private static final String SYSTEM_PROMPT =
            "You are an intelligent, friendly, expert Machine Learning Tutor in an Android App.\n" +
            "Your mission is to help students, developers, and ML learners understand Machine Learning, Artificial Intelligence, Deep Learning, Data Preprocessing, Feature Engineering, Model Evaluation, Mathematics/Statistics for ML, Python Code (NumPy, Pandas, Scikit-learn, TensorFlow, PyTorch), and ML Project implementation.\n\n" +
            "CRITICAL EXPLANATION MANDATE:\n" +
            "When the user asks 'Explain its types', 'Explain the types', 'Explain types of [topic]', or any question asking to list/explain types, categories, or algorithms of a topic, NEVER give only the names of the types. You MUST list each type AND explain each type briefly in simple, easy-to-understand language (2-3 sentences per type). Do not skip the explanations.\n" +
            "If the user asks about a specific type separately, explain that specific type clearly with its definition, working, and a simple example when appropriate.\n\n" +
            "General Rules & Tone:\n" +
            "1. Answer ANY reasonable Machine Learning related question, including new, complex, unseen, or troubleshooting questions.\n" +
            "2. If the user asks in Hinglish or Marathi (e.g., 'ML mhnje kay?', 'clustering che types sang', 'tyache advantages kay?', 'overfitting ka hota?'), understand their intent perfectly and respond warmly in clear natural language.\n" +
            "3. Use conversation context provided to resolve follow-up questions like 'What are its types?', 'Explain the first one', 'Give an example', 'advantages?', 'how it works?'.\n" +
            "4. Structure answers cleanly with key definitions, bullet points, step-by-step working, code blocks, or comparison tables when useful.\n" +
            "5. Keep the response educational, accurate, encouraging, and easy to read on a mobile screen.\n";

    public void generateResponse(String userPrompt, ConversationContext context, AiCallback callback) {
        executor.execute(() -> {
            try {
                String responseText = callGeminiApi(userPrompt, context);
                postSuccess(callback, responseText);
            } catch (Exception e) {
                // Try fallback endpoint or report failure
                try {
                    String fallbackText = callGeminiApiFallback(userPrompt, context);
                    postSuccess(callback, fallbackText);
                } catch (Exception ex) {
                    postFailure(callback, ex);
                }
            }
        });
    }

    private void postSuccess(AiCallback callback, String text) {
        if (mainHandler != null) {
            mainHandler.post(() -> callback.onSuccess(text));
        } else {
            callback.onSuccess(text);
        }
    }

    private void postFailure(AiCallback callback, Exception e) {
        if (mainHandler != null) {
            mainHandler.post(() -> callback.onFailure(e));
        } else {
            callback.onFailure(e);
        }
    }

    private String callGeminiApi(String userPrompt, ConversationContext context) throws Exception {
        return sendRequestToEndpoint(MODEL_URL_2_5, userPrompt, context);
    }

    private String callGeminiApiFallback(String userPrompt, ConversationContext context) throws Exception {
        return sendRequestToEndpoint(MODEL_URL_1_5, userPrompt, context);
    }

    private String sendRequestToEndpoint(String endpointUrl, String userPrompt, ConversationContext context) throws Exception {
        URL url = new URL(endpointUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setConnectTimeout(8000); // 8 seconds timeout
        conn.setReadTimeout(10000);   // 10 seconds read timeout
        conn.setDoOutput(true);

        // Build Payload
        JSONObject root = new JSONObject();

        // System Instruction
        JSONObject systemInstruction = new JSONObject();
        JSONArray sysParts = new JSONArray();
        JSONObject sysPart = new JSONObject();
        sysPart.put("text", SYSTEM_PROMPT);
        sysParts.put(sysPart);
        systemInstruction.put("parts", sysParts);
        root.put("system_instruction", systemInstruction);

        // Contents
        JSONArray contents = new JSONArray();

        // Include Context if present
        if (context != null) {
            String contextSummary = context.buildContextSummary();
            if (!contextSummary.isEmpty()) {
                JSONObject contextContent = new JSONObject();
                contextContent.put("role", "user");
                JSONArray contextParts = new JSONArray();
                JSONObject cPart = new JSONObject();
                cPart.put("text", "[Active Conversation Context]:\n" + contextSummary);
                contextParts.put(cPart);
                contextContent.put("parts", contextParts);
                contents.put(contextContent);

                JSONObject contextAck = new JSONObject();
                contextAck.put("role", "model");
                JSONArray ackParts = new JSONArray();
                JSONObject aPart = new JSONObject();
                aPart.put("text", "Understood active conversation context.");
                ackParts.put(aPart);
                contextAck.put("parts", ackParts);
                contents.put(contextAck);
            }
        }

        // Current User Prompt
        JSONObject userContent = new JSONObject();
        userContent.put("role", "user");
        JSONArray userParts = new JSONArray();
        JSONObject uPart = new JSONObject();
        uPart.put("text", userPrompt);
        userParts.put(uPart);
        userContent.put("parts", userParts);
        contents.put(userContent);

        root.put("contents", contents);

        // Send JSON
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = root.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream is = conn.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray candidates = jsonResponse.optJSONArray("candidates");
                if (candidates != null && candidates.length() > 0) {
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    JSONObject contentObj = firstCandidate.getJSONObject("content");
                    JSONArray partsArray = contentObj.getJSONArray("parts");
                    if (partsArray.length() > 0) {
                        return partsArray.getJSONObject(0).getString("text").trim();
                    }
                }
            }
        }

        throw new Exception("HTTP Error Code: " + responseCode);
    }
}
