# load packages
library(RCulr)
library(XML)

getText <- function(link){
  # download html
  html <- getURL(link, followlocation = TRUE)
  
  # parse html
  doc = htmlParse(html, asText=TRUE)
  plain.text <- xpathSApply(doc, "//p", xmlValue)
  cat(paste(plain.text, collapse = "\n"))
}