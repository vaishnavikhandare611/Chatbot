package com.vaishnavi.chatbot;

/**
 * Structured Knowledge Repository providing topic-and-intent specific responses
 * for Regression, Classification, Clustering, Supervised Learning, Unsupervised Learning,
 * Overfitting, Neural Networks, Deep Learning, Decision Trees, Random Forest, SVM, PCA, KNN, Naive Bayes, etc.
 */
public class TopicKnowledgeRepository {

    public static boolean hasIntentResponse(String topicEntity, IntentDetector.Intent intent) {
        if (topicEntity == null || topicEntity.trim().isEmpty()) return false;
        String topic = topicEntity.toLowerCase().trim();

        if (intent == IntentDetector.Intent.DEFINITION || intent == IntentDetector.Intent.GENERAL) {
            return false; // let standard QA matcher handle definitions
        }

        return isKnownTopic(topic);
    }

    private static boolean isKnownTopic(String topic) {
        return topic.contains("regression") ||
               topic.contains("classification") ||
               topic.contains("clustering") ||
               topic.contains("supervised") ||
               topic.contains("unsupervised") ||
               topic.contains("reinforcement") ||
               topic.contains("decision tree") ||
               topic.contains("random forest") ||
               topic.contains("support vector") || topic.contains("svm") ||
               topic.contains("neural network") || topic.contains("ann") ||
               topic.contains("deep learning") ||
               topic.contains("overfitting") ||
               topic.contains("underfitting") ||
               topic.contains("knn") || topic.contains("k nearest") ||
               topic.contains("naive bayes") ||
               topic.contains("pca") || topic.contains("principal component") ||
               topic.contains("machine learning") ||
               topic.contains("artificial intelligence");
    }

    public static String getIntentResponse(String topicEntity, IntentDetector.Intent intent) {
        if (topicEntity == null || topicEntity.trim().isEmpty()) return null;
        String topic = topicEntity.toLowerCase().trim();

        if (topic.contains("k-means") || topic.contains("kmeans")) {
            return getKmeansResponse(intent);
        } else if (topic.contains("dbscan")) {
            return getDbscanResponse(intent);
        } else if (topic.contains("decision tree")) {
            return getDecisionTreeResponse(intent);
        } else if (topic.contains("random forest")) {
            return getRandomForestResponse(intent);
        } else if (topic.contains("support vector") || topic.contains("svm")) {
            return getSvmResponse(intent);
        } else if (topic.contains("neural network") || topic.contains("ann") || topic.contains("deep learning")) {
            return getNeuralNetworkResponse(intent);
        } else if (topic.contains("overfitting")) {
            return getOverfittingResponse(intent);
        } else if (topic.contains("pca") || topic.contains("principal component")) {
            return getPcaResponse(intent);
        } else if (topic.contains("knn") || topic.contains("k nearest")) {
            return getKnnResponse(intent);
        } else if (topic.contains("naive bayes")) {
            return getNaiveBayesResponse(intent);
        } else if (topic.contains("regression")) {
            return getRegressionResponse(intent);
        } else if (topic.contains("classification")) {
            return getClassificationResponse(intent);
        } else if (topic.contains("clustering")) {
            return getClusteringResponse(intent);
        } else if (topic.contains("supervised")) {
            return getSupervisedResponse(intent);
        } else if (topic.contains("unsupervised")) {
            return getUnsupervisedResponse(intent);
        } else if (topic.contains("reinforcement")) {
            return getReinforcementResponse(intent);
        } else if (topic.contains("machine learning")) {
            return getMlResponse(intent);
        }

        return null;
    }

    // --- K-MEANS RESPONSES ---
    private static String getKmeansResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case ADVANTAGES:
                return "### 💡 Advantages of K-Means Clustering:\n\n" +
                       "• **Fast & Computationally Efficient:** Scales well to large datasets with time complexity O(n*k*t).\n" +
                       "• **Simple to Implement:** Easy to understand, implement, and interpret centroid clusters.\n" +
                       "• **Guaranteed Convergence:** Iteratively minimizes intra-cluster squared distance variance.";

