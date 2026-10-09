package com.vaishnavi.chatbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Question matching engine combining TF-IDF Cosine Similarity, Jaccard Similarity,
 * Levenshtein fuzzy string matching, Keyword Overlap scoring, and Context Boosting.
 */
public class QuestionMatcher {

    public static class MatchResult {
        public MLKnowledgeBase.MLEntry entry;
        public double confidenceScore;

        public MatchResult(MLKnowledgeBase.MLEntry entry, double confidenceScore) {
            this.entry = entry;
            this.confidenceScore = confidenceScore;
        }
    }

    private final MLKnowledgeBase knowledgeBase;
    private final Map<String, Map<String, Double>> tfIdfVectors = new HashMap<>();
    private final Map<String, Double> idfMap = new HashMap<>();

    public QuestionMatcher(MLKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
        buildTfIdfIndex();
    }

    private void buildTfIdfIndex() {
        List<MLKnowledgeBase.MLEntry> entries = knowledgeBase.getEntries();
        int numDocs = entries.size();
        if (numDocs == 0) return;

        Map<String, Integer> docFrequency = new HashMap<>();

        for (MLKnowledgeBase.MLEntry entry : entries) {
            Set<String> uniqueTokens = new HashSet<>();
            for (String q : entry.relatedQuestions) {
                uniqueTokens.addAll(TextPreprocessor.tokenize(TextPreprocessor.preprocess(q)));
            }

            for (String token : uniqueTokens) {
                docFrequency.put(token, docFrequency.getOrDefault(token, 0) + 1);
            }
        }

        for (Map.Entry<String, Integer> e : docFrequency.entrySet()) {
            double idf = Math.log(1.0 + ((double) numDocs / (1.0 + e.getValue())));
            idfMap.put(e.getKey(), idf);
        }

        for (MLKnowledgeBase.MLEntry entry : entries) {
            Map<String, Double> vector = new HashMap<>();
            List<String> tokens = new ArrayList<>();
            for (String q : entry.relatedQuestions) {
                tokens.addAll(TextPreprocessor.tokenize(TextPreprocessor.preprocess(q)));
            }

            int totalTokens = tokens.size();
            if (totalTokens == 0) continue;

            Map<String, Integer> termFreq = new HashMap<>();
            for (String token : tokens) {
                termFreq.put(token, termFreq.getOrDefault(token, 0) + 1);
            }

            for (Map.Entry<String, Integer> tfEntry : termFreq.entrySet()) {
                String term = tfEntry.getKey();
                double tf = (double) tfEntry.getValue() / totalTokens;
                double idf = idfMap.getOrDefault(term, 0.0);
                vector.put(term, tf * idf);
            }

            tfIdfVectors.put(entry.question, vector);
        }
    }

    public MatchResult findBestMatch(String rawUserPrompt) {
        return findBestMatch(rawUserPrompt, null);
    }

