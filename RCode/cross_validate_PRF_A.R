cross_validate_SVM_PRFA <- function(container, nfold, method = "C-classification", cross = 0, cost = 100, kernel = "radial")
{
  extract_label_from_prob_names <- function(x) return(rownames(as.matrix(which.max(x))))
  alldata <- rbind(container@training_matrix, container@classification_matrix)
  allcodes <- as.factor(c(container@training_codes, container@testing_codes))
  rand <- sample(nfold, dim(alldata)[1], replace = T)
  
  cv_accuracy <- NULL
  cv_p1 <- NULL
  cv_r1 <- NULL
  cv_f1 <- NULL
  cv_p0 <- NULL
  cv_r0 <- NULL
  cv_f0 <- NULL

  for (i in sort(unique(rand))) {
    model <- svm(x = alldata[rand != i, ], y = allcodes[rand != i], method = method, cross = cross, cost = cost, kernel = kernel)
    pred <- predict(model, alldata[rand == i, ])
    
    # for comparision: original and predicted
    true_labels <- as.vector(allcodes[rand == i])
    predicted_labels <- as.vector(pred, mode = class(true_labels))
    
    # accuracy
    analyze <- predicted_labels == true_labels
    cv_accuracy[i] <- length(analyze[analyze == TRUE])/length(true_labels)
    
    # precision 1 
    correct_1 <- (predicted_labels == true_labels) & (true_labels == 1)
    cv_p1[i] <- length(correct_1[correct_1 == TRUE])/length(predicted_labels[predicted_labels == 1])
    
    # recall 1
    cv_r1[i] <- length(correct_1[correct_1 == TRUE])/length(true_labels[true_labels == 1])
    
    # f 1
    cv_f1[i] <- (2*cv_p1[i]*cv_r1[i])/(cv_p1[i]+cv_r1[i])
    
    # precision 0
    correct_0 <- (predicted_labels == true_labels) & (true_labels == 0)
    cv_p0[i] <- length(correct_0[correct_0 == TRUE])/length(predicted_labels[predicted_labels == 0])
    
    # recall 0
    cv_r0[i] <- length(correct_0[correct_0 == TRUE])/length(true_labels[true_labels == 0])
    
    # f 1
    cv_f0[i] <- (2*cv_p0[i]*cv_r0[i])/(cv_p0[i]+cv_r0[i])
    
    cat("Fold ", i, " Out of Sample Accuracy", " = ", cv_accuracy[i], "\n", sep = "")
    cat("Precision 0: ", cv_p0[i], " Recall 0: ", cv_r0[i]," F 0: ", cv_f0[i], "\n", sep = "")
    cat("Precision 1: ", cv_p1[i], " Recall 1: ", cv_r1[i]," F 1: ", cv_f1[i], "\n", sep = "")
  }
  cat("\nMean Accuracy: ", mean(cv_accuracy), "\n")
  cat("Mean Precison 0: ", mean(cv_p0), " Mean Recall 0: ", mean(cv_r0), "Mean F 0: ", mean(cv_f0), "\n")
  cat("Mean Precison 1: ", mean(cv_p1), " Mean Recall 1: ", mean(cv_r1), "Mean F 1: ", mean(cv_f1), "\n")
}