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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Abhay Prakash
 */
public class KnowledgeExtraction {

    /**
     * @param args the command line arguments
     */
    static String filePath = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\";
    static String resultFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Results\\result";
    static String naiveFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Results\\naive";
    static String imdFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Results\\imd";
    static String modelFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Results\\modelFile.txt";
    
    static double CONST_K = 0.1;
    
    static List<String> Entities = new ArrayList<String>();
    static HashMap<String, HashMap<String, List<String> > > EntityToAttributeToSentenceList = new HashMap<>();
    static HashMap<String, List<String> > AttributeToEntityList = new HashMap<>();
    
    public static void main(String[] args) throws IOException {
        Entities.add("Sachin_Tendulkar");
        Entities.add("Sourav_Ganguly");
        Entities.add("Rahul_Dravid");
        Entities.add("Virender_Sehwag");
        Entities.add("Mahendra_Singh_Dhoni");
        Entities.add("V._V._S._Laxman");
        Entities.add("Yuvraj_Singh");
        Entities.add("Virat_Kohli");
        Entities.add("Gautam_Gambhir");
        Entities.add("Suresh_Raina");
        Entities.add("Irfan_Pathan");
        Entities.add("Mohammad_Azharuddin");
        
        GetEntityAttributeGraph();
        
        String targetEntity = Entities.get(0);
        
        List<String> interestingFacts = GenerateAndReturnInterestingFacts(targetEntity);
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile+ targetEntity + ".txt"));
        for(String s: interestingFacts){
            writer.write(s+"\n");
        }
        writer.close();
    }
    
    static void GetEntityAttributeGraph() throws IOException {
        File objectFile = new File(modelFile);
        
        if(!objectFile.exists()){
            System.out.println("new model being generated");
            BuildEntityAttributeGraph();

            FileOutputStream fos = new FileOutputStream(modelFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(EntityToAttributeToSentenceList);
            oos.writeObject(AttributeToEntityList);
            oos.close();
            fos.close();
        }
        else{
            System.out.println("Loading earlier model . . .");
            try{
                FileInputStream fis = new FileInputStream(modelFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                EntityToAttributeToSentenceList = (HashMap<String, HashMap<String, List<String> > >) ois.readObject();
                AttributeToEntityList = (HashMap<String, List<String> >) ois.readObject();
                ois.close();
                fis.close();
            } catch (IOException e) {            
            } catch (ClassNotFoundException e) {            
            }
        }
    }
    
    static List<String> GenerateAndReturnInterestingFacts(String targetEntity){
        System.out.println("Generating facts");
        List<String> facts = new ArrayList<>();
        facts.add("Target Entity has these but not others ------------------------------------------------------");
        for(String attribute: EntityToAttributeToSentenceList.get(targetEntity).keySet()){
            if(AttributeToEntityList.get(attribute).size() <= Entities.size() * CONST_K){
                facts.addAll(getSentences(targetEntity, attribute));
            }
        }
        
        facts.add("Target Entity does not has these but not others have ------------------------------------------------------");
        for(String attribute: AttributeToEntityList.keySet()){
            if(EntityToAttributeToSentenceList.get(targetEntity).keySet().contains(attribute) == false){
                if(AttributeToEntityList.get(attribute).size() >= (Entities.size() * (1.0-CONST_K))){
                    facts.add(attribute);
                }
            }
        }
        System.out.println("Generating facts : done");
        return facts;
    }
    
    static List<String> getSentences(String entity, String attribute){
        System.out.println("getting sentences");
        List<String> sentences = EntityToAttributeToSentenceList.get(entity).get(attribute);
        return sentences;
    }
    
    static void BuildEntityAttributeGraph() throws IOException {
        System.out.println("Building entity attribute graph");
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
        for(String name: Entities)
        {
            System.out.println("Building entity attribute graph for " + name);
            BufferedReader reader = new BufferedReader(new FileReader(filePath + name + ".txt"));
            //BufferedWriter NaiveWriter = new BufferedWriter(new FileWriter(naiveFile+ name + ".txt"));
            //BufferedWriter wrimd = new BufferedWriter(new FileWriter(imdFile+ name + ".txt"));
            
            if(EntityToAttributeToSentenceList.containsKey(name) == false)
                EntityToAttributeToSentenceList.put(name, new HashMap<>());
            
            String text = reader.readLine();

            Annotation document = new Annotation(text);
            pipeline.annotate(document);
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
            
            for (CoreMap sentence : sentences) {
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    String lemmatizedWord = token.get(CoreAnnotations.LemmaAnnotation.class);
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    String word = token.get(CoreAnnotations.TextAnnotation.class);
                    /*
                    if(pos.equals("RBS") || pos.equals("JJS") || word.equals("only"))
                    {
                        NaiveWriter.write(sentence+"\n");
                    }
                    */
                    if(pos.contains("VB"))
                    {
                        if(EntityToAttributeToSentenceList.get(name).containsKey(lemmatizedWord) == false)
                            EntityToAttributeToSentenceList.get(name).put(lemmatizedWord, new ArrayList<>());
                        
                        EntityToAttributeToSentenceList.get(name).get(lemmatizedWord).add(sentence.toString());
                        
                        if(AttributeToEntityList.containsKey(lemmatizedWord) == false)
                            AttributeToEntityList.put(lemmatizedWord, new ArrayList<>());
                        
                        if(AttributeToEntityList.get(lemmatizedWord).contains(name) == false)
                            AttributeToEntityList.get(lemmatizedWord).add(name);
                    }
                }
            }
            //NaiveWriter.close();
            //wrimd.close();
            reader.close();
        }
    }
}
