library(tm)
library(RTextTools)

TRAIN_DATA_FILE_NAME <- "train_data_more_movies.txt";
TEST_DATA_FILE_NAME <- "test_set_clean.txt";

do_cross_validate <- TRUE

#reading and selecting columns in train set
train_validate_data <- read.csv(TRAIN_DATA_FILE_NAME, sep='\t', header=T)

# removing non required columns
#train_validate_data$Movie.Roll.Num <- NULL
train_validate_data$INTERESTED <- NULL
train_validate_data$VOTED <- NULL
train_validate_data$LIKENESS_RATIO <- NULL
train_validate_data$BASE_superPOS <- NULL

# in case of name difference, name change of the movie column
names(train_validate_data)[names(train_validate_data) == 'MOVIE_NAME_IMDB'] <- 'MOVIE'

# tracking train_validate_data
train_validate_rows <- nrow(train_validate_data)

# HACK PART: add the unseen test part also
test_data <- read.csv(TEST_DATA_FILE_NAME, header = T, sep = '\t')
test_data$count_boring <- NULL
test_data$count_interesting <- NULL
test_data$count_veryInteresting <- NULL

combined_data <- rbind(train_validate_data[,!(colnames(train_validate_data) == "Movie.Roll.Num")], test_data)

combined_trivia <- combined_data["TRIVIA"]
combined_codes <- combined_data["GRADE"]

# Unigram words: combined for train, validate and test
combined_matrix <- create_matrix(combined_trivia, language = "english", stripWhitespace = TRUE, removeNumbers=FALSE, stemWords=TRUE, toLower = TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)
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

# matrix for all known result rows
comMAT <- data.frame(cbind(train_validate_matrix, train_validate_codes))
rm(combined_data, combined_codes, combined_matrix, combined_rows, test_codes, test_start, train_validate_matrix)

# Cross validating within known result set -----------------
num_times = 5;
indiv_p <- NULL;
total_P_in_10 = 0;
if(do_cross_validate)
for(i in 1:num_times)
{
  # forming validate set (50 movies -> all trivia)
  allMovies <- unique(train_validate_data$Movie.Roll.Num)
  numMovies <- length(allMovies)
  validateMovies_roll_num <- sample(1:numMovies, 111, replace = FALSE)
  trainMovies_roll_num <- setdiff(allMovies, validateMovies_roll_num)
  validate_index <- train_validate_data$Movie.Roll.Num %in% validateMovies_roll_num
  train_index <- train_validate_data$Movie.Roll.Num %in% validateMovies_roll_num

  # preparing container for training and validating data
  trainMAT <- data.frame(comMAT[train_index,])
  validateMAT <- data.frame(comMAT[validate_index,])

  # writing feature matrix for train set and validate set 
  write.table(trainMAT, "rankTemp/train_features.txt", sep = '\t', quote = F, row.names=F)
  write.table(validateMAT, "rankTemp/validate_features.txt", sep = '\t', quote = F, row.names=F)

  # call svmlight_format writer
  system('java svmLight_FormatWriter rankTemp/train_features.txt rankTemp/train_features_svmLight.txt');
  system('java svmLight_FormatWriter rankTemp/validate_features.txt rankTemp/validate_features_svmLight.txt');

  # create model from train part
  system('./svm_rank_learn.exe -c 3 rankTemp/train_features_svmLight.txt rankTemp/model_rank_1_4_IMDb')
  
  # predict on validate part
  system('./svm_rank_classify.exe rankTemp/validate_features_svmLight.txt rankTemp/model_rank_1_4_IMDb rankTemp/validation_predicted_rank_1_4.txt')
  
  # compare for validate part : predicted v/s actual
  predicted_validate <- read.csv("rankTemp/validation_predicted_rank_1_4.txt", sep = '\t', header = FALSE)
  train_validate_data <- read.csv(TRAIN_DATA_FILE_NAME, sep='\t', header=T)
  validate_data <- train_validate_data[validate_index,]
  result_validate <- cbind(validate_data, predicted_validate)
  
  # metric on validation set predictions
  sorted_result <- result_validate[order(-result_validate$V1),]
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
  indiv_p <- c(indiv_p, precision_in_10)
  total_P_in_10 = total_P_in_10 + precision_in_10;
}
print(indiv_p);
cv_value <- total_P_in_10/num_times;
cat("CV Avg. P@10: ", cv_value);

# Final prediction on unseen test -------------------------
#writing features in table format
write.table(comMAT, "rankTemp/all_train_features.txt", sep = '\t', quote = F, row.names=F)
write.table(test_matrix, "rankTemp/test_features.txt", sep = '\t', quote = F, row.names=F)

# converting to svm light format
system('java svmLight_FormatWriter rankTemp/test_features.txt rankTemp/test_features_svmLight.txt');
system('java svmLight_FormatWriter rankTemp/all_train_features.txt rankTemp/all_train_features_svmLight.txt');

# removing unused
rm(comMAT, test_matrix, trainMAT, validateMAT)

# creating model with all available data
system('./svm_rank_learn.exe -c 3 rankTemp/all_train_features_svmLight.txt rankTemp/model_all_train_rank_1_4_IMDb')
#system('java -jar RankLib.jar -train rankTemp/all_train_features_svmLight.txt -ranker 0 -metric2t P@10 -tvs 0.8 -save rankTemp/RankLib_model_all_train_1_4_IMDb -test rankTemp/test_features_svmLight.txt')

# predict on test set
#system('./svm_rank_classify.exe rankTemp/test_features_svmLight.txt rankTemp/model_rank_1_4_IMDb rankTemp/test_predicted_rank_1_4.txt')
system('./svm_rank_classify.exe rankTemp/test_features_svmLight.txt rankTemp/model_all_train_rank_1_4_IMDb rankTemp/test_predicted_rank_1_4.txt')

# generate result file for test set
test_file <- read.csv(TEST_DATA_FILE_NAME, sep = '\t', header = TRUE)
predicted_test <- read.csv("rankTemp/test_predicted_rank_1_4.txt", sep = '\t', header = FALSE)
result_all <- cbind(test_file, predicted_test)

# writing all the sentences in test set
write.table(result_all, "result_all_clean.txt", sep = '\t', row.names = F, quote = F)

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
cat("CV Avg. P@10: ", cv_value);
cat("TEST p@10 : ", precision_in_10);

# writing result file
write.table(top10Result, "result_top10.txt", sep='\t',row.names=F)