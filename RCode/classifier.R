library(tm)
library(RTextTools);
data <- read.csv("trainData_5K_richFeatures.txt", sep='\t', header=T)
data <- data[sample(nrow(data)),]

#name <- data[,"MOVIE_NAME_IMDB"]
training_data <- data["TRIVIA"]
training_codes <- data["CLASS"]

totalRows <- nrow(data)
trainEnd <- round((4*totalRows)/5)
testStart <- trainEnd + 1

# Unigram words
matrix <- create_matrix(training_data, language = "english", stripWhitespace = TRUE, removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)

# parse tree features
root_matrix <- create_matrix(data["ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
subject_matrix <- create_matrix(data["SUBJECT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
under_root_matrix <- create_matrix(data["UNDER_ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
all_linked_entities_matrix <- create_matrix(data["ALL_LINKABLE_ENTITIES_PRESENT"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
matrix <- cbind(matrix, as.matrix(all_linked_entities_matrix), as.matrix(root_matrix), as.matrix(subject_matrix), as.matrix(under_root_matrix))

# + frequency of superlative POS as feature
matrix <- cbind(as.matrix(matrix), data["superPOS"])

# + frequency of different NERs
matrix <- cbind(as.matrix(matrix), data[,c("PERSON","ORGANIZATION","DATE","LOCATION","MONEY","TIME")])

addedFeatures <- c("PERSON","ORGANIZATION","DATE","LOCATION","MONEY","TIME","superPOS")

# converting frequencies to boolean presence
for(col in addedFeatures)
{
  index <- matrix[,col] > 0
  matrix[index,col] <- 1
}

############ training and testing
container <- create_container(matrix, t(training_codes), trainSize=1:trainEnd, testSize=testStart:totalRows, virgin=FALSE)
model <- train_model(container, algorithm=c("SVM"), method = "C-classification", cross = 0, cost = 100, kernel = "linear")
results <- classify_model(container, model)
analytics <- create_analytics(container, results)
print(analytics@algorithm_summary)

############# ranking for features - NOTE: works only for linear kernel
w = t(model$coefs) %*% model$SV
features <- colnames(matrix)[w@ja]
weights <- w@ra
featureWeights <- data.frame(cbind(features, weights))
featureWeights$weights <- abs(as.numeric(as.character(featureWeights$weights)))
featureWeights <- featureWeights[with(featureWeights, order(-weights)),]
row.names(featureWeights) <- 1:nrow(featureWeights)

# get rank of features
cat("Total num of NON-ZERO features: ", nrow(featureWeights) , "/" , ncol(matrix))
print(subset(featureWeights, features %in% addedFeatures))

# predicting
test_data <- read.csv("interstellar.txt", header=T)
test_matrix <- create_matrix(test_data, language = "english", removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)
test_container <- create_container(test_matrix, rep(1,320), testSize=1:320, virgin=TRUE)