package com.vaishnavi.chatbot;

/**
 * Detects specific user question intent (Definition, Types, Detailed Types, Advantages,
 * Disadvantages, Applications, Examples, Working, Algorithms, Comparison, Causes, Fix/Prevent, Code).
 * Works across English, Hinglish, and Marathi phrases.
 */
public class IntentDetector {

    public enum Intent {
        DEFINITION,
        TYPES,
        TYPES_DETAILED,
        ADVANTAGES,
        DISADVANTAGES,
        APPLICATIONS,
        EXAMPLES,
        WORKING,
        ALGORITHMS,
        COMPARISON,
        CAUSES,
        PREVENT_FIX,
        CODE,
        GENERAL
    }

    public static Intent detectIntent(String rawPrompt, String preprocessedInput) {
        if (rawPrompt == null || rawPrompt.trim().isEmpty()) return Intent.GENERAL;

        String clean = rawPrompt.replaceAll("[^a-zA-Z0-9\\s\\-]", "").toLowerCase().trim();
        String prep = preprocessedInput != null ? preprocessedInput.toLowerCase().trim() : "";

        // 1. TYPES_DETAILED
        if ((clean.contains("types") || clean.contains("kinds") || clean.contains("categories") || clean.contains("prakar")) &&
                (clean.contains("detail") || clean.contains("deep") || clean.contains("thorough") || clean.contains("sarkhe"))) {
            return Intent.TYPES_DETAILED;
        }

        // 2. TYPES
        if (clean.matches("^(types|kinds|categories|prakar|che types|types sang|types kontte)$") ||
                clean.contains("types") || clean.contains("kinds") || clean.contains("categories") || clean.contains("prakar")) {
            return Intent.TYPES;
        }

        // 3. DISADVANTAGES (MUST BE CHECKED BEFORE ADVANTAGES because "disadvantage" contains "advantage"!)
        if (clean.matches("^(disadvantages|limitations|drawbacks|cons|tote|disadvantages kay)$") ||
                clean.contains("disadvantage") || clean.contains("limitation") || clean.contains("drawback") || clean.contains("cons") || clean.contains("tote")) {
            return Intent.DISADVANTAGES;
        }

        // 4. ADVANTAGES
        if (clean.matches("^(advantages|benefits|uses|pros|fayde|advantages kay)$") ||
                clean.contains("advantage") || clean.contains("benefit") || clean.contains("pros") || clean.contains("fayde") || clean.contains("why useful")) {
            return Intent.ADVANTAGES;
        }

        // 5. CAUSES ("why does it happen", "causes", "ka hota")
        if (clean.contains("why does it happen") || clean.contains("why it happens") || clean.contains("cause") || clean.contains("ka hota") || clean.contains("ka hote") || clean.contains("why happen")) {
            return Intent.CAUSES;
        }

        // 6. PREVENT_FIX ("how to reduce", "how to fix", "prevent")
        if (clean.contains("reduce") || clean.contains("prevent") || clean.contains("fix") || clean.contains("how to solve") || clean.contains("avoid")) {
            return Intent.PREVENT_FIX;
        }

        // 7. APPLICATIONS / USES
        if (clean.matches("^(applications|uses|where used|real world uses|kuthe use|use case|usecases)$") ||
                clean.contains("application") || clean.contains("use case") || clean.contains("where used") || clean.contains("real world") || clean.contains("kuthe use") || (clean.contains("where") && clean.contains("used"))) {
            return Intent.APPLICATIONS;
        }

        // 8. EXAMPLES
        if (clean.matches("^(example|examples|give an example|real life example|udaharan|example de)$") ||
                clean.contains("example") || clean.contains("udaharan") || clean.contains("sample")) {
            return Intent.EXAMPLES;
        }

        // 9. WORKING
        if (clean.matches("^(how|working|how works|kasa work karto|working sang|kasa work)$") ||
                clean.contains("how does it work") || clean.contains("how it works") || clean.contains("working") || clean.contains("kasa work") || clean.contains("how works")) {
            return Intent.WORKING;
        }

        // 10. ALGORITHMS
        if (clean.matches("^(algorithms|which algorithms|methods|kontte algorithms)$") ||
                clean.contains("algorithm") || clean.contains("method") || clean.contains("which one") || clean.contains("commonly used")) {
            return Intent.ALGORITHMS;
        }

        // 11. COMPARISON
        if (clean.contains("difference") || clean.contains("vs") || clean.contains("compare") || clean.contains("contrast") || clean.contains("madhla difference")) {
            return Intent.COMPARISON;
        }

        // 12. CODE
        if (clean.contains("code") || clean.contains("python") || clean.contains("implement") || clean.contains("script")) {
            return Intent.CODE;
        }

        // 13. DEFINITION
        if (clean.contains("what is") || clean.contains("define") || clean.contains("meaning") || clean.contains("tell me about") || clean.contains("explain") || prep.contains("what is")) {
            return Intent.DEFINITION;
        }

        return Intent.GENERAL;
    }
}
