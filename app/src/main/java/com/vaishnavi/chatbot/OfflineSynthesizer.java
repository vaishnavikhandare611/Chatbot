package com.vaishnavi.chatbot;

/**
 * Synthesizes comprehensive, structured Machine Learning educational responses
 * when the chatbot operates offline or when no exact QA match is found.
 * Ensures the chatbot NEVER responds with "I don't know" or fixed FAQ errors.
 */
public class OfflineSynthesizer {

    private final MLKnowledgeBase knowledgeBase;

    public OfflineSynthesizer(MLKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public String synthesizeResponse(String rawPrompt, String resolvedQuery, ConversationContext context) {
        String lowerQuery = resolvedQuery.toLowerCase().trim();
        String activeTopic = context.getCurrentTopicEntity();

        // 1. Model Performance & Troubleshooting Queries (e.g., "Why model fails on unseen data?")
        if (lowerQuery.contains("perform well during training but fail") || lowerQuery.contains("fail on new data") ||
            lowerQuery.contains("unseen data") || lowerQuery.contains("poor generalization") ||
            (lowerQuery.contains("training") && lowerQuery.contains("test") && lowerQuery.contains("fail"))) {
            return "### 💡 Why Models Perform Well on Training Data but Fail on New/Unseen Data:\n\n" +
                    "This is a classic issue in Machine Learning known as **Overfitting** or **Data Leakage**. Here are the primary reasons and solutions:\n\n" +
                    "1. **Overfitting (High Variance):** The model has memorized the noise and specific patterns of the training dataset instead of learning general underlying relationships.\n" +
                    "   • *Fix:* Apply Regularization (L1/L2), drop features, use early stopping, or increase dataset size.\n\n" +
                    "2. **Data Leakage:** Information from the test dataset accidentally contaminated the training set (e.g., performing scaling before train-test split).\n" +
                    "   • *Fix:* Perform all preprocessing (scaling, imputer) ONLY on training data after splitting.\n\n" +
                    "3. **Train-Test Distribution Mismatch (Concept Drift / Covariate Shift):** The test data distribution differs significantly from training data.\n" +
                    "   • *Fix:* Use Stratified K-Fold Cross-Validation and verify domain alignment.\n\n" +
                    "4. **Feature Complexity:** Overly complex models (e.g. deep decision trees without max_depth constraint).\n" +
                    "   • *Fix:* Prune tree depth or use ensemble models like Random Forest.";
        }

        if ((lowerQuery.contains("make") || lowerQuery.contains("improve") || lowerQuery.contains("boost") || lowerQuery.contains("increase")) &&
            (lowerQuery.contains("model") || lowerQuery.contains("prediction") || lowerQuery.contains("accuracy"))) {
            return "### 🚀 How to Improve Machine Learning Model Performance:\n\n" +
                    "To systematically improve your prediction model, follow these proven strategies:\n\n" +
                    "1. **Quality Data & Preprocessing:**\n" +
                    "   • Clean missing values and handle outliers.\n" +
                    "   • Address class imbalance using SMOTE or class weights.\n\n" +
                    "2. **Feature Engineering & Selection:**\n" +
                    "   • Create domain-specific interaction features.\n" +
                    "   • Eliminate redundant or noisy features using PCA or Feature Importance analysis.\n\n" +
                    "3. **Model Selection & Ensembling:**\n" +
                    "   • Try tree-based ensemble models like **XGBoost**, **LightGBM**, or **Random Forest**.\n" +
                    "   • Combine model outputs using Stacking or Bagging.\n\n" +
                    "4. **Hyperparameter Tuning:**\n" +
                    "   • Use Grid Search CV or Random Search CV to optimize learning rate, tree depth, and regularization penalty.\n\n" +
                    "5. **Validation Strategy:**\n" +
                    "   • Evaluate using 5-Fold Stratified Cross-Validation instead of a single split.";
        }

        // 2. Code / Implementation Query
        if (lowerQuery.contains("code") || lowerQuery.contains("python") || lowerQuery.contains("implement") || lowerQuery.contains("how to train")) {
            String topic = activeTopic != null ? activeTopic : "Classification / Regression";
            return "### 💻 Python Machine Learning Workflow Code (" + topic + "):\n\n" +
                    "```python\n" +
                    "# 1. Import essential libraries\n" +
                    "import numpy as np\n" +
                    "import pandas as pd\n" +
                    "from sklearn.model_selection import train_test_split\n" +
                    "from sklearn.preprocessing import StandardScaler\n" +
                    "from sklearn.ensemble import RandomForestClassifier\n" +
                    "from sklearn.metrics import accuracy_score, classification_report\n\n" +
                    "# 2. Load dataset\n" +
                    "# df = pd.read_csv('data.csv')\n" +
                    "# X = df.drop('target', axis=1)\n" +
                    "# y = df['target']\n\n" +
                    "# 3. Train-Test Split\n" +
                    "X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)\n\n" +
                    "# 4. Feature Scaling\n" +
                    "scaler = StandardScaler()\n" +
                    "X_train_scaled = scaler.fit_transform(X_train)\n" +
                    "X_test_scaled = scaler.transform(X_test)\n\n" +
                    "# 5. Train Model\n" +
                    "model = RandomForestClassifier(n_estimators=100, random_state=42)\n" +
                    "model.fit(X_train_scaled, y_train)\n\n" +
                    "# 6. Evaluate Predictions\n" +
                    "y_pred = model.predict(X_test_scaled)\n" +
                    "print('Accuracy:', accuracy_score(y_test, y_pred))\n" +
                    "print(classification_report(y_test, y_pred))\n" +
                    "```\n\n" +
                    "This clean scikit-learn workflow includes splitting, scaling, training, and evaluation for " + topic + ".";
        }

        // 3. Project Guidance
        if (lowerQuery.contains("project idea") || lowerQuery.contains("project workflow") || lowerQuery.contains("ml project")) {
            return "### 📌 Machine Learning End-to-End Project Workflow:\n\n" +
                    "1. **Problem Definition:** Clearly define objective (Classification, Regression, Clustering, Recommendation).\n" +
                    "2. **Dataset Acquisition:** Sources like Kaggle, UCI ML Repository, or API scrapers.\n" +
                    "3. **Exploratory Data Analysis (EDA):** Visualize distributions, correlations (heatmap), missing values.\n" +
                    "4. **Data Cleaning & Preprocessing:** Imputation, One-Hot Encoding, Outlier handling, Scaling.\n" +
                    "5. **Model Building & Tuning:** Train Baseline model -> Ensembles (XGBoost) -> Hyperparameter Grid Search.\n" +
                    "6. **Evaluation & Explainability:** Evaluate Precision/Recall/ROC-AUC or RMSE, inspect SHAP feature importance.\n" +
                    "7. **Deployment:** Serve model via FastAPI/Flask or Android app using TFLite/ML Kit.";
        }

        // 4. Mathematical / Statistical Concepts
        if (lowerQuery.contains("variance") || lowerQuery.contains("standard deviation") || lowerQuery.contains("probability") ||
            lowerQuery.contains("bayes theorem") || lowerQuery.contains("gradient descent math")) {
            return "### 📊 Mathematics & Statistics in Machine Learning:\n\n" +
                    "Mathematics forms the foundation of Machine Learning algorithms:\n" +
                    "• **Linear Algebra:** Vectors, Matrices, Matrix Multiplication, Matrix Inversion, Eigenvalues (used in PCA, Neural Networks, SVM).\n" +
                    "• **Calculus & Optimization:** Derivatives, Partial Derivatives, Gradients, Gradient Descent (minimized loss functions in Linear Regression & Deep Learning).\n" +
                    "• **Probability & Statistics:** Mean, Variance, Standard Deviation, Normal Distribution, Bayes Theorem (P(A|B) = P(B|A)*P(A)/P(B) in Naive Bayes), Hypothesis Testing.\n\n" +
                    "Tell me which specific formula or equation you want to explore in detail!";
        }

        // 5. Active Topic Context Synthesis Fallback
        if (activeTopic != null && !activeTopic.trim().isEmpty()) {
            return "### 🧠 Concept Summary for **" + activeTopic + "**:\n\n" +
                    "**" + activeTopic + "** is a fundamental topic in Machine Learning.\n\n" +
                    "• **Key Principle:** It plays a vital role in enabling algorithms to process data, discover patterns, and make accurate predictions.\n" +
                    "• **Common Applications:** Automated decision making, predictive modeling, pattern extraction, and data analysis.\n" +
                    "• **Related Concepts:** Feature engineering, model evaluation, supervised/unsupervised learning, and hyperparameter optimization.\n\n" +
                    "Feel free to ask specific follow-ups like:\n" +
                    "1. What are its types?\n" +
                    "2. How does it work?\n" +
                    "3. What are its advantages and real-life examples?\n" +
                    "4. Give me Python code for it.";
        }

        // 6. Generic Machine Learning Overview Fallback
        return "### 🤖 Machine Learning Overview:\n\n" +
                "Machine Learning (ML) is a branch of Artificial Intelligence that focuses on building algorithms that learn patterns from data to make predictions or decisions without being explicitly programmed.\n\n" +
                "**Core Areas You Can Ask Me About:**\n" +
                "1. **Supervised Learning:** Linear/Logistic Regression, Decision Trees, Random Forest, SVM, KNN, XGBoost.\n" +
                "2. **Unsupervised Learning:** K-Means, Hierarchical Clustering, DBSCAN, PCA, Anomaly Detection.\n" +
                "3. **Deep Learning & Neural Networks:** ANN, CNN, RNN, LSTM, Transformers, PyTorch/TensorFlow.\n" +
                "4. **Data Preprocessing & Evaluation:** Normalization, One-Hot Encoding, SMOTE, Precision/Recall, ROC-AUC, Overfitting.\n" +
                "5. **Python Code & ML Project Guidance:** Scikit-learn workflows, project ideas, and debugging.\n\n" +
                "What specific topic or question would you like to explore?";
    }
}
