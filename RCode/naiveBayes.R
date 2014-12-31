# load data
data <- read.csv("data.txt", sep='\t', header=T)

# see data
data

# testing syntax for sampling
sample(5)

# sampling data
data <- data[sample(nrow(data)),]

# seeing data
data

# text selection
data.text <- data[2]

# seeing selected text
data.text

# label selection
data.label <- data[3]
data.product <- data[1]

# taking help
??create_matrix

# creating matrix with pre-processing
mat = create_matrix(data.text, language = "english", removeStopwords = TRUE, removePunctuation=TRUE, removeNumbers = FALSE, stemWords = TRUE, tm::weightTfIdf)

# see the mat
mat

# see the actual matrix format
as.matrix(mat)

#library used for "naive bayes"
library(e1071)

# allocating in matrix format
mat = as.matrix(mat)

mat_pro = create_matrix(data.product, language = "english", removeStopwords = TRUE, removePunctuation=TRUE, removeNumbers = FALSE, stemWords = TRUE, tm::weightTfIdf)
mat_pro = as.matrix(mat_pro)
mat = cbind(mat_pro, mat)

# ACTUAL TRAINING THE MODEL with 12 rows
classifier = naiveBayes(mat[1:12, ], as.factor(data.label[1:12,])) # ..(feature, class_label)

# predicting with 6 rows
predicted = predict(classifier, mat[13:18, ])

# see the predicted labels
predicted

#build confusion matrix
table(data.label[13:18,], predicted)
