library(tm)
library(RTextTools);
data <- read.csv("trainData_Merged.txt", sep='\t', header=T)
data <- data[sample(nrow(data)),]

training_data <- data[-2]
training_codes <- data[2]

matrix <- create_matrix(training_data, language = "english", removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)
container <- create_container(matrix, t(training_codes), trainSize=1:5904, testSize=5905:7380, virgin=FALSE)

model <- train_model(container, algorithm=c("SVM"), method = "C-classification", cross = 0, cost = 100, kernel = "radial")
results <- classify_model(container, model)
analytics <- create_analytics(container, results)

#predicting
test_data <- read.csv("interstellar.txt", header=T)
test_matrix <- create_matrix(test_data, language = "english", removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)
test_container <- create_container(test_matrix, rep(1,320), testSize=1:320, virgin=TRUE)