library(tm)
library(RTextTools)
library(e1071)
library(gbm)

data <- read.csv("trainData_5K_richFeatures_rank.txt", sep='\t', header=T)
data <- data[sample(nrow(data)),]

train_validate_data <- data

train_validate_data$CLASS <- train_validate_data$RANK

train_validate_data$MOVIE <- NULL
train_validate_data$CLASS_V <- NULL
#train_validate_data$MOVIE_NAME_IMDB <- NULL
train_validate_data$INTERESTED <- NULL
train_validate_data$VOTED <- NULL
train_validate_data$LIKENESS_RATIO <- NULL
train_validate_data$FOG <- NULL
train_validate_data$RANK <- NULL

# name change of a column
names(train_validate_data)[names(train_validate_data) == 'MOVIE_NAME_IMDB'] <- 'MOVIE'

# tracking train_validate_data
train_validate_rows <- nrow(train_validate_data)

# HACK PART: add the unseen test part also
test_data <- read.csv("test_wiki_features.txt", header = T, sep = '\t')
#test_data$MOVIE <- NULL
test_data$FOG_INDEX <- NULL
combined_data <- rbind(train_validate_data, test_data)

combined_trivia <- combined_data["TRIVIA"]
combined_codes <- combined_data["CLASS"]

# Unigram words: combined for train, validate and test
combined_matrix <- create_matrix(combined_trivia, language = "english", stripWhitespace = TRUE, removeNumbers=FALSE, stemWords=TRUE, removePunctuation=TRUE, removeStopwords = TRUE, weighting=weightTfIdf)

# parse tree features: combined for train, validate and test
root_matrix <- create_matrix(combined_data["ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
subject_matrix <- create_matrix(combined_data["SUBJECT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
under_root_matrix <- create_matrix(combined_data["UNDER_ROOT_WORDS"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
all_linked_entities_matrix <- create_matrix(combined_data["ALL_LINKABLE_ENTITIES_PRESENT"], removePunctuation = FALSE, removeStopwords = FALSE, weighting = weightTf)
parse_features_matrix <- cbind(as.matrix(all_linked_entities_matrix), as.matrix(root_matrix), as.matrix(subject_matrix), as.matrix(under_root_matrix))

combined_matrix <- cbind(as.matrix(combined_matrix), as.matrix(parse_features_matrix))

# + frequency of superlative POS and comparative POS as features
combined_matrix <- cbind(combined_matrix, as.matrix(combined_data["superPOS"]))
combined_matrix <- cbind(combined_matrix, as.matrix(combined_data["compPOS"]))

# + frequency of different NERs
combined_matrix <- cbind(combined_matrix, as.matrix(combined_data[,c("MOVIE","PERSON","ORGANIZATION","DATE","LOCATION","MONEY","TIME")]))

addedFeatures <- c("PERSON","ORGANIZATION","DATE","LOCATION","MONEY","TIME","superPOS", "compPOS")

# converting frequencies to boolean presence
for(col in addedFeatures)
{
  index <- combined_matrix[,col] > 0
  combined_matrix[index,col] <- 1
}

# tracking breakpoint
test_start <- train_validate_rows+1
combined_rows <- nrow(combined_matrix)

# splitting combined_matrix
train_validate_matrix <- combined_matrix[1:train_validate_rows,]
test_matrix <- combined_matrix[test_start:combined_rows,]

# splitting combined_codes
train_validate_codes <- combined_codes[1:train_validate_rows,]
test_codes <- combined_codes[test_start:combined_rows,]

# preparing container for training and validating data
trainEnd <- round((4*train_validate_rows)/5)
validateStart <- trainEnd + 1
#train_validate_container <- create_container(train_validate_matrix, t(train_validate_codes), trainSize=1:trainEnd, testSize=validateStart:train_validate_rows, virgin=FALSE)

comMAT <- data.frame(cbind(train_validate_matrix, train_validate_codes))
trainMAT <- data.frame(comMAT[1:trainEnd,])
validateMAT <- data.frame(comMAT[validateStart:train_validate_rows,])

# training
#model <- train_model(train_validate_container, algorithm=c("SVM"), method = "C-classification", cross = 0, cost = 100, kernel = "linear")

ndcg <- function(x) {
  # x is a vector of relevance scores
  ideal_x <- rev(sort(x))
  DCG <- function(y) y[1] + sum(y[-1]/log(2:length(y), base = 2))
  DCG(x)/DCG(ideal_x)
}

gbmModel<-gbm( train_validate_codes ~ .
               , data = trainMAT
               #, distribution="bernoulli"
               , distribution = list( # loss function:
                 name='pairwise', # pairwise
                 metric="ndcg", # ranking metric: normalized discounted cumulative gain
                 group='MOVIE',
                 max.rank=5),
               , n.trees=100
               , interaction.depth = 8
               , n.minobsinnode = 20
               , shrinkage = 0.1
               , bag.fraction =0.5
               , train.fraction = 0.8,
               , verbose=TRUE)

results<-predict(gbmModel,validateMAT,n.trees=gbmModel$n.trees,type="response")