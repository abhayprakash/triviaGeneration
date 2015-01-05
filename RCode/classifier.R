library(tm)
library(RTextTools);
data <- read.csv("trainData_5K.txt", sep='\t', header=T)
data <- data[sample(nrow(data)),]

name <- data[,"MOVIE_NAME_IMDB"]
training_data <- data["TRIVIA"]
training_codes <- data["CLASS"]

totalRows <- nrow(data)
trainEnd <- round((4*totalRows)/5)
testStart <- trainEnd + 1

print(trainEnd)

matrix <- create_matrix(training_data, language = "english", stripWhitespace = TRUE, removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)
container <- create_container(matrix, t(training_codes), trainSize=1:trainEnd, testSize=testStart:totalRows, virgin=FALSE)

model <- train_model(container, algorithm=c("SVM"), method = "C-classification", cross = 0, cost = 100, kernel = "linear")
results <- classify_model(container, model)
analytics <- create_analytics(container, results)

w = t(model$coefs) %*% model$SV
print(analytics@algorithm_summary)

#predicting
test_data <- read.csv("interstellar.txt", header=T)
test_matrix <- create_matrix(test_data, language = "english", removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)
test_container <- create_container(test_matrix, rep(1,320), testSize=1:320, virgin=TRUE)