library(tm)
library(RTextTools);
data <- read.csv("trainData_withMovie.txt", sep='\t', header=T)
data <- data[sample(nrow(data)),]

name <- data[,1]
training_data <- data[2]
training_codes <- data[3]

matrix <- create_matrix(training_data, language = "english", removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf, toLower = TRUE)

name_id <- as.numeric(data$MOVIE)
mat <- cbind(name_id, as.matrix(matrix))

container <- create_container(mat, t(training_codes), trainSize=1:5904, testSize=5905:7380, virgin=FALSE)

models <- train_models(container, algorithm=c("SVM"), method = "C-classification", cross = 0, cost = 100, kernel = "linear")
results <- classify_models(container, models)
analytics <- create_analytics(container, results)

w = t(model$coefs) %*% model$SV
print(analytics@algorithm_summary)