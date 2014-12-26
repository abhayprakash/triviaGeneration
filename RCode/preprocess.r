cleanAndMakeCorpus <- function(s, f)
{
  library(tm)
  data <- readLines(s)
  corp <- Corpus(VectorSource(data))
  #summary(corp)
  
  # cleaning
  a <- tm_map(corp, removePunctuation)
  a <- tm_map(a, stripWhitespace)
  a <- tm_map(a, content_transformer(tolower))
  a <- tm_map(a, removeWords, stopwords("english"))
  a <- tm_map(a, stemDocument, language = "english")
  
  writeCorpus(a, f)
  
  
  myTdm <- TermDocumentMatrix(a)
  
  # data cisualize by frequency
  library(wordcloud)
  wordcloud(a, scale=c(5,0.5), max.words=100, random.order=FALSE)
}