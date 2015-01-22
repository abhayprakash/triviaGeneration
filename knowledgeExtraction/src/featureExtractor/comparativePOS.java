/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package featureExtractor;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import static featureExtractor.NLPFeatures.bw_root;
import static featureExtractor.NLPFeatures.resultFile_Root;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Abhay Prakash
 */
public class comparativePOS {
    static StanfordCoreNLP pipeline;
    static BufferedWriter bw;
    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        String resultComp = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\anotherSelected5k\\hack_predict\\resultComp_POS.txt";
        FileWriter fw = new FileWriter(resultComp);
        bw = new BufferedWriter(fw);
        
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos");
        pipeline = new StanfordCoreNLP(props);
        
        String inputFilePath = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\anotherSelected5k\\hack_predict\\testMovies_wiki_trivia.txt";
        FileReader inputFile = new FileReader(inputFilePath);
        BufferedReader bufferReader = new BufferedReader(inputFile);
        String input;
        int lineNum = 0;
        //input = bufferReader.readLine();
        while((input = bufferReader.readLine()) != null)
        {
            processLine(input, lineNum);
            System.out.println(lineNum);
            lineNum++;
            //break;
        }
        bw.close();
    }
    
    static void processLine(String text,int lineId) throws IOException
    {
        bw.write(Integer.toString(lineId));
        int comparativePOS = 0;
        try{
            Annotation document = new Annotation(text);
            pipeline.annotate(document);
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap sentence : sentences) {
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    if(pos.equals("JJR") || pos.equals("RBR"))
                        comparativePOS++;
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("IGNORED:");
        }
        
        bw.write("\t" + comparativePOS + "\n");
    }
}
