library(e1071)
data <- read.csv("trainData_Merged.txt", sep='\t', header=T)
data <- data[sample(nrow(data)),]
training_data <- data[-2]
training_codes <- data[2]
matrix <- create_matrix(training_data, language = "english", removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)
mat = as.matrix(matrix)
classifier = naiveBayes(mat[1:5904, ], as.factor(t(training_codes)[1:5904]))
predicted = predict(classifier, mat[5905:7380, ])
table(t(training_codes)[5905:7380], predicted)