package com.vaishnavi.chatbot;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Knowledge Base repository for Machine Learning Q&A entries.
 * Loads entries from res/raw/ml_knowledge_base.csv.
 */
public class MLKnowledgeBase {

    public static class MLEntry {
        public String category;
        public String question;
        public String answer;
        public List<String> keywords;
        public List<String> relatedQuestions;
        public String topic;
        public String intent;

        public MLEntry(String category, String question, String answer, List<String> keywords, List<String> relatedQuestions) {
            this(category, question, answer, keywords, relatedQuestions, inferTopic(question, category), inferIntent(question));
        }

        public MLEntry(String category, String question, String answer, List<String> keywords, List<String> relatedQuestions, String topic, String intent) {
            this.category = category;
            this.question = question;
            this.answer = answer;
            this.keywords = keywords;
            this.relatedQuestions = relatedQuestions;
            this.topic = topic;
            this.intent = intent;
        }

        private static String inferTopic(String question, String category) {
            String q = question.toLowerCase();
            if (q.contains("machine learning") || q.contains("ml")) return "machine_learning";
            if (q.contains("clustering")) return "clustering";
            if (q.contains("regression")) return "regression";
            if (q.contains("classification")) return "classification";
            if (q.contains("supervised learning")) return "supervised_learning";
            if (q.contains("unsupervised learning")) return "unsupervised_learning";
            if (q.contains("reinforcement learning")) return "reinforcement_learning";
            if (q.contains("k-means") || q.contains("kmeans")) return "kmeans";
            if (q.contains("dbscan")) return "dbscan";
            return category.toLowerCase().replaceAll("\\s+", "_");
        }

        private static String inferIntent(String question) {
            String q = question.toLowerCase();
            if (q.contains("types of machine learning") || q.contains("types of ml")) return "ml_types";
            if (q.contains("types")) return "types";
            if (q.contains("advantage") || q.contains("disadvantage")) return "pros_cons";
            if (q.contains("what is") || q.contains("define") || q.contains("explain")) return "definition";
            return "general";
        }
    }

    private final List<MLEntry> entries = new ArrayList<>();
    private final Map<String, MLEntry> exactMatchMap = new HashMap<>();

    public MLKnowledgeBase(Context context) {
        loadFromCsv(context);
    }

    public List<MLEntry> getEntries() {
        return entries;
    }

    public Map<String, MLEntry> getExactMatchMap() {
        return exactMatchMap;
    }

    private void loadFromCsv(Context context) {
        try {
            InputStream inputStream = null;
            if (context != null) {
                inputStream = context.getResources().openRawResource(R.raw.ml_knowledge_base);
            } else {
                inputStream = getClass().getClassLoader().getResourceAsStream("raw/ml_knowledge_base.csv");
                if (inputStream == null) {
                    inputStream = getClass().getResourceAsStream("/raw/ml_knowledge_base.csv");
                }
                if (inputStream == null) {
                    inputStream = getClass().getClassLoader().getResourceAsStream("res/raw/ml_knowledge_base.csv");
                }
                if (inputStream == null) {
                    inputStream = getClass().getResourceAsStream("/res/raw/ml_knowledge_base.csv");
                }

                if (inputStream == null) {
                    File currentDir = new File(".").getAbsoluteFile();
                    for (int i = 0; i < 5 && currentDir != null; i++) {
                        File candidate1 = new File(currentDir, "app/src/main/res/raw/ml_knowledge_base.csv");
                        File candidate2 = new File(currentDir, "src/main/res/raw/ml_knowledge_base.csv");
                        if (candidate1.exists()) {
                            inputStream = new FileInputStream(candidate1);
                            break;
                        } else if (candidate2.exists()) {
                            inputStream = new FileInputStream(candidate2);
                            break;
                        }
                        currentDir = currentDir.getParentFile();
                    }
                }
            }

            if (inputStream == null) return;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                boolean isHeader = true;

                while ((line = reader.readLine()) != null) {
                    line = line.replace("\uFEFF", "").trim();
                    if (line.isEmpty()) continue;

                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }

                    List<String> columns = parseCsvLine(line);
                    if (columns.size() >= 3) {
                        String category = columns.get(0).trim();
                        String question = columns.get(1).trim();
                        String answer = columns.get(2).trim();

                        String keywordsStr = columns.size() >= 4 ? columns.get(3).trim() : "";
                        String relatedStr = columns.size() >= 5 ? columns.get(4).trim() : "";

                        List<String> keywords = new ArrayList<>(Arrays.asList(keywordsStr.split(";")));
                        List<String> related = new ArrayList<>(Arrays.asList(relatedStr.split(";")));

                        related.add(question); // Include canonical question as a related question

                        MLEntry entry = new MLEntry(category, question, answer, keywords, related);
                        entries.add(entry);

                        for (String q : related) {
                            String cleanQ = TextPreprocessor.preprocess(q);
                            if (!cleanQ.isEmpty()) {
                                exactMatchMap.put(cleanQ, entry);
                            }
                            String rawClean = q.replaceAll("[^a-zA-Z0-9\\s\\-]", "").toLowerCase().trim();
                            if (!rawClean.isEmpty()) {
                                exactMatchMap.put(rawClean, entry);
                            }
                        }

                        String lowerQ = question.toLowerCase();
                        if (lowerQ.contains("types of machine learning") || lowerQ.contains("types of ml")) {
                            exactMatchMap.put("types of ml", entry);
                            exactMatchMap.put("types of machine learning", entry);
                            exactMatchMap.put("ml types", entry);
                            exactMatchMap.put("machine learning types", entry);
                            exactMatchMap.put("what are types of ml", entry);
                            exactMatchMap.put("ml che types", entry);
                            exactMatchMap.put("machine learning che types", entry);
                            exactMatchMap.put("machine learning che prakar", entry);
                            exactMatchMap.put("ml che prakar", entry);
                            exactMatchMap.put("types sang", entry);
                            exactMatchMap.put("types machine learning", entry);
                        } else if (lowerQ.contains("k-means")) {
                            exactMatchMap.put("what is k means", entry);
                            exactMatchMap.put("what is kmeans", entry);
                            exactMatchMap.put("k means", entry);
                            exactMatchMap.put("kmeans", entry);
                        } else if (lowerQ.contains("dbscan")) {
                            exactMatchMap.put("what is dbscan", entry);
                            exactMatchMap.put("dbscan", entry);
                        } else if (lowerQ.contains("overfitting")) {
                            exactMatchMap.put("what is overfitting", entry);
                            exactMatchMap.put("overfitting", entry);
                        } else if (lowerQ.contains("what is machine learning")) {
                            exactMatchMap.put("what is machine learning", entry);
                            exactMatchMap.put("what is ml", entry);
                            exactMatchMap.put("machine learning", entry);
                        } else if (lowerQ.contains("types of clustering")) {
                            exactMatchMap.put("types of clustering", entry);
                            exactMatchMap.put("clustering types", entry);
                        } else if (lowerQ.contains("types of regression")) {
                            exactMatchMap.put("types of regression", entry);
                            exactMatchMap.put("regression types", entry);
                        } else if (lowerQ.contains("types of supervised")) {
                            exactMatchMap.put("types of supervised learning", entry);
                            exactMatchMap.put("supervised learning types", entry);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        if (line == null || line.isEmpty()) return result;

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result;
    }
}
