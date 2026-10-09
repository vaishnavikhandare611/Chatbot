package com.vaishnavi.chatbot;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

/**
 * Performs 100% on-device OCR (Optical Character Recognition) using ML Kit.
 * Does NOT require internet connection or cloud APIs.
 */
public class OfflineOCRProcessor {

    public interface OcrCallback {
        void onSuccess(String extractedText);
        void onFailure(Exception e);
    }

    private final TextRecognizer recognizer;

    public OfflineOCRProcessor() {
        // On-Device ML Kit Text Recognizer
        this.recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    public void processImage(Context context, Uri imageUri, OcrCallback callback) {
        if (context == null || imageUri == null) {
            callback.onFailure(new IllegalArgumentException("Invalid context or Uri"));
            return;
        }

        try {
            InputImage inputImage = InputImage.fromFilePath(context, imageUri);
            recognizer.process(inputImage)
                    .addOnSuccessListener(visionText -> {
                        String text = visionText.getText();
                        callback.onSuccess(text != null ? text.trim() : "");
                    })
                    .addOnFailureListener(callback::onFailure);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    public void processBitmap(Bitmap bitmap, OcrCallback callback) {
        if (bitmap == null) {
            callback.onFailure(new IllegalArgumentException("Bitmap is null"));
            return;
        }

        try {
            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
            recognizer.process(inputImage)
                    .addOnSuccessListener(visionText -> {
                        String text = visionText.getText();
                        callback.onSuccess(text != null ? text.trim() : "");
                    })
                    .addOnFailureListener(callback::onFailure);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }
}
