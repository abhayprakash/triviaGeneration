library(tm)
library(RTextTools);
data <- read.csv("trainData_withMovie.txt", sep='\t', header=T)
data <- data[sample(nrow(data)),]

name <- data[,1]
training_data <- data[2]
training_codes <- data[3]

matrix <- create_matrix(training_data, language = "english", removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, toLower = TRUE)

mat_name <- create_matrix(as.numeric(as.factor(name)))

mat <- cbind(mat_name, matrix)
container <- create_container(mat, t(training_codes), trainSize=1:5904, testSize=5905:7380, virgin=FALSE)

model <- train_model(container, algorithm=c("SVM"), method = "C-classification", cross = 0, cost = 100, kernel = "radial")
results <- classify_model(container, model)
analytics <- create_analytics(container, results)