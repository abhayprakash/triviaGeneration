library(tm)
library(RTextTools)

#reading and selecting columns in train set
train_validate_data <- read.csv("trainData_variation_rank_1_4.txt", sep='\t', header=T)
train_validate_data$Movie.Roll.Num <- NULL
train_validate_data$INTERESTED <- NULL
train_validate_data$VOTED <- NULL
train_validate_data$LIKENESS_RATIO <- NULL

# in case of name difference, name change of the movie column
names(train_validate_data)[names(train_validate_data) == 'MOVIE_NAME_IMDB'] <- 'MOVIE'

# tracking train_validate_data
train_validate_rows <- nrow(train_validate_data)

# HACK PART: add the unseen test part also
test_data <- read.csv("test_set_extremely_clean.txt", header = T, sep = '\t')
test_data$count_boring <- NULL
test_data$count_interesting <- NULL
test_data$count_veryInteresting <- NULL

combined_data <- rbind(train_validate_data, test_data)

# removing unused
rm(test_data, train_validate_data)

combined_trivia <- combined_data["TRIVIA"]
combined_codes <- combined_data["GRADE"]

# Unigram words: combined for train, validate and test
combined_matrix <- create_matrix(combined_trivia, language = "english", stripWhitespace = TRUE, removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)
rm(combined_trivia)

# parse tree features: combined for train, validate and test
root_matrix <- create_matrix(combined_data["ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
subject_matrix <- create_matrix(combined_data["SUBJECT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
under_root_matrix <- create_matrix(combined_data["UNDER_ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
all_linked_entities_matrix <- create_matrix(combined_data["ALL_LINKABLE_ENTITIES_PRESENT"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
parse_features_matrix <- cbind(as.matrix(all_linked_entities_matrix), as.matrix(root_matrix), as.matrix(subject_matrix), as.matrix(under_root_matrix))

combined_matrix <- cbind(as.matrix(combined_matrix), as.matrix(parse_features_matrix))
rm(parse_features_matrix, all_linked_entities_matrix, under_root_matrix, subject_matrix, root_matrix)

# + frequency of superlative POS and comparative POS as features
combined_matrix <- cbind(combined_matrix, as.matrix(combined_data["superPOS"]))
#combined_matrix <- cbind(combined_matrix, as.matrix(combined_data["compPOS"]))

# + frequency of different NERs
combined_matrix <- cbind(combined_matrix, as.matrix(combined_data[,c("MOVIE","PERSON","ORGANIZATION","DATE","LOCATION","MONEY","TIME", "FOG", "Contradict")]))

addedFeatures <- c("PERSON","ORGANIZATION","DATE","LOCATION","MONEY","TIME","superPOS", "Contradict")#, "compPOS"

# converting frequencies to boolean presence
for(col in addedFeatures)
{
  index <- combined_matrix[,col] > 0
  combined_matrix[index,col] <- 1
}

index <- (combined_matrix[,"FOG"] < 7)
combined_matrix[index, "FOG"] <- as.factor(1)

index <- (combined_matrix[,"FOG"] >= 7)
combined_matrix[index,"FOG"] <- as.factor(2)

index <- (combined_matrix[,"FOG"] >= 15)
combined_matrix[index,"FOG"] <- as.factor(3)

rm(addedFeatures, col, index)

# tracking breakpoint
test_start <- train_validate_rows+1
combined_rows <- nrow(combined_matrix)

# splitting combined_matrix
train_validate_matrix <- data.frame(combined_matrix[1:train_validate_rows,])
test_matrix <- data.frame(combined_matrix[test_start:combined_rows,])

# splitting combined_codes
train_validate_codes <- combined_codes[1:train_validate_rows,]
test_codes <- combined_codes[test_start:combined_rows,]

# preparing container for training and validating data
trainEnd <- floor((4*train_validate_rows)/5)
validateStart <- trainEnd + 1

comMAT <- data.frame(cbind(train_validate_matrix, train_validate_codes))
trainMAT <- data.frame(comMAT[1:trainEnd,])
validateMAT <- data.frame(comMAT[validateStart:train_validate_rows,])

rm(combined_data, combined_codes, combined_matrix, combined_rows, test_codes, test_start, train_validate_matrix)

# writing feature matrix for train set and validate set for doing metrics by 5 fold cv 
write.table(trainMAT, "train_features.txt", sep = '\t', quote = F, row.names=F)
write.table(validateMAT, "validate_features.txt", sep = '\t', quote = F, row.names=F)
write.table(comMAT, "all_train_features.txt", sep = '\t', quote = F, row.names=F)

# writing test set features
write.table(test_matrix, "test_features.txt", sep = '\t', quote = F, row.names=F)

# call svmlight_format writer
system('java svmLight_FormatWriter train_features.txt train_features_svmLight.txt');
system('java svmLight_FormatWriter validate_features.txt validate_features_svmLight.txt');
system('java svmLight_FormatWriter test_features.txt test_features_svmLight.txt');
system('java svmLight_FormatWriter all_train_features.txt all_train_features_svmLight.txt');

# removing unused
rm(comMAT, test_matrix, trainMAT, validateMAT)

# create model from train part
system('./svm_rank_learn.exe -c 3 train_features_svmLight.txt model_rank_1_4_IMDb')

# creating model with all available data
system('./svm_rank_learn.exe -c 3 all_train_features_svmLight.txt model_all_train_rank_1_4_IMDb')

# predict on validate part
system('./svm_rank_classify.exe validate_features_svmLight.txt model_rank_1_4_IMDb validation_predicted_rank_1_4.txt')

# compare for validate part : predicted v/s actual
predicted_validate <- read.csv("validation_predicted_rank_1_4.txt", sep = '\t', header = FALSE)
train_validate_codes <- data.frame(train_validate_codes)
actual_validate <- train_validate_codes[validateStart:train_validate_rows,]
compare_validate <- cbind(actual_validate, predicted_validate)

# predict on test set
system('./svm_rank_classify.exe test_features_svmLight.txt model_all_train_rank_1_4_IMDb test_predicted_rank_1_4.txt')

# generate result file for test set
test_file <- read.csv("test_set_extremely_clean.txt", sep = '\t', header = TRUE)
predicted_test <- read.csv("test_predicted_rank_1_4.txt", sep = '\t', header = FALSE)
result_all <- cbind(test_file, predicted_test)

# writing all the sentences in test set
write.table(result_all, "result_all_extremely_clean.txt", sep = '\t', row.names = F, quote = F)

# getting top 10 from each
sorted_result <- result_all[order(-result_all$V1),]
movie_result <- split(sorted_result, sorted_result$MOVIE)

top10Result <- NULL
total_correct_in_10 <- 0
for(i in 1:length(movie_result))
{
  top10Result <- rbind(data.frame(top10Result), data.frame(head(movie_result[[i]], 10)))
  thisMovie <- data.frame(head(movie_result[[i]], 10))
  correct_in_10 <- sum(thisMovie$CLASS)
  total_correct_in_10 <- total_correct_in_10 + correct_in_10
}
precision_in_10 <- total_correct_in_10/length(unique(sorted_result$MOVIE))
cat("p@10 : ", precision_in_10)

# writing result file
write.table(top10Result, "temp_result_top10_rank_svm_test_set_extremely_clean.txt", sep='\t',row.names=F)