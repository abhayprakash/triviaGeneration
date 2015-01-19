library(tm)
library(RTextTools)

cross_validate_SVM_PRFA <- function(container, nfold, method = "C-classification", cross = 0, cost = 100, kernel = "radial")
{
  extract_label_from_prob_names <- function(x) return(rownames(as.matrix(which.max(x))))
  alldata <- rbind(container@training_matrix, container@classification_matrix)
  allcodes <- as.factor(c(container@training_codes, container@testing_codes))
  rand <- sample(nfold, dim(alldata)[1], replace = T)
  
  cv_accuracy <- NULL
  cv_p1 <- NULL
  cv_r1 <- NULL
  cv_f1 <- NULL
  cv_p0 <- NULL
  cv_r0 <- NULL
  cv_f0 <- NULL
  
  for (i in sort(unique(rand))) {
    model <- svm(x = alldata[rand != i, ], y = allcodes[rand != i], method = method, cross = cross, cost = cost, kernel = kernel)
    pred <- predict(model, alldata[rand == i, ])
    
    # for comparision: original and predicted
    true_labels <- as.vector(allcodes[rand == i])
    predicted_labels <- as.vector(pred, mode = class(true_labels))
    
    # accuracy
    analyze <- predicted_labels == true_labels
    cv_accuracy[i] <- length(analyze[analyze == TRUE])/length(true_labels)
    
    # precision 1 
    correct_1 <- (predicted_labels == true_labels) & (true_labels == 2)
    cv_p1[i] <- length(correct_1[correct_1 == TRUE])/length(predicted_labels[predicted_labels == 2])
    
    # recall 1
    cv_r1[i] <- length(correct_1[correct_1 == TRUE])/length(true_labels[true_labels == 2])
    
    # f 1
    cv_f1[i] <- (2*cv_p1[i]*cv_r1[i])/(cv_p1[i]+cv_r1[i])
    
    # precision 0
    correct_0 <- (predicted_labels == true_labels) & (true_labels == 1)
    cv_p0[i] <- length(correct_0[correct_0 == TRUE])/length(predicted_labels[predicted_labels == 1])
    
    # recall 0
    cv_r0[i] <- length(correct_0[correct_0 == TRUE])/length(true_labels[true_labels == 1])
    
    # f 1
    cv_f0[i] <- (2*cv_p0[i]*cv_r0[i])/(cv_p0[i]+cv_r0[i])
    
    cat("Fold ", i, " Out of Sample Accuracy", " = ", cv_accuracy[i], "\n", sep = "")
    cat("Precision 0: ", cv_p0[i], "  Recall 0: ", cv_r0[i],"  F 0: ", cv_f0[i], "\n", sep = "")
    cat("Precision 1: ", cv_p1[i], "  Recall 1: ", cv_r1[i],"  F 1: ", cv_f1[i], "\n", sep = "")
    cat("----------\n")
  }
  cat("\nMean Accuracy: ", mean(cv_accuracy), "\n")
  cat("Mean Precison 0: ", mean(cv_p0), "  Mean Recall 0: ", mean(cv_r0), "  Mean F 0: ", mean(cv_f0), "\n")
  cat("Mean Precison 1: ", mean(cv_p1), "  Mean Recall 1: ", mean(cv_r1), "  Mean F 1: ", mean(cv_f1), "\n")
  cat("Average F score: ", (mean(cv_f0) + mean(cv_f1))/2)
}

data <- read.csv("trainData_5K_richFeatures.txt", sep='\t', header=T)
data <- data[sample(nrow(data)),]
#load("compareData.RData")

#name <- data[,"MOVIE_NAME_IMDB"]
training_data <- data["TRIVIA"]
#data[data$CLASS_V == "Very_Interesting_fact", ]$CLASS_V <- "Interesting_fact"
training_codes <- data["CLASS"]

totalRows <- nrow(data)
trainEnd <- round((4*totalRows)/5)
testStart <- trainEnd + 1
matrix <- NULL

# Unigram words
matrix <- create_matrix(training_data, language = "english", stripWhitespace = TRUE, removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTf)

# parse tree features
root_matrix <- create_matrix(data["ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
subject_matrix <- create_matrix(data["SUBJECT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
under_root_matrix <- create_matrix(data["UNDER_ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
all_linked_entities_matrix <- create_matrix(data["ALL_LINKABLE_ENTITIES_PRESENT"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
parse_features_matrix <- cbind(as.matrix(all_linked_entities_matrix), as.matrix(root_matrix), as.matrix(subject_matrix), as.matrix(under_root_matrix))

#parse_features_matrix <- cbind(as.matrix(root_matrix), as.matrix(subject_matrix), as.matrix(under_root_matrix))

matrix <- cbind(as.matrix(matrix), as.matrix(parse_features_matrix))

# + frequency of superlative and comparative POS as feature
matrix <- cbind(matrix, as.matrix(data["superPOS"]))
matrix <- cbind(matrix, as.matrix(data["compPOS"]))

# + frequency of different NERs
matrix <- cbind(matrix, as.matrix(data[,c("PERSON","ORGANIZATION","DATE","LOCATION","MONEY","TIME")]))

addedFeatures <- c("PERSON","ORGANIZATION","DATE","LOCATION","MONEY","TIME","superPOS", "compPOS")

# converting frequencies to boolean presence
for(col in addedFeatures)
{
  index <- matrix[,col] > 0
  matrix[index,col] <- 1
}

############ training and testing
container <- create_container(matrix, t(training_codes), trainSize=1:trainEnd, testSize=testStart:totalRows, virgin=FALSE)
cross_validate_SVM_PRFA(container, 5, "SVM", kernel = "linear")

  # only on 1 of the folds
  model <- train_model(container, algorithm=c("SVM"), method = "C-classification", cross = 5, cost = 100, kernel = "linear")
  results <- classify_model(container, model)
  analytics <- create_analytics(container, results)
  print(analytics@algorithm_summary)

  # cross validate for accuracy
  print(cross_validate(container, 5, "SVM"))

############# ranking for features - NOTE: works only for linear kernel
w = t(model$coefs) %*% model$SV
features <- colnames(matrix)[w@ja]
weights <- w@ra
featureWeights <- data.frame(cbind(features, weights))
featureWeights$weights <- abs(as.numeric(as.character(featureWeights$weights)))
featureWeights <- featureWeights[with(featureWeights, order(-weights)),]
row.names(featureWeights) <- 1:nrow(featureWeights)
zeroFeatures <- colnames(matrix)[-w@ja]

# get rank of features
cat("Total num of NON-ZERO features: ", nrow(featureWeights) , "/" , ncol(matrix))
print(subset(featureWeights, features %in% addedFeatures))

# get results for analysis of false negatives and false positives
actualKnownResultsForTestRows <- data.frame(training_codes[testStart:totalRows,])
test_TriviaSentences <- data.frame(training_data[testStart:totalRows,])
comparingData <- cbind(test_TriviaSentences, results, actualKnownResultsForTestRows)
write.table(comparingData, file="falseNeg_Pos_analysis.txt", row.names=F, sep='\t')