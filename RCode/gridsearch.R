library(tm)
library(RTextTools)

TRAIN_DATA_FILE_NAME <- "train_data_5Buckets.txt";
TEST_DATA_FILE_NAME <- "test_candidates_relaxed.txt";

ndcg <- function(x) {
  # x is a vector of relevance scores
  ideal_x <- rev(sort(x))
  DCG <- function(y) y[1] + sum(y[-1]/log(2:length(y), base = 2))
  DCG(x)/DCG(ideal_x)
}

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
test_data$BASE_superPOS <- NULL

combined_data <- rbind(train_validate_data[,!(colnames(train_validate_data) == "Movie.Roll.Num")], test_data)

combined_trivia <- combined_data["TRIVIA"]
combined_codes <- combined_data["GRADE"]

# Unigram words: combined for train, validate and test
combined_matrix <- as.matrix(create_matrix(combined_trivia, language = "english", stripWhitespace = TRUE, removeNumbers=FALSE, stemWords=TRUE, toLower = TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf))
#rm(combined_trivia)

# parse tree features: combined for train, validate and test
root_matrix <- create_matrix(combined_data["ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
subject_matrix <- create_matrix(combined_data["SUBJECT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
under_root_matrix <- create_matrix(combined_data["UNDER_ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
all_linked_entities_matrix <- create_matrix(combined_data["ALL_LINKABLE_ENTITIES_PRESENT"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
parse_features_matrix <- cbind(as.matrix(all_linked_entities_matrix), as.matrix(root_matrix), as.matrix(subject_matrix), as.matrix(under_root_matrix))

combined_matrix <- cbind(as.matrix(combined_matrix), as.matrix(parse_features_matrix))
#rm(parse_features_matrix, all_linked_entities_matrix, under_root_matrix, subject_matrix, root_matrix)

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

rm(col, index)

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
#rm(combined_data, combined_codes, combined_matrix, combined_rows, test_codes, test_start, train_validate_matrix)

# Cross validating within known result set -----------------
num_times = 5
num_validate = length(unique(train_validate_data$Movie.Roll.Num))/num_times
allMovies <- unique(train_validate_data$Movie.Roll.Num)
numMovies <- length(allMovies)
validateMovies_roll_num <- sample(1:numMovies, num_validate, replace = FALSE)
trainMovies_roll_num <- setdiff(allMovies, validateMovies_roll_num)
validate_index <- train_validate_data$Movie.Roll.Num %in% validateMovies_roll_num
train_index <- train_validate_data$Movie.Roll.Num %in% trainMovies_roll_num

# preparing container for training and validating data
trainMAT <- data.frame(comMAT[train_index,])
validateMAT <- data.frame(comMAT[validate_index,])

# writing feature matrix for train set and validate set 
write.table(trainMAT, "rankTemp/train_features.txt", sep = '\t', quote = F, row.names=F)
write.table(validateMAT, "rankTemp/validate_features.txt", sep = '\t', quote = F, row.names=F)

# call svmlight_format writer
system('java svmLight_FormatWriter rankTemp/train_features.txt rankTemp/train_features_svmLight.txt rankTemp/f2.txt');
system('java svmLight_FormatWriter rankTemp/validate_features.txt rankTemp/validate_features_svmLight.txt rankTemp/f3.txt');

max_n = 0;
max_c = 1;
max_e = 0.01;
cc <- NULL
ee <- NULL
nn <- NULL

for(c in seq(1,25,2))
{
  for(e in seq(0.01,1,0.02))
  {
    command <- capture.output(cat('./svm_rank_learn.exe -c ',c,' -e ',e,' rankTemp/train_features_svmLight.txt rankTemp/model_rank_1_4_IMDb'))
    system(command)
                              
    # predict on validate part
    system('./svm_rank_classify.exe rankTemp/validate_features_svmLight.txt rankTemp/model_rank_1_4_IMDb rankTemp/validation_predicted_rank_1_4.txt')
  
    # compare for validate part : predicted v/s actual
    predicted_validate <- read.csv("rankTemp/validation_predicted_rank_1_4.txt", sep = '\t', header = FALSE)
    train_validate_data <- read.csv(TRAIN_DATA_FILE_NAME, sep='\t', header=T)
    validate_data <- train_validate_data[validate_index,]
    result_validate <- cbind(validate_data, predicted_validate)
    
    # metric on validation set predictions
    sorted_result <- result_validate[order(-result_validate$V1),]
    
    total_ndcg_5_over_all_movies <- 0
    total_ndcg_10_over_all_movies <- 0
    total_precision_10_all_movies <- 0
    
    for(mv_name in unique(sorted_result$MOVIE))
    {
      this_movie <- sorted_result[sorted_result$MOVIE_NAME_IMDB == mv_name,]
      top_10_rank <- head(this_movie$GRADE, 10)
      top_5_rank <- head(this_movie$GRADE, 5)
      
      ndcg_10_this_movie <- ndcg(top_10_rank)
      
      total_ndcg_10_over_all_movies = total_ndcg_10_over_all_movies + ndcg_10_this_movie
    }
    
    ndcg_10_over_all_movies <- total_ndcg_10_over_all_movies/length(unique(sorted_result$MOVIE))
    
    if(ndcg_10_over_all_movies > max_n)
    {
      max_n <- ndcg_10_over_all_movies
      max_c <- c
      max_e <- e
    }
    
    nn <- c(nn, ndcg_10_over_all_movies)
    cc <- c(cc, c)
    ee <- c(ee, e)
  }
}

graph <- rbind(cc,ee,nn)
write.table(t(graph), "param_graph_validate.txt", row.names=F, quote=F, sep = '\t')
cat(max_c, " : ", max_e, "\n");