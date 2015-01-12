library(tm)
library(RTextTools);
#data <- read.csv("trainData_5K_richFeatures.txt", sep='\t', header=T)
#data <- data[sample(nrow(data)),]
load("compareData.RData")
train_validate_data <- data
rm(data)

# tracking train_validate_data
train_validate_rows <- nrow(train_validate_data)

# HACK PART: add the unseen test part also
test_data <- read.csv("Test_Movies_wiki_Features.txt", header = T, sep = '\t')
combined_data <- rbind(train_validate_data, test_data)

# remove unused
rm(test_data, train_validate_data)

#name <- data[,"MOVIE_NAME_IMDB"]
combined_trivia <- combined_data["TRIVIA"]
combined_codes <- combined_data["CLASS"]

# Unigram words: combined for train, validate and test
combined_matrix <- create_matrix(combined_trivia, language = "english", stripWhitespace = TRUE, removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)
rm(combined_trivia)

# parse tree features: combined for train, validate and test
root_matrix <- create_matrix(combined_data["ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
subject_matrix <- create_matrix(combined_data["SUBJECT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
under_root_matrix <- create_matrix(combined_data["UNDER_ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
all_linked_entities_matrix <- create_matrix(combined_data["ALL_LINKABLE_ENTITIES_PRESENT"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
parse_features_matrix <- cbind(as.matrix(all_linked_entities_matrix), as.matrix(root_matrix), as.matrix(subject_matrix), as.matrix(under_root_matrix))
rm(root_matrix, subject_matrix, under_root_matrix, all_linked_entities_matrix)

combined_matrix <- cbind(as.matrix(combined_matrix), as.matrix(parse_features_matrix))
rm(parse_features_matrix)

# + frequency of superlative POS as feature
combined_matrix <- cbind(combined_matrix, as.matrix(combined_data["superPOS"]))

# + frequency of different NERs
combined_matrix <- cbind(combined_matrix, as.matrix(combined_data[,c("PERSON","ORGANIZATION","DATE","LOCATION","MONEY","TIME")]))

addedFeatures <- c("PERSON","ORGANIZATION","DATE","LOCATION","MONEY","TIME","superPOS")

# converting frequencies to boolean presence
for(col in addedFeatures)
{
  index <- combined_matrix[,col] > 0
  combined_matrix[index,col] <- 1
}
rm(col, index, addedFeatures)

# tracking breakpoint
test_start <- train_validate_rows+1
combined_rows <- nrow(combined_matrix)

# splitting combined_data
#train_validate_data <- combined_data[1:train_validate_rows,]
test_data <- combined_data[test_start:combined_rows,]
rm(combined_data)

# splitting combined_matrix
train_validate_matrix <- combined_matrix[1:train_validate_rows,]
test_matrix <- combined_matrix[test_start:combined_rows,]
rm(combined_matrix)

# splitting combined_codes
train_validate_codes <- combined_codes[1:train_validate_rows,]
test_codes <- combined_codes[test_start:combined_rows,]
rm(combined_codes, combined_rows, test_start)

# preparing container for training and validating data
trainEnd <- round((4*train_validate_rows)/5)
validateStart <- trainEnd + 1
train_validate_container <- create_container(train_validate_matrix, t(train_validate_codes), trainSize=1:trainEnd, testSize=validateStart:train_validate_rows, virgin=FALSE)
rm(train_validate_rows, trainEnd, validateStart, train_validate_matrix, train_validate_codes)

# training
model <- train_model(train_validate_container, algorithm=c("SVM"), method = "C-classification", cross = 0, cost = 100, kernel = "linear")

# getting analytics
validate_results <- classify_model(train_validate_container, model)
analytics <- create_analytics(train_validate_container, validate_results)
print(analytics@algorithm_summary)
rm(train_validate_container, analytics, validate_results)

# preparing container for test data
test_rows <- nrow(test_matrix)
test_container <- create_container(test_matrix, t(test_codes), trainSize=NULL, testSize=1:test_rows, virgin=FALSE)
rm(test_matrix, test_codes, test_rows)

#prediction
test_results <- classify_model(test_container, model)
rm(model, test_container)

#generating result file for unseen test
write.table(test_results,"Result_rich.txt", sep='\t',row.names=F)