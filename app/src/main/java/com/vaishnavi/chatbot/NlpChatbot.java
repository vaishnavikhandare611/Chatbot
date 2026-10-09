package com.vaishnavi.chatbot;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Intelligent Machine Learning Conversational Tutor Engine.
 * Multi-turn Topic + Intent processing architecture.
 * Operates 100% OFFLINE using Topic Knowledge Repository, Intent Detector,
 * Question Matcher, and Offline Synthesizer.
 */
public class NlpChatbot {

    public interface ChatCallback {
        void onResponse(String responseText);
    }

    private final MLKnowledgeBase knowledgeBase;
    private final QuestionMatcher questionMatcher;
    private final ConversationContext conversationContext;
    private final OfflineSynthesizer offlineSynthesizer;

    public NlpChatbot() {
        this(null);
    }

    public NlpChatbot(Context context) {
        this.knowledgeBase = new MLKnowledgeBase(context);
        this.questionMatcher = new QuestionMatcher(knowledgeBase);
        this.conversationContext = new ConversationContext();
        this.offlineSynthesizer = new OfflineSynthesizer(knowledgeBase);
    }

    public ConversationContext getConversationContext() {
        return conversationContext;
    }

    private String checkSimpleGreeting(String preprocessedInput) {
        if (preprocessedInput.matches("^(hi|hii|hiii|hiiii|hello|hey|namaste|good morning|good afternoon|good evening)$")) {
            return "Hello! I am your Machine Learning Tutor. Ask me any question on ML, AI, Deep Learning, algorithms, code, math, or project implementation!";
        }
        return null;
    }

    private boolean isBroadMlTypesQuery(String rawLower, String preprocessedInput) {
        String cleanRaw = rawLower.replaceAll("[^a-zA-Z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        String cleanPrep = preprocessedInput.replaceAll("[^a-zA-Z0-9\\s]", " ").replaceAll("\\s+", " ").trim();

        // Check if any specific subtopic is present
        if (cleanRaw.contains("clustering") || cleanRaw.contains("regression") || cleanRaw.contains("classification") ||
            cleanRaw.contains("supervised") || cleanRaw.contains("unsupervised") || cleanRaw.contains("reinforcement") ||
            cleanRaw.contains("kmeans") || cleanRaw.contains("k means") || cleanRaw.contains("dbscan") ||
            cleanRaw.contains("decision tree") || cleanRaw.contains("random forest") || cleanRaw.contains("svm") ||
            cleanRaw.contains("knn") || cleanRaw.contains("naive bayes") || cleanRaw.contains("pca") || cleanRaw.contains("overfitting")) {
            return false;
        }

        // Exact match phrases
        if (cleanRaw.equals("types of ml") || cleanRaw.equals("types of machine learning") ||
            cleanRaw.equals("what are types of ml") || cleanRaw.equals("what are the types of ml") ||
            cleanRaw.equals("ml types") || cleanRaw.equals("machine learning types") ||
            cleanRaw.equals("ml che types") || cleanRaw.equals("machine learning che types") ||
            cleanRaw.equals("ml che prakar") || cleanRaw.equals("machine learning che prakar") ||
            cleanRaw.equals("types sang") || cleanRaw.equals("prakar sang") ||
            cleanPrep.equals("types of ml") || cleanPrep.equals("types machine learning") ||
            cleanPrep.equals("machine learning types") || cleanPrep.equals("types of machine learning")) {
            return true;
        }

        boolean hasTypesKeyword = cleanRaw.contains("types") || cleanRaw.contains("prakar") || cleanRaw.contains("categories") || cleanRaw.contains("kinds");
        boolean hasMlKeyword = cleanRaw.contains("machine learning") || cleanRaw.matches(".*\\bml\\b.*") || cleanPrep.contains("machine learning");

        if (hasTypesKeyword && hasMlKeyword) {
            return true;
        }

        if ((cleanRaw.equals("types") || cleanRaw.equals("prakar")) &&
            (conversationContext.getCurrentTopicEntity() == null || "Machine Learning".equalsIgnoreCase(conversationContext.getCurrentTopicEntity()))) {
            return true;
        }

        return false;
    }

    /**
     * Asynchronously generates an intelligent answer completely offline using Topic + Intent context and QuestionMatcher.
     */
    public void getResponseAsync(String userPrompt, ChatCallback callback) {
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            callback.onResponse("Please type a message!");
            return;
        }

        String response = getResponse(userPrompt);
        callback.onResponse(response);
    }