            case DISADVANTAGES:
                return "### ⚠️ Disadvantages of K-Means Clustering:\n\n" +
                       "• **Requires K in Advance:** Must specify number of clusters K prior to training (evaluated via Elbow Method).\n" +
                       "• **Sensitivity to Outliers:** Outliers pull cluster centroids away from natural centers.\n" +
                       "• **Spherical Cluster Assumption:** Struggles with arbitrarily shaped or non-convex clusters.";

            case WORKING:
                return "### ⚙️ How K-Means Clustering Works:\n\n" +
                       "1. **Select K:** Choose the number of clusters K.\n" +
                       "2. **Initialize Centroids:** Randomly initialize K cluster centroids in feature space.\n" +
                       "3. **Assign Points:** Assign each data point to its nearest centroid based on Euclidean distance.\n" +
                       "4. **Update Centroids:** Recalculate each centroid position as the mean of assigned data points.\n" +
                       "5. **Iterate:** Repeat assignment and update steps until centroids stop changing.";

            default:
                return null;
        }
    }

    // --- DBSCAN RESPONSES ---
    private static String getDbscanResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case ADVANTAGES:
                return "### 💡 Advantages of DBSCAN Clustering:\n\n" +
                       "• **No K Required:** Automatically discovers optimal cluster count based on feature density.\n" +
                       "• **Finds Arbitrary Shapes:** Discovers complex non-spherical clusters.\n" +
                       "• **Isolates Outliers:** Automatically identifies low-density points as noise outliers.";

            default:
                return null;
        }
    }

    // --- REGRESSION INTENT RESPONSES ---
    private static String getRegressionResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case TYPES:
            case TYPES_DETAILED:
                return "### 📌 Types of Regression in Machine Learning:\n\n" +
                       "1. **Linear Regression:** Fits a straight line equation Y = w0 + w1*X to predict a continuous target from a single independent feature. It is simple, fast, and easy to interpret.\n\n" +
                       "2. **Multiple Linear Regression:** Extends linear regression by using two or more independent predictor features to predict a single continuous target variable. It models complex multi-feature linear relationships.\n\n" +
                       "3. **Polynomial Regression:** Models non-linear relationships by expressing the dependent target as an nth-degree polynomial function of independent features. It captures non-linear curves in continuous data.\n\n" +
                       "4. **Ridge Regression:** Linear regression with L2 regularization that adds a squared weight penalty to the loss function. It constrains weight magnitudes to prevent overfitting and handle multicollinearity.\n\n" +
                       "5. **Lasso Regression:** Linear regression with L1 regularization that adds an absolute weight penalty to the loss function. It can shrink uninformative feature weights to exact zero, performing automatic feature selection.\n\n" +
                       "6. **Elastic Net Regression:** Combines both L1 (Lasso) and L2 (Ridge) regularization penalties. It balances feature selection and coefficient stability for high-dimensional correlated datasets.\n\n" +
                       "7. **Decision Tree Regression:** Uses a decision tree structure to partition continuous feature spaces into rectangular regions and predict average continuous values for each leaf node.\n\n" +
                       "8. **Random Forest Regression:** An ensemble method that constructs multiple independent decision trees and averages their continuous predictions to drastically reduce model variance and overfitting.\n\n" +
                       "9. **Support Vector Regression (SVR):** Applies Support Vector Machine principles to find a decision hyperplane that bounds maximum continuous data points within a specified error margin (epsilon).";

            case ADVANTAGES:
                return "### 💡 Advantages of Regression:\n\n" +
                       "• **Predicts Continuous Values:** Ideal for estimating exact quantitative outcomes such as prices, sales forecasts, temperatures, and financial values.\n" +
                       "• **Clear Interpretability:** Linear and regularized regression models provide explicit coefficients showing feature importance and directional impact.\n" +
                       "• **Fast & Computationally Efficient:** Linear, Ridge, and Lasso regression train rapidly even on large tabular datasets.\n" +
                       "• **Regularization Support:** Ridge and Lasso prevent overfitting on noisy or high-dimensional features.";

            case DISADVANTAGES:
                return "### ⚠️ Disadvantages & Limitations of Regression:\n\n" +
                       "• **Linearity Assumption:** Simple linear regression assumes strict linear relationships between features and target, failing on complex non-linear patterns.\n" +
                       "• **Sensitivity to Outliers:** Mean Squared Error (MSE) heavily penalizes large errors, making linear regression vulnerable to extreme outlier values.\n" +
                       "• **Multicollinearity Sensitivity:** Highly correlated independent predictor variables can destabilize linear regression coefficients.";

            case APPLICATIONS:
                return "### 🌍 Real-World Applications & Uses of Regression:\n\n" +
                       "1. **Real Estate Valuation:** Predicting house or property prices based on size, location, and number of bedrooms.\n" +
                       "2. **Financial Forecasting:** Estimating future stock prices, revenue streams, and asset growth rates.\n" +
                       "3. **Medical Risk Analysis:** Estimating patient recovery times, blood pressure levels, or disease progression scores based on patient metrics.\n" +
                       "4. **Weather & Demand Forecasting:** Forecasting temperature, rainfall amounts, or energy consumption demand.";

            case EXAMPLES:
                return "### 📝 Real-Life Example of Regression:\n\n" +
                       "**House Price Prediction:**\n" +
                       "Suppose a real estate company wants to predict the market price of a house ($Y$) based on its area in square feet ($X$).\n" +
                       "• **Input Feature ($X$):** House area = 1,500 sq ft.\n" +
                       "• **Model Equation:** $Price = \\$50,000 + (120 \\times Area)$.\n" +
                       "• **Prediction:** $Price = \\$50,000 + (120 \\times 1500) = \\$230,000$.";

            case WORKING:
                return "### ⚙️ How Regression Works (Step-by-Step):\n\n" +
                       "1. **Feature Input:** The algorithm takes continuous or encoded numerical features ($X$).\n" +
                       "2. **Model Function Formulation:** It assumes a target function, e.g., $Y = W^T X + b$.\n" +
                       "3. **Loss Function Calculation:** Calculates prediction error using Mean Squared Error (MSE) = $\\frac{1}{n} \\sum (y_i - \\hat{y}_i)^2$.\n" +
                       "4. **Weight Optimization:** Minimizes loss via Gradient Descent or Ordinary Least Squares to find optimal weights ($W$) and bias ($b$).\n" +
                       "5. **Continuous Prediction:** Uses optimal weights to output continuous predictions for new unseen data.";

            default:
                return null;
        }
    }

    // --- CLASSIFICATION INTENT RESPONSES ---
    private static String getClassificationResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case TYPES:
            case TYPES_DETAILED:
                return "### 📌 Types of Classification in Machine Learning:\n\n" +
                       "1. **Binary Classification:** Target variable has exactly two mutually exclusive classes (e.g., Spam vs Ham email detection). The algorithm calculates probability threshold cutoffs (0 or 1).\n\n" +
                       "2. **Multiclass Classification:** Target variable has three or more mutually exclusive classes (e.g., classifying handwritten digits into 0 through 9). Each instance belongs to exactly one category.\n\n" +
                       "3. **Multilabel Classification:** Each instance can be assigned multiple non-exclusive labels simultaneously (e.g., tagging a news article as both 'Politics' and 'Economy').\n\n" +
                       "4. **Imbalanced Classification:** Classification where one class heavily outnumbers the other (e.g., Fraud Detection with 99.9% non-fraud vs 0.1% fraud). Requiring specialized metrics like PR-AUC and SMOTE.";

            case ADVANTAGES:
                return "### 💡 Advantages of Classification:\n\n" +
                       "• **Categorical Decision Making:** Automates qualitative categorization tasks like spam filtering, disease diagnosis, and sentiment analysis.\n" +
                       "• **Probabilistic Outputs:** Models like Logistic Regression and Naive Bayes provide calibrated class probability confidence scores.\n" +
                       "• **Rich Model Variety:** Offers diverse algorithms ranging from linear classifiers (Logistic Regression) to tree ensembles (XGBoost) and deep networks (CNNs).";

            case DISADVANTAGES:
                return "### ⚠️ Disadvantages of Classification:\n\n" +
                       "• **Requires Labeled Data:** Supervised classification requires expensive human annotation.\n" +
                       "• **Class Imbalance Vulnerability:** Standard accuracy metrics fail when one class dominates the dataset.\n" +
                       "• **Boundary Overfitting:** Complex non-linear classifiers can overfit decision boundaries on noisy training data.";

            case APPLICATIONS:
                return "### 🌍 Applications of Classification:\n\n" +
                       "1. **Spam & Malware Detection:** Classifying emails as Spam or Legitimate.\n" +
                       "2. **Medical Diagnosis:** Classifying medical tumors as Benign or Malignant.\n" +
                       "3. **Sentiment Analysis:** Categorizing customer reviews as Positive, Neutral, or Negative.\n" +
                       "4. **Image Recognition:** Identifying objects, animals, or faces in photos.";

            case EXAMPLES:
                return "### 📝 Example of Classification:\n\n" +
                       "**Email Spam Filtering:**\n" +
                       "• **Inputs:** Email text features (words like 'free', 'discount', 'winner').\n" +
                       "• **Output Classes:** Class 1 = Spam, Class 0 = Not Spam (Ham).\n" +
                       "• **Decision:** If probability of spam $P > 0.5$, route email to Spam folder.";

            case ALGORITHMS:
                return "### 🛠️ Common Classification Algorithms:\n\n" +
                       "1. **Logistic Regression:** Linear probabilistic classifier.\n" +
                       "2. **Decision Tree Classifier:** Tree-based recursive splitting rules.\n" +
                       "3. **Random Forest & XGBoost:** High-accuracy tree ensembles.\n" +
                       "4. **Support Vector Machine (SVM):** Hyperplane margin maximizer.\n" +
                       "5. **Naive Bayes:** Fast probabilistic classifier based on Bayes Theorem.\n" +
                       "6. **K-Nearest Neighbors (KNN):** Distance-based majority voting.";

            default:
                return null;
        }
    }

    // --- CLUSTERING INTENT RESPONSES ---
    private static String getClusteringResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case TYPES:
            case TYPES_DETAILED:
                return "### 📌 Types of Clustering Algorithms:\n\n" +
                       "1. **Centroid-Based Clustering (K-Means):** Partitions data into a predefined number of K clusters based on distance to cluster centroids. Data points with similar characteristics are placed in the same cluster, making it fast and efficient for well-separated spherical data.\n\n" +
                       "2. **Density-Based Clustering (DBSCAN):** Groups data points based on spatial feature density. It can automatically discover clusters of arbitrary shapes and isolates low-density points as noise outliers without requiring a specified K.\n\n" +
                       "3. **Hierarchical Clustering:** Builds a tree-like hierarchy of clusters called a dendrogram. It can be performed using an Agglomerative (bottom-up) approach merging small clusters or a Divisive (top-down) approach splitting large clusters.\n\n" +
                       "4. **Distribution-Based Clustering (GMM):** Probabilistic clustering that assumes data points are generated from a mixture of Gaussian distributions. It provides soft cluster assignments where points have calculated probabilities for each cluster.";

            case ADVANTAGES:
                return "### 💡 Advantages of Clustering:\n\n" +
                       "• **No Labels Required:** Operates on unlabeled raw data, eliminating manual annotation costs.\n" +
                       "• **Discovers Hidden Structure:** Uncovers natural customer segments, anomaly groups, or topic themes automatically.\n" +
                       "• **Versatile Applications:** Used across market segmentation, image compression, document grouping, and anomaly detection.";

            case DISADVANTAGES:
                return "### ⚠️ Disadvantages of Clustering:\n\n" +
                       "• **Sensitivity to Parameter Selection:** K-Means requires specifying K in advance; DBSCAN requires careful tuning of eps and minPts.\n" +
                       "• **Evaluation Complexity:** Lacking true labels, evaluating cluster quality relies on heuristic metrics like Silhouette Score.\n" +
                       "• **Curse of Dimensionality:** Distance metrics become less informative in very high-dimensional feature spaces.";

            case APPLICATIONS:
                return "### 🌍 Applications of Clustering:\n\n" +
                       "1. **Customer Segmentation:** Grouping retail customers by purchasing behavior for targeted marketing.\n" +
                       "2. **Document & News Grouping:** Categorizing news articles into thematic clusters.\n" +
                       "3. **Anomaly & Fraud Detection:** Isolating outlier data points in low-density clusters.\n" +
                       "4. **Image Segmentation:** Clustering pixels by color similarity for computer vision preprocessing.";

            case EXAMPLES:
                return "### 📝 Example of Clustering:\n\n" +
                       "**E-Commerce Customer Segmentation:**\n" +
                       "• **Data:** Annual spending ($X_1$) and purchase frequency ($X_2$).\n" +
                       "• **Result:** K-Means identifies 3 clusters:\n" +
                       "  - Cluster 1: High Spending, High Frequency ('VIP Customers')\n" +
                       "  - Cluster 2: Moderate Spending, Low Frequency ('Occasion Buyers')\n" +
                       "  - Cluster 3: Low Spending, High Frequency ('Bargain Hunters')";

            default:
                return null;
        }
    }

    // --- SUPERVISED LEARNING RESPONSES ---
    private static String getSupervisedResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case TYPES:
            case TYPES_DETAILED:
                return "### 📌 Types of Supervised Learning:\n\n" +
                       "1. **Classification:** Predicts discrete categorical target labels. Algorithms learn decision boundaries to categorize instances (e.g. Spam vs Ham, Disease Diagnosis).\n\n" +
                       "2. **Regression:** Predicts continuous quantitative target values. Algorithms model mathematical relationships to estimate numerical quantities (e.g. House Price, Temperature, Sales Forecasts).";

            case ADVANTAGES:
                return "### 💡 Advantages of Supervised Learning:\n\n" +
                       "• **High Predictive Accuracy:** Training on true ground-truth targets produces highly reliable predictions.\n" +
                       "• **Clear Performance Evaluation:** Metrics like Precision, Recall, F1-Score, and MSE give clear feedback on model accuracy.\n" +
                       "• **Wide Algorithm Ecosystem:** Offers powerful models from Decision Trees to XGBoost and Neural Networks.";

            case DISADVANTAGES:
                return "### ⚠️ Disadvantages of Supervised Learning:\n\n" +
                       "• **Requires Labeled Datasets:** Acquiring ground-truth labels requires expensive, time-consuming human annotation.\n" +
                       "• **Overfitting Risk:** Models can memorize training data noise rather than learning general relationships.";

            case EXAMPLES:
                return "### 📝 Examples of Supervised Learning:\n\n" +
                       "1. **Email Spam Classification:** Predicting whether an email is Spam (1) or Ham (0) based on text features.\n" +
                       "2. **House Price Prediction (Regression):** Predicting house market values based on square footage, bedrooms, and location.";

            default:
                return null;
        }
    }

    // --- UNSUPERVISED LEARNING RESPONSES ---
    private static String getUnsupervisedResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case TYPES:
            case TYPES_DETAILED:
                return "### 📌 Types of Unsupervised Learning:\n\n" +
                       "1. **Clustering:** Partitions unlabeled data into distinct group clusters based on feature similarity (e.g. K-Means, DBSCAN, Hierarchical Clustering).\n\n" +
                       "2. **Dimensionality Reduction:** Reduces feature dimensions while retaining essential dataset variance and underlying structure (e.g. PCA, t-SNE, UMAP).\n\n" +
                       "3. **Association Rule Learning:** Uncovers co-occurrence relationships and association rules between items in large transaction datasets (e.g. Apriori, FP-Growth).";

            case ADVANTAGES:
                return "### 💡 Advantages of Unsupervised Learning:\n\n" +
                       "• **No Human Labeling Needed:** Processes raw unlabeled datasets directly, saving time and annotation costs.\n" +
                       "• **Discovers Unknown Patterns:** Finds unsuspected data groupings, customer segments, and feature correlations.";

            default:
                return null;
        }
    }

    // --- REINFORCEMENT LEARNING RESPONSES ---
    private static String getReinforcementResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case TYPES:
            case TYPES_DETAILED:
                return "### 📌 Types / Approaches in Reinforcement Learning:\n\n" +
                       "1. **Model-Based RL:** The agent learns or uses an explicit model of the environment's state transitions and rewards to plan optimal actions.\n\n" +
                       "2. **Model-Free RL:** The agent learns optimal actions directly through environmental trial-and-error without modeling state transitions (e.g. Q-Learning, SARSA).\n\n" +
                       "3. **Value-Based RL:** Focuses on optimizing a Value Function V(s) or Q(s,a) estimating expected cumulative future rewards.\n\n" +
                       "4. **Policy-Based RL:** Directly optimizes the decision Policy function π(a|s) mapping states to action probabilities (e.g. Policy Gradients, PPO).";

            default:
                return null;
        }
    }

    // --- DECISION TREE RESPONSES ---
    private static String getDecisionTreeResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case TYPES:
            case TYPES_DETAILED:
                return "### 📌 Types of Decision Trees:\n\n" +
                       "1. **Classification Tree:** Target variable is categorical. Tree splits nodes to maximize class purity evaluated via Gini Impurity or Information Gain.\n\n" +
                       "2. **Regression Tree:** Target variable is continuous. Tree splits nodes to minimize target variance or Mean Squared Error (MSE).";

            case ADVANTAGES:
                return "### 💡 Advantages of Decision Trees:\n\n" +
                       "• **Highly Interpretable:** Easy to visualize decision rules like an IF-THEN flowchart.\n" +
                       "• **No Feature Scaling Required:** Works directly on unscaled numerical and categorical features.\n" +
                       "• **Handles Non-Linear Relationships:** Naturally captures complex non-linear feature interactions.";

            case DISADVANTAGES:
                return "### ⚠️ Disadvantages of Decision Trees:\n\n" +
                       "• **High Overfitting Risk:** Deep unpruned trees easily memorize training noise (High Variance).\n" +
                       "• **Instability:** Small changes in training data can result in completely different tree structures.";

            default:
                return null;
        }
    }

    // --- RANDOM FOREST RESPONSES ---
    private static String getRandomForestResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case TYPES:
            case TYPES_DETAILED:
                return "### 📌 Random Forest Variants & Tasks:\n\n" +
                       "1. **Random Forest Classifier:** Combines multiple decision trees trained on bootstrap samples. Aggregates predictions via Majority Voting for categorical classification.\n\n" +
                       "2. **Random Forest Regressor:** Combines multiple decision trees and averages their continuous output predictions to estimate numerical targets.";

            case ADVANTAGES:
                return "### 💡 Advantages of Random Forest:\n\n" +
                       "• **Reduces Overfitting:** Combining diverse independent trees drastically lowers model variance.\n" +
                       "• **High Accuracy:** One of the most accurate off-the-shelf benchmark algorithms for tabular data.\n" +
                       "• **Built-in Feature Importance:** Automatically computes Gini feature importance rankings.";

            default:
                return null;
        }
    }

    // --- SVM RESPONSES ---
    private static String getSvmResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case TYPES:
            case TYPES_DETAILED:
                return "### 📌 Types / Kernels of Support Vector Machine (SVM):\n\n" +
                       "1. **Linear SVM:** Separates data using a linear hyperplane in original feature space. Fast and suitable for linearly separable text data.\n\n" +
                       "2. **RBF (Radial Basis Function) Kernel SVM:** Non-linear kernel mapping feature instances into infinite-dimensional Hilbert space for non-linear separation.\n\n" +
                       "3. **Polynomial Kernel SVM:** Maps features into higher dimensions using polynomial combinations of input variables.";

            default:
                return null;
        }
    }

    // --- NEURAL NETWORK RESPONSES ---
    private static String getNeuralNetworkResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case TYPES:
            case TYPES_DETAILED:
                return "### 📌 Types of Artificial Neural Networks (Deep Learning):\n\n" +
                       "1. **Artificial Neural Network (ANN / Multi-Layer Perceptron):** Fully connected feedforward layers suited for structured tabular data classification and regression.\n\n" +
                       "2. **Convolutional Neural Network (CNN):** Specialized for spatial data like images. Uses sliding convolutional kernels and pooling layers for spatial feature extraction.\n\n" +
                       "3. **Recurrent Neural Network (RNN & LSTM):** Specialized for sequential data like text and time-series. Maintains hidden state memory across time steps.\n\n" +
                       "4. **Transformers:** Uses Self-Attention mechanisms to process entire text sequences in parallel, serving as the foundation for modern LLMs (GPT, BERT).";

            default:
                return null;
        }
    }

    // --- OVERFITTING RESPONSES ---
    private static String getOverfittingResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case CAUSES:
                return "### ❓ Why Does Overfitting Happen (Causes of Overfitting):\n\n" +
                       "1. **Model Complexity is Too High:** The model has too many parameters (e.g. deep unpruned decision trees, complex neural networks) relative to data volume.\n\n" +
                       "2. **Training Data Noise & Outliers:** The algorithm memorizes specific random noise, outliers, and spurious patterns in the training set instead of learning general relationships.\n\n" +
                       "3. **Insufficient Training Samples:** The dataset is too small for the model to generalize effectively across unseen feature combinations.\n\n" +
                       "4. **Training for Too Many Epochs:** Continuous optimization without early stopping forces weights to adapt to specific training instances.";

            case PREVENT_FIX:
                return "### 🛠️ How to Reduce & Prevent Overfitting in Machine Learning:\n\n" +
                       "1. **Regularization (L1 Lasso / L2 Ridge):** Adds penalty terms to the loss function to constrain weight magnitudes.\n\n" +
                       "2. **Pruning & Tree Constraints:** Limit max_depth, min_samples_split, or min_samples_leaf in Decision Trees.\n\n" +
                       "3. **Cross-Validation:** Use 5-Fold Stratified Cross-Validation to evaluate generalization performance.\n\n" +
                       "4. **More Training Data & Data Augmentation:** Increase training sample size or augment images/text.\n\n" +
                       "5. **Early Stopping & Dropout:** In Neural Networks, stop training when validation loss stops improving and drop random neurons during training.";

            default:
                return null;
        }
    }

    // --- PCA RESPONSES ---
    private static String getPcaResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case ADVANTAGES:
                return "### 💡 Advantages of Principal Component Analysis (PCA):\n\n" +
                       "• **Reduces Dimensionality:** Eliminates redundant correlated features while retaining maximum dataset variance.\n" +
                       "• **Improves Model Speed:** Accelerates training times for downstream classification and regression algorithms.\n" +
                       "• **Enables Data Visualization:** Projects high-dimensional datasets into 2D or 3D scatter plots.";

            default:
                return null;
        }
    }

    // --- KNN RESPONSES ---
    private static String getKnnResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case WORKING:
                return "### ⚙️ How K-Nearest Neighbors (KNN) Works:\n\n" +
                       "1. **Choose K:** Select the number of nearest neighbors K (e.g. K = 5).\n" +
                       "2. **Calculate Distance:** Compute distance (Euclidean or Manhattan) between the new point and all training instances.\n" +
                       "3. **Find K Neighbors:** Identify the K closest training points.\n" +
                       "4. **Majority Vote / Average:** Assign the majority class label (classification) or average target value (regression) among the K neighbors.";

            default:
                return null;
        }
    }

    // --- NAIVE BAYES RESPONSES ---
    private static String getNaiveBayesResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case TYPES:
            case TYPES_DETAILED:
                return "### 📌 Types of Naive Bayes Classifiers:\n\n" +
                       "1. **Gaussian Naive Bayes:** Assumes continuous features follow a normal Gaussian distribution. Used for continuous quantitative variables.\n\n" +
                       "2. **Multinomial Naive Bayes:** Used for discrete word frequency counts in text classification and spam filtering.\n\n" +
                       "3. **Bernoulli Naive Bayes:** Designed for binary/boolean features (0 or 1 presence of words).";

            default:
                return null;
        }
    }

    // --- GENERAL ML RESPONSES ---
    private static String getMlResponse(IntentDetector.Intent intent) {
        switch (intent) {
            case TYPES:
            case TYPES_DETAILED:
            case GENERAL:
                return "### Types of Machine Learning\n\n" +
                       "1. Supervised Learning\n" +
                       "   - Learns from labeled data.\n" +
                       "   - Examples: Classification, Regression.\n\n" +
                       "2. Unsupervised Learning\n" +
                       "   - Learns patterns from unlabeled data.\n" +
                       "   - Examples: Clustering, Dimensionality Reduction.\n\n" +
                       "3. Semi-Supervised Learning\n" +
                       "   - Uses a combination of labeled and unlabeled data.\n\n" +
                       "4. Reinforcement Learning\n" +
                       "   - Learns by interacting with an environment using rewards and penalties.";

            default:
                return null;
        }
    }
}
