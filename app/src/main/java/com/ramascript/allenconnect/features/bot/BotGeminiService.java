package com.ramascript.allenconnect.features.bot;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BotGeminiService {
    private static final String TAG = "BotGeminiService";
    private static final String GEMINI_API_KEY = "AIzaSyCm79FWDaAaftqAftv3GoXG6kP1cxX1Yho";
    private static final int MAX_CONTEXT_LENGTH = 20000; // Limited to avoid token overflows
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent";

    private final OkHttpClient client;
    private final DatabaseReference database;
    private final Context context;

    public interface ResponseCallback {
        void onResponseReceived(String response);

        void onError(String errorMessage);
    }

    public BotGeminiService(Context context) {
        this.context = context;

        // Initialize OkHttpClient
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().getReference();
    }

    public void getResponseWithDocumentContext(String userId, String question, ResponseCallback callback) {
        // First, check if we need to include document context
        fetchRelevantDocumentText(userId, question, (contextText) -> {
            if (contextText != null && !contextText.isEmpty()) {
                // We have relevant document context, include it in the prompt
                String enhancedPrompt = buildPromptWithContext(question, contextText);
                generateResponse(enhancedPrompt, callback);
            } else {
                // No relevant context, just answer the question directly
                generateResponse(question, callback);
            }
        });
    }

    private void fetchRelevantDocumentText(String userId, String question, DocumentContextCallback contextCallback) {
        // Query all documents uploaded by the user
        Query query = database.child("data_for_chatbot")
                .orderByChild("userId")
                .equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> relevantTexts = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String extractedText = snapshot.child("extractedText").getValue(String.class);

                    if (extractedText != null && !extractedText.isEmpty()) {
                        // Include this document's text in our context
                        relevantTexts.add(extractedText);
                    }
                }

                // Combine all relevant document texts
                String combinedContext = combineAndTruncateContexts(relevantTexts);
                contextCallback.onContextFetched(combinedContext);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching document context: " + databaseError.getMessage());
                contextCallback.onContextFetched(""); // Empty context on error
            }
        });
    }

    private String combineAndTruncateContexts(List<String> contextTexts) {
        StringBuilder combined = new StringBuilder();

        for (String text : contextTexts) {
            combined.append(text).append("\n\n");

            // If we've reached our limit, stop adding more context
            if (combined.length() > MAX_CONTEXT_LENGTH) {
                combined.setLength(MAX_CONTEXT_LENGTH);
                break;
            }
        }

        return combined.toString();
    }

    private String buildPromptWithContext(String question, String contextText) {
        return "You are Allen Bot, a virtual assistant developed by Ramanand Kumar Gupta, a BCA student of Allenhouse Business School, Rooma, Kanpur. Your primary purpose is to assist and answer queries of students and faculty members of Allenhouse Business School. You are designed to be informative, helpful, and supportive.\n" +
            "\n" +
            "You must always behave politely and respectfully. Never use offensive, harmful, vulgar, or inappropriate language. If a user tries to engage in inappropriate or harmful conversation, you must firmly decline and steer the conversation back to a productive and respectful direction.\n" +
            "\n" +
            "You are expected to assist students with academic questions, college-related information, event details, and general queries in a helpful and clear manner. If you are asked about your identity, you should respond:\n" +
            "\n" +
            "\"I am Allen Bot, developed by Ramanand Kumar Gupta, a BCA student of Allenhouse Business School, Rooma, Kanpur. I am here to assist you.\"\n" +
            "\n" +
            "Handle all conversations responsibly and ethically. Always provide helpful, factual, and safe responses.\nAnswer the following question using only the provided context and your general knowledge. " +
                "If the context doesn't contain the information needed to answer the question, say you don't have " +
                "enough information from the documents.\n\n" +
                "Context:\n" + contextText + "\n\n" +
                "Question: " + question;
    }

    public void generateResponse(String prompt, ResponseCallback callback) {
        new Thread(() -> {
            try {
                // Create request JSON with proper structure
                JSONObject requestJson = new JSONObject();

                // Create content array
                JSONObject content = new JSONObject();
                JSONArray parts = new JSONArray();
                JSONObject textPart = new JSONObject();
                textPart.put("text", prompt);
                parts.put(textPart);
                content.put("parts", parts);
                content.put("role", "user");

                JSONArray contentsArray = new JSONArray();
                contentsArray.put(content);
                requestJson.put("contents", contentsArray);

                // Configure generation settings
                JSONObject generationConfig = new JSONObject();
                generationConfig.put("temperature", 0.7);
                generationConfig.put("topK", 40);
                generationConfig.put("topP", 0.95);
                generationConfig.put("maxOutputTokens", 1024);
                requestJson.put("generationConfig", generationConfig);

                // Create API request
                MediaType JSON = MediaType.get("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(requestJson.toString(), JSON);

                String apiUrlWithKey = GEMINI_API_URL + "?key=" + GEMINI_API_KEY;
                Request request = new Request.Builder()
                        .url(apiUrlWithKey)
                        .post(body)
                        .build();

                // Execute request
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        // Extract response text
                        String responseText = jsonResponse
                                .getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        // Return response on main thread
                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                        mainHandler.post(() -> callback.onResponseReceived(responseText));
                    } else {
                        String errorMessage = "API request failed: " +
                                (response.body() != null ? response.body().string() : "Unknown error");
                        Log.e(TAG, errorMessage);

                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                        mainHandler.post(() -> callback.onError(errorMessage));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error calling Gemini API", e);

                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> callback.onError("Error calling Gemini API: " + e.getMessage()));
            }
        }).start();
    }

    public void listAvailableModels(ResponseCallback callback) {
        new Thread(() -> {
            try {
                String listModelsUrl = "https://generativelanguage.googleapis.com/v1/models?key=" + GEMINI_API_KEY;
                Request request = new Request.Builder()
                        .url(listModelsUrl)
                        .get()
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                        mainHandler.post(() -> callback.onResponseReceived("Available models: " + responseBody));
                    } else {
                        String errorMessage = "Failed to list models: " +
                                (response.body() != null ? response.body().string() : "Unknown error");
                        Log.e(TAG, errorMessage);
                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                        mainHandler.post(() -> callback.onError(errorMessage));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error listing models", e);
                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> callback.onError("Error listing models: " + e.getMessage()));
            }
        }).start();
    }

    private interface DocumentContextCallback {
        void onContextFetched(String contextText);
    }

    // For saving extracted text to Firebase
    public void saveExtractedText(String fileId, String extractedText, String userId) {
        database.child("data_for_chatbot").child(fileId).child("extractedText").setValue(extractedText)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Text extraction saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving extracted text", e));
    }
}