    /**
     * Synchronous response generator operating 100% offline.
     */
    public String getResponse(String userPrompt) {
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            return "Please type a message!";
        }

        String preprocessedInput = TextPreprocessor.preprocess(userPrompt);
        String rawLower = userPrompt.toLowerCase().trim();

        String greetingResponse = checkSimpleGreeting(preprocessedInput);
        if (greetingResponse != null) {
            return greetingResponse;
        }

        // Direct General ML Types Override for broad ML types queries
        if (isBroadMlTypesQuery(rawLower, preprocessedInput)) {
            conversationContext.setCurrentTopicEntity("Machine Learning");
            String mlTypesAns = TopicKnowledgeRepository.getIntentResponse("Machine Learning", IntentDetector.Intent.TYPES);
            if (mlTypesAns != null) {
                List<String> subTypes = extractSubTypesFromAnswer(mlTypesAns);
                conversationContext.updateContext("Machine Learning", userPrompt, mlTypesAns, subTypes, IntentDetector.Intent.TYPES);
                return mlTypesAns;
            }
        }

        // Resolve follow-up query and extract topic entity
        String resolvedQuery = conversationContext.resolveFollowUpQuery(userPrompt);
        String preprocessedResolved = TextPreprocessor.preprocess(resolvedQuery);

        String detectedEntity = detectExplicitTopicEntity(resolvedQuery);
        if (detectedEntity == null) {
            detectedEntity = detectExplicitTopicEntity(userPrompt);
        }
        if (detectedEntity != null) {
            conversationContext.setCurrentTopicEntity(detectedEntity);
        }

        IntentDetector.Intent userIntent = IntentDetector.detectIntent(resolvedQuery, preprocessedResolved);
        String activeTopic = conversationContext.getCurrentTopicEntity();

        if (TopicKnowledgeRepository.hasIntentResponse(activeTopic, userIntent)) {
            String structuredAnswer = TopicKnowledgeRepository.getIntentResponse(activeTopic, userIntent);
            if (structuredAnswer != null) {
                List<String> subTypes = extractSubTypesFromAnswer(structuredAnswer);
                conversationContext.updateContext(activeTopic, userPrompt, structuredAnswer, subTypes, userIntent);
                return structuredAnswer;
            }
        }

