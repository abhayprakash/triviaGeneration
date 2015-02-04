library(tm)
library(RTextTools)
library(e1071)

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
  model <- NULL
  
  for (i in sort(unique(rand))) {
    model <- svm(x = alldata[rand != i, ], y = allcodes[rand != i], gamma=0.001, method = method, cross = cross, cost = cost, kernel = kernel, probability = TRUE)
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
  cat("Average F score: ", (mean(cv_f0) + mean(cv_f1))/2);
  model
}

train_validate_data <- read.csv("train_data.txt", sep='\t', header=T)
train_validate_data <- train_validate_data[sample(nrow(train_validate_data)),]

# for multiclass
train_validate_data$GRADE <- NULL
train_validate_data$INTERESTED <- NULL
train_validate_data$VOTED <- NULL
train_validate_data$LIKENESS_RATIO <- NULL

# tracking train_validate_data
train_validate_rows <- nrow(train_validate_data)

# HACK PART: add the unseen test part also
test_data <- read.csv("test_set.txt", header = T, sep = '\t')
test_data$MOVIE <- NULL
test_data$count_boring <- NULL
test_data$count_interesting <- NULL
test_data$count_veryInteresting <- NULL
test_data$GRADE <- NULL

combined_data <- rbind(train_validate_data, test_data)

#name <- data[,"MOVIE_NAME_IMDB"]
combined_trivia <- combined_data["TRIVIA"]
combined_codes <- combined_data["CLASS"]

# Unigram words: combined for train, validate and test
combined_matrix <- create_matrix(combined_trivia, language = "english", stripWhitespace = TRUE, removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)

# parse tree features: combined for train, validate and test
root_matrix <- create_matrix(combined_data["ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
subject_matrix <- create_matrix(combined_data["SUBJECT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
under_root_matrix <- create_matrix(combined_data["UNDER_ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
all_linked_entities_matrix <- create_matrix(combined_data["ALL_LINKABLE_ENTITIES_PRESENT"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
parse_features_matrix <- cbind(as.matrix(all_linked_entities_matrix), as.matrix(root_matrix), as.matrix(subject_matrix), as.matrix(under_root_matrix))

combined_matrix <- cbind(as.matrix(combined_matrix), as.matrix(parse_features_matrix))

# + frequency of superlative POS and comparative POS as features
combined_matrix <- cbind(combined_matrix, as.matrix(combined_data["superPOS"]))
#combined_matrix <- cbind(combined_matrix, as.matrix(combined_data["compPOS"]))

# + frequency of different NERs
combined_matrix <- cbind(combined_matrix, as.matrix(combined_data[,c("PERSON","ORGANIZATION","DATE","LOCATION","MONEY","TIME", "FOG", "Contradict")]))

addedFeatures <- c("PERSON","ORGANIZATION","DATE","LOCATION","MONEY","TIME","superPOS", "Contradict")#, "compPOS")

# converting frequencies to boolean presence
for(col in addedFeatures)
{
  index <- combined_matrix[,col] > 0
  combined_matrix[index,col] <- 1
}

index <- (combined_matrix[,"FOG"] < 7)
combined_matrix[index, "FOG"] <- as.factor(1)

index <- (combined_matrix[,"FOG"] >= 7)
combined_matrix[index,"FOG"] <- as.factor(2)

index <- (combined_matrix[,"FOG"] >= 15)
combined_matrix[index,"FOG"] <- as.factor(3)

# tracking breakpoint
test_start <- train_validate_rows+1
combined_rows <- nrow(combined_matrix)

# splitting combined_matrix
train_validate_matrix <- combined_matrix[1:train_validate_rows,]
test_matrix <- combined_matrix[test_start:combined_rows,]

# splitting combined_codes
train_validate_codes <- combined_codes[1:train_validate_rows,]
test_codes <- combined_codes[test_start:combined_rows,]

# preparing container for training and validating data
trainEnd <- round((4*train_validate_rows)/5)
validateStart <- trainEnd + 1
train_validate_container <- create_container(train_validate_matrix, t(train_validate_codes), trainSize=1:trainEnd, testSize=validateStart:train_validate_rows, virgin=FALSE)

# training
model <- cross_validate_SVM_PRFA(train_validate_container, 5, "SVM", kernel = "radial")#train_model(train_validate_container, algorithm=c("SVM"), method = "C-classification", cross = 0, cost = 90, kernel = "linear")

# preparing container for test data
test_rows <- nrow(test_matrix)
test_container <- create_container(test_matrix, t(test_codes), trainSize=NULL, testSize=1:test_rows, virgin=FALSE)

# prediction
test_results <- classify_model(test_container, model)

# result all
test_data <- read.csv("test_set.txt", header = T, sep='\t')
results <- cbind(data.frame(test_data),data.frame(test_results))

# generating predict file for unseen test
#write.table(results,"predicted_classify_1_0_rich_IMb_heuristic.txt", sep='\t',row.names=F)

# getting performance
true_labels <- as.vector(results$CLASS)
predicted_labels <- as.vector(results$SVM_LABEL, mode = class(true_labels)) - 1

# accuracy
analyze <- (predicted_labels == true_labels)
accuracy <- length(analyze[analyze == TRUE])/length(true_labels)

# precision 1 
correct_1 <- (predicted_labels == true_labels) & (true_labels == 1)
cv_p1 <- length(correct_1[correct_1 == TRUE])/length(predicted_labels[predicted_labels == 1])

# recall 1
cv_r1 <- length(correct_1[correct_1 == TRUE])/length(true_labels[true_labels == 1])

# f 1
cv_f1 <- (2*cv_p1*cv_r1)/(cv_p1+cv_r1)

cat("acc. ", accuracy, " p1 ", cv_p1 , " r1 ", cv_r1, " f1 ", cv_f1)

# only top 10 trivia of selected movies
sorted_results <- results[order(-as.numeric(results$SVM_LABEL),-results$SVM_PROB),]
movie_result <- split(sorted_results, sorted_results$MOVIE)

top10Result <- NULL
total_correct_in_10 <- 0
for(i in 1:length(movie_result))
{
  top10Result <- rbind(data.frame(top10Result), data.frame(head(movie_result[[i]], 10)))
  thisMovie <- data.frame(head(movie_result[[i]], 10))
  correct_in_10 <- sum(thisMovie$CLASS)
  total_correct_in_10 <- total_correct_in_10 + correct_in_10
}
precision_in_10 <- total_correct_in_10/length(unique(sorted_results$MOVIE))
cat("p@10 : ", precision_in_10)

# writing result file
#write.table(top10Result, "top10_1_0_rich_classification.txt", sep='\t',row.names=F)