package com.vaishnavi.chatbot;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks conversation state, current topic entity, previous topic entity,
 * user intent, extracted sub-types/numbered lists, and multi-turn message history for context resolution.
 */
public class ConversationContext {

    public static class ChatTurn {
        public String userMessage;
        public String botResponse;
        public IntentDetector.Intent intent;

        public ChatTurn(String userMessage, String botResponse, IntentDetector.Intent intent) {
            this.userMessage = userMessage;
            this.botResponse = botResponse;
            this.intent = intent;
        }
    }

    private String currentTopicEntity = null;
    private String previousTopicEntity = null;
    private String lastQuestion = null;
    private String lastAnswer = null;
    private IntentDetector.Intent lastIntent = IntentDetector.Intent.GENERAL;
    private List<String> currentSubTypes = new ArrayList<>();
    private final List<ChatTurn> history = new ArrayList<>();

    public synchronized void updateContext(String topicEntity, String question, String answer, List<String> subTypes, IntentDetector.Intent intent) {
        if (topicEntity != null && !topicEntity.trim().isEmpty()) {
            if (currentTopicEntity != null && !currentTopicEntity.equalsIgnoreCase(topicEntity)) {
                previousTopicEntity = currentTopicEntity;
            }
            currentTopicEntity = topicEntity;
        }

        if (question != null) {
            lastQuestion = question;
        }

        if (answer != null) {
            lastAnswer = answer;
        }

        if (intent != null) {
            lastIntent = intent;
        }

        if (subTypes != null && !subTypes.isEmpty()) {
            currentSubTypes = new ArrayList<>(subTypes);
        }

        if (question != null && answer != null) {
            history.add(new ChatTurn(question, answer, lastIntent));
            if (history.size() > 10) {
                history.remove(0); // keep recent 10 turns
            }
        }
    }

    public synchronized void updateContext(String topicEntity, String question, String answer, List<String> subTypes) {
        updateContext(topicEntity, question, answer, subTypes, IntentDetector.Intent.GENERAL);
    }

    public synchronized String getCurrentTopicEntity() {
        return currentTopicEntity;
    }

    public synchronized String getPreviousTopicEntity() {
        return previousTopicEntity;
    }

    public synchronized String getLastQuestion() {
        return lastQuestion;
    }

    public synchronized String getLastAnswer() {
        return lastAnswer;
    }

    public synchronized IntentDetector.Intent getLastIntent() {
        return lastIntent;
    }

    public synchronized List<String> getCurrentSubTypes() {
        return currentSubTypes;
    }

    public synchronized List<ChatTurn> getHistory() {
        return new ArrayList<>(history);
    }

    public synchronized void setCurrentTopicEntity(String entity) {
        if (this.currentTopicEntity != null && !this.currentTopicEntity.equalsIgnoreCase(entity)) {
            this.previousTopicEntity = this.currentTopicEntity;
        }
        this.currentTopicEntity = entity;
    }

    public synchronized void setCurrentSubTypes(List<String> subTypes) {
        this.currentSubTypes = subTypes != null ? new ArrayList<>(subTypes) : new ArrayList<>();
    }

    public synchronized void setLastIntent(IntentDetector.Intent intent) {
        this.lastIntent = intent;
    }

    public synchronized void clear() {
        currentTopicEntity = null;
        previousTopicEntity = null;
        lastQuestion = null;
        lastAnswer = null;
        lastIntent = IntentDetector.Intent.GENERAL;
        currentSubTypes.clear();
        history.clear();
    }

