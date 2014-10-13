/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knowledgeextraction;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Abhay Prakash
 */
public class KnowledgeExtraction {

    /**
     * @param args the command line arguments
     */
    static String filePath = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\Sachin_Tendulkar.txt";
    static String resultFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\result.txt";
    static String imdFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\imd.txt";
    
    public static void main(String[] args) throws IOException {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile));
        BufferedWriter wrimd = new BufferedWriter(new FileWriter(imdFile));
        
        String text;
        while((text = reader.readLine()) != null)
        {
            Annotation document = new Annotation(text);
            pipeline.annotate(document);
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
            
            for (CoreMap sentence : sentences) {
                //System.out.println("Sentence: " + sentence);
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    String word = token.get(CoreAnnotations.TextAnnotation.class);
                    //System.out.println("word: "+word + " pos: "+pos);
                    
                    if(pos.equals("RBS") || pos.equals("JJS") || word.equals("only"))
                    {
                        writer.write(sentence+"\n");
                        //System.out.println(sentence);
                        break;
                    }
                    /*
                    if(pos.contains("VB"))
                    {
                        wrimd.write(lemma + " --> " + sentence + "\n");
                    }
                    */
                    //String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                }
            }
        }
        writer.close();
        wrimd.close();
        reader.close();
    }
}
