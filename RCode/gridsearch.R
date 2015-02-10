# first generate train feature matrix, and validate feature matrix in svmlight format
# then run the following code to get optimum values of c and e

TEST_DATA_FILE_NAME <- "test_candidates_relaxed.txt";
max_n = 0;
max_c = 1;
max_e = 0.01;
cc <- NULL
ee <- NULL
nn <- NULL

for(c in seq(1,100,4))
{
  for(e in seq(0.01,1,0.04))
  {
    command <- capture.output(cat('./svm_rank_learn.exe -c ',c,' -e ',e,' rankTemp/train_features_svmLight.txt rankTemp/model_rank_1_4_IMDb')
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