    /**
     * Resolves pronouns, relative references ("first one", "second one"), and short aspect questions
     * using active conversation context.
     */
    public synchronized String resolveFollowUpQuery(String rawPrompt) {
        if (rawPrompt == null || rawPrompt.trim().isEmpty()) return rawPrompt;

        String cleanPrompt = rawPrompt.replaceAll("[^a-zA-Z0-9\\s\\-]", "").trim();
        String lower = cleanPrompt.toLowerCase();

        // 1. Ordinal References ("first one", "second one", "3rd type", "pahila", "dusra")
        if (lower.contains("first one") || lower.contains("first type") || lower.contains("1st one") || lower.contains("pahila")) {
            if (!currentSubTypes.isEmpty()) {
                String sub = currentSubTypes.get(0);
                setCurrentTopicEntity(sub);
                if (lower.contains("advantage") || lower.contains("benefit")) {
                    return "What are the advantages of " + sub;
                }
                if (lower.contains("disadvantage") || lower.contains("limitation")) {
                    return "What are the disadvantages of " + sub;
                }
                if (lower.contains("example")) {
                    return "Give a real life example of " + sub;
                }
                return "Explain " + sub;
            }
        }

        if (lower.contains("second one") || lower.contains("second type") || lower.contains("2nd one") || lower.contains("dusra")) {
            if (currentSubTypes.size() >= 2) {
                String sub = currentSubTypes.get(1);
                setCurrentTopicEntity(sub);
                if (lower.contains("advantage") || lower.contains("benefit")) {
                    return "What are the advantages of " + sub;
                }
                if (lower.contains("disadvantage") || lower.contains("limitation")) {
                    return "What are the disadvantages of " + sub;
                }
                if (lower.contains("example")) {
                    return "Give a real life example of " + sub;
                }
                return "Explain " + sub;
            }
        }

        if (lower.contains("third one") || lower.contains("third type") || lower.contains("3rd one") || lower.contains("tisra")) {
            if (currentSubTypes.size() >= 3) {
                String sub = currentSubTypes.get(2);
                setCurrentTopicEntity(sub);
                return "Explain " + sub;
            }
        }

        String activeTopic = currentTopicEntity;
        if (activeTopic != null && !activeTopic.trim().isEmpty()) {

            // 2. Short aspect / follow-up questions
            if (lower.matches("^(types|kinds|categories|che types|types sang|types kontte|prakar)$") ||
                    lower.contains("its types") || lower.contains("what are its types") || lower.contains("types of it") || lower.contains("tyache types")) {
                if (lower.contains("detail") || lower.contains("deep") || lower.contains("thorough") || lower.contains("sarkhe")) {
                    return "Explain the types of " + activeTopic + " in detail";
                }
                return "What are the types of " + activeTopic;
            }

            if (lower.matches("^(advantages|benefits|uses|advantages kay|tyache advantages)$") ||
                    lower.contains("its advantages") || lower.contains("what are its advantages") || lower.contains("advantages of it")) {
                return "What are the advantages of " + activeTopic;
            }

            if (lower.matches("^(disadvantages|limitations|drawbacks|disadvantages kay)$") ||
                    lower.contains("its disadvantages") || lower.contains("what are its disadvantages") || lower.contains("limitations of it")) {
                return "What are the disadvantages of " + activeTopic;
            }

            if (lower.matches("^(how|working|steps|kasa work karto)$") ||
                    lower.contains("how does it work") || lower.contains("its working") || lower.contains("how it works")) {
                return "How does " + activeTopic + " work";
            }

            if (lower.matches("^(applications|examples|use cases|real-life example|real life example)$") ||
                    lower.contains("real life example") || lower.contains("real-life example") || lower.contains("its applications") || lower.contains("examples of it")) {
                return "What are real life examples and applications of " + activeTopic;
            }

            if (lower.matches("^(algorithms|methods|which algorithms)$") ||
                    lower.contains("which algorithm") || lower.contains("algorithms used") || lower.contains("which one") || lower.contains("commonly used")) {
                return "Which algorithms are used for " + activeTopic;
            }

            if (lower.contains("how to reduce it") || lower.contains("how can we reduce it") || lower.contains("how to fix it") || lower.contains("how to prevent it")) {
                return "How to prevent or reduce " + activeTopic;
            }

            if (lower.contains("formula") || lower.contains("equation") || lower.contains("math")) {
                return "What is the formula or mathematical equation for " + activeTopic;
            }

            if (lower.contains("code") || lower.contains("python") || lower.contains("implementation")) {
                return "Provide Python implementation code for " + activeTopic;
            }

            // 3. Pronoun replacements
            if (lower.contains("it") || lower.contains("its") || lower.contains("this") || lower.contains("that") ||
                lower.contains("ha") || lower.contains("hi") || lower.contains("he") || lower.contains("tyache")) {
                return rawPrompt.replaceAll("(?i)\\b(it|its|this|that|these|those|ha|hi|he|tyache)\\b", activeTopic);
            }

            // If prompt is short (< 4 words) and contains no explicit topic, append active topic
            if (cleanPrompt.split("\\s+").length <= 3 && !lower.contains("what") && !lower.contains("explain") && !lower.contains("how") &&
                !lower.contains("types") && !lower.contains("regression") && !lower.contains("classification") && !lower.contains("clustering") &&
                !lower.contains("supervised") && !lower.contains("unsupervised") && !lower.contains("reinforcement") && !lower.contains("overfitting")) {
                return rawPrompt + " " + activeTopic;
            }
        }

        return rawPrompt;
    }

    /**
     * Builds a summary context string for LLM or generator.
     */
    public synchronized String buildContextSummary() {
        StringBuilder sb = new StringBuilder();
        if (currentTopicEntity != null) {
            sb.append("Current Topic: ").append(currentTopicEntity).append("\n");
        }
        if (previousTopicEntity != null) {
            sb.append("Previous Topic: ").append(previousTopicEntity).append("\n");
        }
        if (lastIntent != null) {
            sb.append("Active User Intent: ").append(lastIntent.name()).append("\n");
        }
        if (!currentSubTypes.isEmpty()) {
            sb.append("Recent Subtopics/Types: ").append(String.join(", ", currentSubTypes)).append("\n");
        }
        if (!history.isEmpty()) {
            sb.append("Recent Conversation History:\n");
            for (ChatTurn turn : history) {
                sb.append("User: ").append(turn.userMessage).append(" (Intent: ").append(turn.intent != null ? turn.intent.name() : "GENERAL").append(")\n");
                sb.append("Bot: ").append(turn.botResponse.length() > 100 ? turn.botResponse.substring(0, 100) + "..." : turn.botResponse).append("\n");
            }
        }
        return sb.toString();
    }
}
