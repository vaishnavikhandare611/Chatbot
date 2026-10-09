package com.vaishnavi.chatbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Advanced Text Preprocessor for Machine Learning NLP Engine.
 * Handles spelling correction, acronym expansion, Marathi-English/Hinglish phrase mapping,
 * tokenization, and stop-word filtering.
 */
public class TextPreprocessor {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",
            "in", "on", "at", "to", "for", "from", "with", "by", "of", "and", "or",
            "as", "it", "its", "this", "that", "these", "those"
    ));

    public static String preprocess(String input) {
        if (input == null) return "";

        // 1. Lowercase
        String text = input.toLowerCase();

        // 2. Normalize Marathi & Hinglish phrase patterns before character stripping
        text = text.replaceAll("(mhnje kay|mnje kay|mhanje kay|kay ahe|kay aahe|kay h)", " what is ");
        text = text.replaceAll("(explain kar|samjhav|sanga|sang|saang)", " explain ");
        text = text.replaceAll("(mala sang|sanga mala)", " tell me ");
        text = text.replaceAll("(kasa work karto|kasa kaam karto|kasa chalte|kasa chalat)", " how does it work ");
        text = text.replaceAll("(kasa ahe|kasa aahe)", " how is ");
        text = text.replaceAll("(che types|chya types|che prakar|chya prakar|prakar sang|types sang|prakar kontte|types kontte|kontte types|kontte prakar)", " types of ");
        text = text.replaceAll("(prakar|kinds|categories)", " types ");
        text = text.replaceAll("(che advantages|chya advantages|advantages kay|tyache advantages)", " advantages ");
        text = text.replaceAll("(che disadvantages|chya disadvantages|disadvantages kay|tyache disadvantages)", " disadvantages ");
        text = text.replaceAll("(madhla difference kay|madhe kay difference|madhe kaay difference|difference sang)", " difference between ");
        text = text.replaceAll("(ka use kartat|ka vapartat|use ka kartat)", " why is it used ");
        text = text.replaceAll("(ka hota|ka hote)", " why does it happen ");
        text = text.replaceAll("(kashi vadhvaychi|kashi vadhva)", " how to improve ");
        text = text.replaceAll("(konti algorithm best ahe|konti best ahe)", " which algorithm is best ");

        // 3. Remove punctuation except dashes
        text = text.replaceAll("[^a-z0-9\\s\\-]", " ");

        // 4. Normalize whitespace
        text = text.replaceAll("\\s+", " ").trim();

        // 5. Map spelling errors, acronyms, and single tokens
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();

        for (String w : words) {
            // Spelling Correction
            if (w.equals("clustring") || w.equals("clusterin") || w.equals("clustrin")) {
                sb.append("clustering ");
            } else if (w.equals("regresion") || w.equals("regresn")) {
                sb.append("regression ");
            } else if (w.equals("classifcation") || w.equals("classifictn")) {
                sb.append("classification ");
            } else if (w.equals("algorithem") || w.equals("algoritham") || w.equals("algoritm")) {
                sb.append("algorithm ");
            } else if (w.equals("advntages") || w.equals("advantags") || w.equals("advatages")) {
                sb.append("advantages ");
            } else if (w.equals("disadvntages") || w.equals("disadvantags")) {
                sb.append("disadvantages ");
            } else if (w.equals("supervisd")) {
                sb.append("supervised ");
            } else if (w.equals("unsupervisd")) {
                sb.append("unsupervised ");
            } else if (w.equals("overfiting") || w.equals("overfitin")) {
                sb.append("overfitting ");
            } else if (w.equals("underfiting") || w.equals("underfitin")) {
                sb.append("underfitting ");
            } else if (w.equals("precison")) {
                sb.append("precision ");
            } else if (w.equals("recal")) {
                sb.append("recall ");
            } else if (w.equals("hyperparamter") || w.equals("hyperparmeter")) {
                sb.append("hyperparameter ");
            } else if (w.equals("crossvalidtn")) {
                sb.append("cross validation ");
            } else if (w.equals("regularisatn") || w.equals("regularisaton")) {
                sb.append("regularization ");
            }
            // Acronym Expansion
            else if (w.equals("ml")) {
                sb.append("machine learning ");
            } else if (w.equals("ai")) {
                sb.append("artificial intelligence ");
            } else if (w.equals("dl")) {
                sb.append("deep learning ");
            } else if (w.equals("svm")) {
                sb.append("support vector machine ");
            } else if (w.equals("knn")) {
                sb.append("k nearest neighbors ");
            } else if (w.equals("cnn")) {
                sb.append("convolutional neural network ");
            } else if (w.equals("rnn")) {
                sb.append("recurrent neural network ");
            } else if (w.equals("lstm")) {
                sb.append("long short term memory ");
            } else if (w.equals("pca")) {
                sb.append("principal component analysis ");
            } else if (w.equals("smote")) {
                sb.append("synthetic minority oversampling technique ");
            } else if (w.equals("mse")) {
                sb.append("mean squared error ");
            } else if (w.equals("rmse")) {
                sb.append("root mean squared error ");
            } else if (w.equals("mae")) {
                sb.append("mean absolute error ");
            } else if (w.equals("rf")) {
                sb.append("random forest ");
            } else if (w.equals("xgboost")) {
                sb.append("extreme gradient boosting ");
            } else if (w.equals("ann")) {
                sb.append("artificial neural network ");
            } else if (w.equals("nlp")) {
                sb.append("natural language processing ");
            } else if (w.equals("cv")) {
                sb.append("computer vision ");
            } else if (w.equals("auc")) {
                sb.append("area under curve ");
            } else if (w.equals("roc")) {
                sb.append("receiver operating characteristic ");
            } else if (w.equals("gmm")) {
                sb.append("gaussian mixture models ");
            } else if (w.equals("dbscan")) {
                sb.append("density based spatial clustering of applications with noise ");
            } else if (w.equals("r")) {
                sb.append("are ");
            } else if (w.equals("u")) {
                sb.append("you ");
            } else if (w.equals("ur")) {
                sb.append("your ");
            } else {
                sb.append(w).append(" ");
            }
        }

        text = sb.toString().trim();

        // 6. Remove question filler prefixes
        text = text.replaceAll("^(can you tell me|please tell me|tell me about|what do you know about|do you know|what is meant by|meaning of|define|give me a brief explanation of|can you explain|explain)\\s+", "");

        return text.trim();
    }

    public static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        if (input == null || input.isEmpty()) return tokens;

        String[] rawTokens = input.split("\\s+");
        for (String token : rawTokens) {
            if (!token.isEmpty() && !STOP_WORDS.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    public static Set<String> tokenizeToSet(String input) {
        return new HashSet<>(tokenize(input));
    }
}