        return getOfflineResponse(userPrompt, resolvedQuery, userIntent);
    }

    /**
     * Core offline response generator using Topic + Intent logic, QuestionMatcher, and OfflineSynthesizer.
     */
    private String getOfflineResponse(String rawPrompt, String resolvedQuery, IntentDetector.Intent userIntent) {
        String activeTopic = conversationContext.getCurrentTopicEntity();

        if (TopicKnowledgeRepository.hasIntentResponse(activeTopic, userIntent)) {
            String structuredAnswer = TopicKnowledgeRepository.getIntentResponse(activeTopic, userIntent);
            if (structuredAnswer != null) {
                List<String> subTypes = extractSubTypesFromAnswer(structuredAnswer);
                conversationContext.updateContext(activeTopic, rawPrompt, structuredAnswer, subTypes, userIntent);
                return structuredAnswer;
            }
        }

        // Search best match in knowledge base
        QuestionMatcher.MatchResult matchResult = questionMatcher.findBestMatch(resolvedQuery, conversationContext);

        String preprocessed = TextPreprocessor.preprocess(resolvedQuery);
        if ((matchResult == null || matchResult.confidenceScore < 0.22) && !resolvedQuery.equalsIgnoreCase(preprocessed)) {
            matchResult = questionMatcher.findBestMatch(preprocessed, conversationContext);
        }

        if (matchResult != null && matchResult.entry != null && matchResult.confidenceScore >= 0.20) {
            MLKnowledgeBase.MLEntry entry = matchResult.entry;

            // Check if matched entry contradicts user intent
            boolean isContradictory = (userIntent == IntentDetector.Intent.TYPES || userIntent == IntentDetector.Intent.TYPES_DETAILED ||
                                      userIntent == IntentDetector.Intent.ADVANTAGES || userIntent == IntentDetector.Intent.DISADVANTAGES ||
                                      userIntent == IntentDetector.Intent.APPLICATIONS || userIntent == IntentDetector.Intent.EXAMPLES) &&
                                      (entry.question.toLowerCase().contains("what is") || entry.question.toLowerCase().contains("explain"));

            if (!isContradictory) {
                String entityToSet = detectExplicitTopicEntity(entry.question);
                if (entityToSet == null) {
                    entityToSet = entry.category;
                }

                List<String> subTypes = extractSubTypesFromAnswer(entry.answer);
                conversationContext.updateContext(entityToSet, rawPrompt, entry.answer, subTypes, userIntent);

                return entry.answer;
            }
        }

        // Synthesize offline response tailored to topic + intent
        String synthesized = offlineSynthesizer.synthesizeResponse(rawPrompt, resolvedQuery, conversationContext);
        List<String> subTypes = extractSubTypesFromAnswer(synthesized);
        conversationContext.updateContext(conversationContext.getCurrentTopicEntity(), rawPrompt, synthesized, subTypes, userIntent);

        return synthesized;
    }

    public String detectExplicitTopicEntity(String input) {
        if (input == null) return null;
        String raw = input.toLowerCase();
        String prep = TextPreprocessor.preprocess(input);
        String combined = (raw + " " + prep).toLowerCase();

        if (combined.contains("linear regression")) return "Linear Regression";
        if (combined.contains("multiple linear regression")) return "Multiple Linear Regression";
        if (combined.contains("polynomial regression")) return "Polynomial Regression";
        if (combined.contains("logistic regression")) return "Logistic Regression";
        if (combined.contains("k means") || combined.contains("kmeans")) return "K-Means Clustering";
        if (combined.contains("hierarchical clustering") || combined.contains("agglomerative")) return "Hierarchical Clustering";
        if (combined.contains("dbscan")) return "DBSCAN Clustering";
        if (combined.contains("supervised learning") || combined.contains("supervised ml")) return "Supervised Learning";
        if (combined.contains("unsupervised learning") || combined.contains("unsupervised ml")) return "Unsupervised Learning";
        if (combined.contains("semi supervised") || combined.contains("semi-supervised")) return "Semi-Supervised Learning";
        if (combined.contains("reinforcement learning") || combined.contains("reinforcement ml")) return "Reinforcement Learning";
        if (combined.contains("clustering")) return "Clustering";
        if (combined.contains("classification")) return "Classification";
        if (combined.contains("regression")) return "Regression";
        if (combined.contains("decision tree")) return "Decision Tree";
        if (combined.contains("random forest")) return "Random Forest";
        if (combined.contains("k nearest") || combined.contains("knn")) return "K-Nearest Neighbors";
        if (combined.contains("support vector") || combined.contains("svm")) return "Support Vector Machine";
        if (combined.contains("naive bayes")) return "Naive Bayes";
        if (combined.contains("overfitting")) return "Overfitting";
        if (combined.contains("underfitting")) return "Underfitting";
        if (combined.contains("neural network") || combined.contains("ann")) return "Artificial Neural Network";
        if (combined.contains("deep learning")) return "Deep Learning";
        if (combined.contains("convolutional neural network") || combined.contains("cnn")) return "Convolutional Neural Network";
        if (combined.contains("recurrent neural network") || combined.contains("rnn")) return "Recurrent Neural Network";
        if (combined.contains("principal component") || combined.contains("pca")) return "Principal Component Analysis";
        if (combined.contains("machine learning") || raw.matches(".*\\bml\\b.*") || combined.contains("artificial intelligence") || combined.contains("ai")) return "Machine Learning";

        return null;
    }

    private List<String> extractSubTypesFromAnswer(String answer) {
        List<String> subTypes = new ArrayList<>();
        if (answer == null) return subTypes;

        String[] lines = answer.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.matches("^(\\d+\\.|•|\\-).*")) {
                String cleaned = line.replaceAll("^(\\d+\\.|•|\\-)\\s*", "");
                int colonIdx = cleaned.indexOf(":");
                if (colonIdx != -1) {
                    cleaned = cleaned.substring(0, colonIdx).trim();
                }
                cleaned = cleaned.replaceAll("\\*\\*", "").trim();
                if (!cleaned.isEmpty() && cleaned.length() < 40) {
                    subTypes.add(cleaned);
                }
            }
        }
        return subTypes;
    }

    /**
     * Processes OCR extracted text from an attached image along with user prompt.
     */
    public void getResponseForImageAsync(String extractedText, String userPrompt, ChatCallback callback) {
        if (extractedText == null || extractedText.trim().isEmpty()) {
            if (userPrompt != null && !userPrompt.trim().isEmpty()) {
                getResponseAsync(userPrompt, responseText ->
                        callback.onResponse(responseText + "\n\n(Note: Could not extract readable text from attached image using offline OCR.)"));
            } else {
                callback.onResponse("Could not extract readable text or fully interpret visual content from this image. Please ensure the image contains clear text, ML diagrams, or code snippets.");
            }
            return;
        }

        String cleanExtracted = extractedText.trim();
        StringBuilder prefix = new StringBuilder();
        prefix.append("📷 [OCR Extracted Text]:\n\"")
                .append(cleanExtracted.length() > 200 ? cleanExtracted.substring(0, 200) + "..." : cleanExtracted)
                .append("\"\n\n💡 [Explanation]:\n");

        String queryToMatch;
        if (userPrompt != null && !userPrompt.trim().isEmpty() &&
                !userPrompt.toLowerCase().contains("explain this") &&
                !userPrompt.toLowerCase().contains("what is this")) {
            queryToMatch = userPrompt + "\nImage Text: " + cleanExtracted;
        } else {
            queryToMatch = "Explain the following ML text or diagram extracted from image: " + cleanExtracted;
        }

        getResponseAsync(queryToMatch, responseText -> callback.onResponse(prefix.toString() + responseText));
    }

    public String getResponseForImage(String extractedText, String userPrompt) {
        if (extractedText == null || extractedText.trim().isEmpty()) {
            if (userPrompt != null && !userPrompt.trim().isEmpty()) {
                return getResponse(userPrompt) + "\n\n(Note: Could not extract readable text from attached image using offline OCR.)";
            } else {
                return "Could not extract readable text or fully interpret visual content from this image. Please ensure the image contains clear text, ML diagrams, or code snippets.";
            }
        }

        String cleanExtracted = extractedText.trim();
        String queryToMatch = (userPrompt != null && !userPrompt.trim().isEmpty()) ? userPrompt + " " + cleanExtracted : cleanExtracted;

        return "📷 [OCR Extracted Text]:\n\"" +
                (cleanExtracted.length() > 200 ? cleanExtracted.substring(0, 200) + "..." : cleanExtracted) +
                "\"\n\n💡 [Explanation]:\n" + getResponse(queryToMatch);
    }
}
