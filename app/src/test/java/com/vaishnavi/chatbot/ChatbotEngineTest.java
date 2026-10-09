package com.vaishnavi.chatbot;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ChatbotEngineTest {

    private NlpChatbot chatbot;

    @Before
    public void setUp() {
        chatbot = new NlpChatbot(null); // Local test mode
    }

    // TEST REQUIRED BY BUG FIX SPECIFICATION
    @Test
    public void testTypesOfMlQueryReturnsGeneralMlTypes() {
        String[] queries = {
            "types of ml",
            "types of machine learning",
            "ml types",
            "what are types of ml",
            "machine learning types",
            "ml che types",
            "machine learning che types",
            "machine learning che prakar",
            "ml che prakar"
        };

        for (String q : queries) {
            chatbot.getConversationContext().clear(); // Reset context for independent test
            String resp = chatbot.getResponse(q);
            assertNotNull("Response for '" + q + "' should not be null!", resp);

            // MUST contain general ML types
            assertTrue("Query '" + q + "' must contain 'Supervised Learning'!", resp.contains("Supervised Learning"));
            assertTrue("Query '" + q + "' must contain 'Unsupervised Learning'!", resp.contains("Unsupervised Learning"));
            assertTrue("Query '" + q + "' must contain 'Semi-Supervised Learning'!", resp.contains("Semi-Supervised Learning"));
            assertTrue("Query '" + q + "' must contain 'Reinforcement Learning'!", resp.contains("Reinforcement Learning"));

            // MUST NOT contain clustering algorithms header
            assertFalse("Query '" + q + "' MUST NOT return Clustering algorithms!", resp.contains("Types of Clustering Algorithms"));
        }
    }

    @Test
    public void testSpecificSubtopicQueries() {
        String resp1 = chatbot.getResponse("what is machine learning");
        assertNotNull(resp1);
        assertTrue(resp1.toLowerCase().contains("machine learning"));

        String resp2 = chatbot.getResponse("types of clustering");
        assertNotNull(resp2);
        assertTrue(resp2.contains("K-Means") || resp2.contains("DBSCAN") || resp2.contains("Hierarchical"));

        String resp3 = chatbot.getResponse("types of supervised learning");
        assertNotNull(resp3);
        assertTrue(resp3.contains("Classification") && resp3.contains("Regression"));

        String resp4 = chatbot.getResponse("types of regression");
        assertNotNull(resp4);
        assertTrue(resp4.contains("Linear Regression") || resp4.contains("Types of Regression"));

        String resp5 = chatbot.getResponse("what is k means");
        assertNotNull(resp5);
        assertTrue(resp5.toLowerCase().contains("k-means") || resp5.toLowerCase().contains("centroid") || resp5.toLowerCase().contains("cluster") || resp5.toLowerCase().contains("kmeans") || resp5.toLowerCase().contains("unsupervised"));

        String resp6 = chatbot.getResponse("what is dbscan");
        assertNotNull(resp6);
        assertTrue(resp6.toLowerCase().contains("dbscan") || resp6.toLowerCase().contains("density"));
    }

    // TEST 1: Regression Full Multi-Turn Conversation
    @Test
    public void testRegressionConversationFlow() {
        // 1. Definition
        String resp1 = chatbot.getResponse("what is regression");
        assertNotNull(resp1);
        assertTrue(resp1.toLowerCase().contains("regression") || resp1.toLowerCase().contains("predict"));

        // 2. Types
        String resp2 = chatbot.getResponse("what are its types");
        assertNotNull(resp2);
        assertTrue(resp2.contains("Linear Regression") || resp2.contains("Types of Regression"));
        assertFalse("Response should not repeat definition!", resp2.toLowerCase().startsWith("regression is a supervised learning task used to predict continuous"));

        // 3. Detailed Types
        String resp3 = chatbot.getResponse("explain its types in detail");
        System.err.println("DEBUG_REGRESSION_RESP3: [" + resp3 + "]");
        assertNotNull(resp3);
        assertTrue("REGRESSION_RESP3_FAILED: " + resp3, resp3.contains("Linear Regression") && (resp3.contains("Polynomial") || resp3.contains("Ridge") || resp3.contains("Lasso")));

        // 4. Advantages
        String resp4 = chatbot.getResponse("what are its advantages");
        assertNotNull(resp4);
        assertTrue(resp4.contains("Advantages of Regression") || resp4.toLowerCase().contains("predicts continuous values"));

        // 5. Disadvantages
        String resp5 = chatbot.getResponse("what are its disadvantages");
        assertNotNull(resp5);
        assertTrue(resp5.contains("Disadvantages") || resp5.toLowerCase().contains("sensitivity to outliers") || resp5.toLowerCase().contains("linearity assumption"));

        // 6. Applications
        String resp6 = chatbot.getResponse("where is it used");
        assertNotNull(resp6);
        assertTrue(resp6.contains("Applications") || resp6.toLowerCase().contains("real estate") || resp6.toLowerCase().contains("house price"));
    }

    // TEST 2: Clustering Follow-Up Sequence
    @Test
    public void testClusteringFollowUpSequence() {
        String resp1 = chatbot.getResponse("what is clustering");
        assertNotNull(resp1);

        String resp2 = chatbot.getResponse("what are its types");
        assertNotNull(resp2);

        String resp3 = chatbot.getResponse("explain the first one");
        assertNotNull(resp3);

        String resp4 = chatbot.getResponse("what are its advantages");
        assertNotNull(resp4);
        assertTrue(resp4.toLowerCase().contains("advantage") || resp4.toLowerCase().contains("simple") || resp4.toLowerCase().contains("fast"));
    }

    // TEST 3: Supervised Learning Sequence
    @Test
    public void testSupervisedLearningSequence() {
        String resp1 = chatbot.getResponse("what is supervised learning");
        assertNotNull(resp1);

        String resp2 = chatbot.getResponse("what are its types");
        assertNotNull(resp2);
        assertTrue(resp2.contains("Classification") && resp2.contains("Regression"));

        String resp3 = chatbot.getResponse("give examples");
        assertNotNull(resp3);
        assertTrue(resp3.toLowerCase().contains("example") || resp3.toLowerCase().contains("spam") || resp3.toLowerCase().contains("house price"));
    }

    // TEST 4: Classification Sequence
    @Test
    public void testClassificationSequence() {
        String resp1 = chatbot.getResponse("what is classification");
        assertNotNull(resp1);

        String resp2 = chatbot.getResponse("what are its algorithms");
        assertNotNull(resp2);
        assertTrue(resp2.contains("Logistic Regression") || resp2.contains("Decision Tree") || resp2.contains("Random Forest"));

        String resp3 = chatbot.getResponse("which one is commonly used");
        System.err.println("===CLASS_RESP3===" + resp3 + "===");
        assertNotNull(resp3);
        assertTrue("CLASS_RESP3_MSG: " + resp3, resp3.toLowerCase().contains("algorithm") || resp3.toLowerCase().contains("xgboost") || resp3.toLowerCase().contains("random forest"));
    }

    // TEST 5: Overfitting Sequence
    @Test
    public void testOverfittingSequence() {
        String resp1 = chatbot.getResponse("what is overfitting");
        assertNotNull(resp1);

        String resp2 = chatbot.getResponse("why does it happen");
        assertNotNull(resp2);

        String resp3 = chatbot.getResponse("how can we reduce it");
        assertNotNull(resp3);
        assertTrue(resp3.contains("Regularization") || resp3.contains("Cross-Validation") || resp3.toLowerCase().contains("pruning"));
    }

    // TEST 6: Marathi & Hinglish Follow-ups
    @Test
    public void testMarathiHinglishFollowUps() {
        chatbot.getResponse("regression mhnje kay");
        String resp = chatbot.getResponse("tyache types kontte");
        assertNotNull(resp);
        assertTrue(resp.contains("Linear Regression") || resp.contains("Types of Regression"));

        String resp2 = chatbot.getResponse("tyache advantages kay");
        assertNotNull(resp2);
        assertTrue(resp2.toLowerCase().contains("advantage") || resp2.contains("Advantages"));
    }

    // TEST 7: Short Questions
    @Test
    public void testShortQuestions() {
        chatbot.getResponse("what is regression");

        String resp1 = chatbot.getResponse("types?");
        assertTrue(resp1.contains("Linear Regression") || resp1.contains("Types of Regression"));

        String resp2 = chatbot.getResponse("advantages?");
        assertTrue(resp2.toLowerCase().contains("advantage") || resp2.contains("Advantages"));

        String resp3 = chatbot.getResponse("applications?");
        assertTrue(resp3.toLowerCase().contains("application") || resp3.toLowerCase().contains("real estate"));
    }
}