    public MatchResult findBestMatch(String rawUserPrompt, ConversationContext context) {
        if (rawUserPrompt == null || rawUserPrompt.trim().isEmpty()) {
            return new MatchResult(null, 0.0);
        }

        String preprocessedInput = TextPreprocessor.preprocess(rawUserPrompt);
        String rawClean = rawUserPrompt.replaceAll("[^a-zA-Z0-9\\s\\-]", "").toLowerCase().trim();

        // 1. Exact Match Checks
        if (knowledgeBase.getExactMatchMap().containsKey(preprocessedInput)) {
            return new MatchResult(knowledgeBase.getExactMatchMap().get(preprocessedInput), 1.0);
        }
        if (knowledgeBase.getExactMatchMap().containsKey(rawClean)) {
            return new MatchResult(knowledgeBase.getExactMatchMap().get(rawClean), 1.0);
        }

        List<String> inputTokensList = TextPreprocessor.tokenize(preprocessedInput);
        Set<String> inputTokensSet = new HashSet<>(inputTokensList);

        if (inputTokensList.isEmpty()) {
            return new MatchResult(null, 0.0);
        }

        // Build input TF-IDF vector
        Map<String, Double> inputVector = new HashMap<>();
        int inputLength = inputTokensList.size();
        Map<String, Integer> inputTfMap = new HashMap<>();

        for (String t : inputTokensList) {
            inputTfMap.put(t, inputTfMap.getOrDefault(t, 0) + 1);
        }

        for (Map.Entry<String, Integer> e : inputTfMap.entrySet()) {
            String term = e.getKey();
            double tf = (double) e.getValue() / inputLength;
            double idf = idfMap.getOrDefault(term, 0.5);
            inputVector.put(term, tf * idf);
        }

        IntentDetector.Intent userIntent = IntentDetector.detectIntent(rawUserPrompt, preprocessedInput);
        String promptSubtopic = extractSpecificSubtopic(rawClean, preprocessedInput);
        String activeTopic = context != null ? context.getCurrentTopicEntity() : null;

        MLKnowledgeBase.MLEntry bestEntry = null;
        double highestScore = 0.0;

        for (MLKnowledgeBase.MLEntry entry : knowledgeBase.getEntries()) {
            double score = calculateCompositeScore(
                    preprocessedInput, rawClean, inputTokensSet, inputVector,
                    entry, activeTopic, promptSubtopic, userIntent
            );
            if (score > highestScore) {
                highestScore = score;
                bestEntry = entry;
            }
        }

        // Apply confidence threshold
        if (highestScore < 0.25) {
            return new MatchResult(null, highestScore);
        }

        return new MatchResult(bestEntry, highestScore);
    }

    private String extractSpecificSubtopic(String rawClean, String preprocessed) {
        String combined = (rawClean + " " + preprocessed).toLowerCase();
        if (combined.contains("k-means") || combined.contains("kmeans") || combined.contains("k means")) return "kmeans";
        if (combined.contains("dbscan")) return "dbscan";
        if (combined.contains("clustering")) return "clustering";
        if (combined.contains("regression")) return "regression";
        if (combined.contains("classification")) return "classification";
        if (combined.contains("supervised learning") || combined.contains("supervised")) return "supervised";
        if (combined.contains("unsupervised learning") || combined.contains("unsupervised")) return "unsupervised";
        if (combined.contains("reinforcement learning") || combined.contains("reinforcement")) return "reinforcement";
        if (combined.contains("decision tree")) return "decision tree";
        if (combined.contains("random forest")) return "random forest";
        if (combined.contains("support vector") || combined.contains("svm")) return "svm";
        if (combined.contains("knn") || combined.contains("k nearest")) return "knn";
        if (combined.contains("naive bayes")) return "naive bayes";
        if (combined.contains("pca") || combined.contains("principal component")) return "pca";
        if (combined.contains("overfitting")) return "overfitting";
        return null;
    }

