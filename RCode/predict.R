# predicting
test_data <- read.csv("interstellar.txt", header=T)
test_matrix <- create_matrix(test_data, language = "english", removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)
test_container <- create_container(test_matrix, rep(1,320), testSize=1:320, virgin=TRUE)