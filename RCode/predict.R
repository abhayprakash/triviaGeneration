library(tm);
library(RTextTools);

# clean workspace
#rm(list = ls())

# load candidates
test_data <- read.csv("Test_Movies_wiki_Features.txt", header = T, sep = '\t')
test_trivia <- data["TRIVIA"]
test_codes <- data["CLASS"]

test_totalRows <- nrow(data)

# load model
load("rich.RData")

# unigram matrix
test_matrix <- create_matrix(test_data, language = "english", originalMatrix = matrix, stripWhitespace = TRUE, removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)

# parse tree features
root_matrix <- create_matrix(data["ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
subject_matrix <- create_matrix(data["SUBJECT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
under_root_matrix <- create_matrix(data["UNDER_ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
all_linked_entities_matrix <- create_matrix(data["ALL_LINKABLE_ENTITIES_PRESENT"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
parse_features_matrix <- cbind(as.matrix(all_linked_entities_matrix), as.matrix(root_matrix), as.matrix(subject_matrix), as.matrix(under_root_matrix))

test_matrix <- cbind(as.matrix(test_matrix), as.matrix(parse_features_matrix))

# + frequency of superlative POS as feature
test_matrix <- cbind(test_matrix, as.matrix(data["superPOS"]))

# + frequency of different NERs
test_matrix <- cbind(test_matrix, as.matrix(data[,c("PERSON","ORGANIZATION","DATE","LOCATION","MONEY","TIME")]))

addedFeatures <- c("PERSON","ORGANIZATION","DATE","LOCATION","MONEY","TIME","superPOS")

# converting frequencies to boolean presence
for(col in addedFeatures)
{
  index <- test_matrix[,col] > 0
  test_matrix[index,col] <- 1
}

# do prediction
test_container <- create_container(test_matrix, t(test_codes), trainSize=NULL, testSize=1:totalRows, virgin=TRUE)
results <- classify_model(test_container, model)

######################### random selection ################################
#randomResult <- NULL
#for (i in 1:length(movieNames))
#{
#  thisMovieData <- subset(data, MOVIE == movieNames[i])
  
#  selectedSentences <- head(thisMovieData[sample(nrow(thisMovieData)),],10)
#  randomResult <- rbind(result, selectedSentences)
#}