    private double calculateCompositeScore(String cleanInput, String rawClean, Set<String> inputTokens,
                                           Map<String, Double> inputVector, MLKnowledgeBase.MLEntry entry,
                                           String activeTopic, String promptSubtopic, IntentDetector.Intent userIntent) {

        // 1. Cosine Similarity
        double cosineSim = 0.0;
        Map<String, Double> docVector = tfIdfVectors.get(entry.question);
        if (docVector != null) {
            cosineSim = calculateCosineSimilarity(inputVector, docVector);
        }

        // 2. Sample questions max Jaccard & Levenshtein
        double maxJaccard = 0.0;
        double maxLevenshtein = 0.0;

        for (String relQ : entry.relatedQuestions) {
            String cleanRelQ = TextPreprocessor.preprocess(relQ);
            Set<String> relTokens = new HashSet<>(TextPreprocessor.tokenize(cleanRelQ));

            double jaccard = calculateJaccardSimilarity(inputTokens, relTokens);
            double lev = calculateLevenshteinSimilarity(cleanInput, cleanRelQ);

            if (jaccard > maxJaccard) maxJaccard = jaccard;
            if (lev > maxLevenshtein) maxLevenshtein = lev;
        }

        // 3. Keyword Match Score
        int kwMatches = 0;
        for (String kw : entry.keywords) {
            String cleanKw = TextPreprocessor.preprocess(kw);
            if (!cleanKw.isEmpty() && (inputTokens.contains(cleanKw) || cleanInput.contains(cleanKw) || rawClean.contains(cleanKw))) {
                kwMatches++;
            }
        }

        double kwScore = entry.keywords.isEmpty() ? 0.0 : (double) kwMatches / entry.keywords.size();

        // Substring bonus
        double substringBonus = 0.0;
        for (String relQ : entry.relatedQuestions) {
            String cleanRelQ = TextPreprocessor.preprocess(relQ);
            if (!cleanRelQ.isEmpty() && !cleanInput.isEmpty() &&
                    (cleanInput.contains(cleanRelQ) || cleanRelQ.contains(cleanInput))) {
                substringBonus = 0.20;
                break;
            }
        }

        // Context Boost
        double contextBoost = 0.0;
        if (activeTopic != null && !activeTopic.trim().isEmpty()) {
            String cleanTopic = TextPreprocessor.preprocess(activeTopic);
            if (entry.question.toLowerCase().contains(cleanTopic.toLowerCase()) ||
                entry.category.toLowerCase().contains(cleanTopic.toLowerCase())) {
                contextBoost = 0.15;
            }
        }

        // Intent Alignment
        double intentScore = 0.0;
        boolean entryIsTypes = entry.question.toLowerCase().contains("types") || "ml_types".equalsIgnoreCase(entry.intent) || "types".equalsIgnoreCase(entry.intent);
        if (userIntent == IntentDetector.Intent.TYPES || userIntent == IntentDetector.Intent.TYPES_DETAILED) {
            if (entryIsTypes) {
                intentScore = 0.20;
            } else {
                intentScore = -0.15;
            }
        }

        // Subtopic Alignment & Mismatch Penalty
        double subtopicPenalty = 0.0;
        String entrySubtopic = extractSpecificSubtopic(entry.question.toLowerCase(), entry.category.toLowerCase() + " " + String.join(" ", entry.keywords));

        if (promptSubtopic != null) {
            if (entrySubtopic != null && entrySubtopic.equalsIgnoreCase(promptSubtopic)) {
                subtopicPenalty += 0.25; // Matching specific subtopic
            } else if (entrySubtopic != null && !entrySubtopic.equalsIgnoreCase(promptSubtopic)) {
                subtopicPenalty -= 0.40; // Penalty for wrong subtopic
            } else if (entrySubtopic == null && "machine_learning".equalsIgnoreCase(entry.topic)) {
                subtopicPenalty -= 0.20; // Specific query requested, don't pick general entry
            }
        } else {
            // Prompt is general (e.g. "types of ml"), check if entry is about a specific subtopic (e.g. clustering)
            if (entrySubtopic != null) {
                subtopicPenalty -= 0.45; // Heavy penalty for matching specific subtopic when general query asked!
            }
        }

        // Weighted Composite Score
        double compositeScore = (0.30 * cosineSim) + (0.25 * maxJaccard) + (0.15 * maxLevenshtein) + (0.15 * kwScore) + substringBonus + contextBoost + intentScore + subtopicPenalty;

        return Math.max(0.0, Math.min(1.0, compositeScore));
    }

    private double calculateCosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (double val : v1.values()) {
            norm1 += val * val;
        }

        for (Map.Entry<String, Double> entry : v2.entrySet()) {
            String key = entry.getKey();
            double val = entry.getValue();
            norm2 += val * val;

            if (v1.containsKey(key)) {
                dotProduct += v1.get(key) * val;
            }
        }

        if (norm1 == 0.0 || norm2 == 0.0) return 0.0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty()) return 1.0;
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    private double calculateLevenshteinSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) dp[i][0] = i;
        for (int j = 0; j <= len2; j++) dp[0][j] = j;

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        int distance = dp[len1][len2];
        int maxLength = Math.max(len1, len2);
        return 1.0 - ((double) distance / maxLength);
    }
}
