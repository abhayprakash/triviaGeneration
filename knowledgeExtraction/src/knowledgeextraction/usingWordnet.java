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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Abhay Prakash
 */
public class usingWordnet {

    /**
     * @param args the command line arguments
     */
    static String filePath = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\";
    static String resultFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Results\\result";
    static String naiveFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Results\\naive";
    static String imdFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Results\\imd";
    static String modelFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Results\\modelFile.txt";
    static String graphFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Results\\GraphFile";
    
    static Boolean forceTrain = false;
    static double CONST_K = 0.1;
    
    static List<String> Entities = new ArrayList<String>();
    static HashMap<String, HashMap<String, List<String> > > EntityToAttributeToSentenceList = new HashMap<>();
    static HashMap<String, List<String> > AttributeToEntityList = new HashMap<>();
    
    static void PopulateEntityList(){
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
    }
    
    public static void main(String[] args) throws IOException {
        PopulateEntityList();        
        String targetEntity = Entities.get(2);
        
        // change only below two
        GetEntityAttributeGraph();        
        printGraph();
        List<String> interestingFacts = GenerateAndReturnInterestingFacts(targetEntity);
        // no need to change
        printResult(interestingFacts, targetEntity);//*/
        generateGEXF();
    }
    
    static void generateGEXF() throws IOException{
        /*
        <?xml version="1.0" encoding="UTF-8"?>
        <gexf xmlns="http://www.gexf.net/1.2draft" version="1.2">
            <graph mode="static" defaultedgetype="directed">
                <nodes>
                    <node id="0" label="Hello" />
                    <node id="1" label="Word" />
                </nodes>
                <edges>
                    <edge id="0" source="0" target="1" />
                </edges>
            </graph>
        </gexf>
        */
        PrintWriter writer = new PrintWriter(graphFile+".gexf", "UTF-8");
        
        writer.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<gexf xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\">\n" +
                     "    <graph mode=\"static\" defaultedgetype=\"directed\">\n" +
                     "        <nodes>\n");
        
        BufferedReader reader = new BufferedReader(new FileReader(graphFile + ".txt"));
        String line;
        while((line = reader.readLine()) != null)
        {
            String[] strs = line.trim().split("\\s+");
            writer.println("            <node id=\""+ strs[0]  + "\" label=\"Entity\" />");
            writer.println("            <node id=\""+ strs[1]  + "\" label=\"Attribute\" />");
        }
        reader.close();
        writer.println("        </nodes>");
        writer.println("        <edges>");
        int count = 0;
        reader = new BufferedReader(new FileReader(graphFile + ".txt"));
        while((line = reader.readLine()) != null)
        {
            String[] strs = line.trim().split("\\s+");
            writer.println("            <edge id=\""+count+"\" source=\""+strs[0]+"\" target=\""+strs[1]+"\" />");
            count++;
        }
        reader.close();
        writer.println("        </edges>");
        writer.println("    </graph>");
        writer.println("</gexf>");
        writer.close();
    }
    
    static void printGraph() throws IOException {
        //static HashMap<String, List<String> >
        PrintWriter writer = new PrintWriter(graphFile+".txt", "UTF-8");
        Iterator it = AttributeToEntityList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            for(String ent: (List<String>) pairs.getValue())
            {
                writer.println(ent+"\t"+pairs.getKey());
            }
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            //it.remove(); // avoids a ConcurrentModificationException
        }
        writer.close();
    }
    
    static void printResult(List<String> interestingFacts, String targetEntity) throws IOException {
        PrintWriter writer = new PrintWriter(resultFile + targetEntity + ".txt", "UTF-8");
        //BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile+ targetEntity + ".txt"));
        HashMap<String, Boolean> appeared = new HashMap<>();
        for(String s: interestingFacts){
            if(!appeared.containsKey(s)){
                writer.println(s);
                appeared.put(s, true);
            }
        }
        writer.close();
    }
    
    static void GetEntityAttributeGraph() throws IOException {
        File objectFile = new File(modelFile);
        
        if(forceTrain || !objectFile.exists()){
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
    
    static List<String> GenerateAndReturnInterestingFacts(String targetEntity) throws IOException {
        System.out.println("Generating facts");
        List<String> facts = new ArrayList<>();
        
        facts.add("Target Entity has these but not others ------------------------------------------------------");
        facts.add("    ");
        for(String attribute: EntityToAttributeToSentenceList.get(targetEntity).keySet()){
            if(AttributeToEntityList.get(attribute).size() <= Entities.size() * CONST_K){
                for(String s: getSentences(targetEntity, attribute))
                    facts.add(attribute + "\t\t:\t" + s);
            }
        }
        
        facts.add("  ");
        facts.add("   ");
        facts.add("Target Entity does not has these but others have ------------------------------------------------------");
        facts.add("    ");
        for(String attribute: AttributeToEntityList.keySet()){
            if(EntityToAttributeToSentenceList.get(targetEntity).keySet().contains(attribute) == false){
                if(AttributeToEntityList.get(attribute).size() >= (Entities.size() * (1.0-CONST_K))){
                    facts.add(attribute);
                }
            }
        }
        facts.add("     ");
        facts.add("      ");
        facts.add("Based on Superlative Adjectives and adverbs -------------------------------------------------");
        facts.add("       ");
        BufferedReader reader = new BufferedReader(new FileReader(naiveFile + targetEntity + ".txt"));
        String naiveFacts;
        while((naiveFacts = reader.readLine()) != null)
        {
            facts.add(naiveFacts);
        }
        
        reader.close();
        
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
