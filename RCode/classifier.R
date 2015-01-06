library(tm)
library(RTextTools);
data <- read.csv("trainData_5K_POS_NERcount.txt", sep='\t', header=T)
data <- data[sample(nrow(data)),]

#name <- data[,"MOVIE_NAME_IMDB"]
training_data <- data["TRIVIA"]
training_codes <- data["CLASS"]

totalRows <- nrow(data)
trainEnd <- round((4*totalRows)/5)
testStart <- trainEnd + 1

matrix <- create_matrix(training_data, language = "english", stripWhitespace = TRUE, removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)

# taking frequency of superlative POS as feature
mat <- cbind(as.matrix(matrix), data["superPOS"])
matrix <- mat


# training and testing
container <- create_container(matrix, t(training_codes), trainSize=1:trainEnd, testSize=testStart:totalRows, virgin=FALSE)
model <- train_model(container, algorithm=c("SVM"), method = "C-classification", cross = 0, cost = 100, kernel = "linear")
results <- classify_model(container, model)
analytics <- create_analytics(container, results)
print(analytics@algorithm_summary)

#ranking for features - NOTE: works only for linear kernel
w = t(model$coefs) %*% model$SV
features <- colnames(matrix)[w@ja]
weights <- w@ra
featureWeights <- data.frame(cbind(features, weights))
featureWeights$weights <- abs(as.numeric(as.character(featureWeights$weights)))
featureWeights <- featureWeights[with(featureWeights, order(-weights)),]
row.names(featureWeights) <- 1:nrow(featureWeights)

# get rank of superlative POS frequency
cat("Rank of superPOS: ", row.names(featureWeights[featureWeights$features=='superPOS',]), "/", ncol(matrix))
print(featureWeights[featureWeights$features=='superPOS',])

#predicting
test_data <- read.csv("interstellar.txt", header=T)
test_matrix <- create_matrix(test_data, language = "english", removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)
test_container <- create_container(test_matrix, rep(1,320), testSize=1:320, virgin=TRUE)