/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package trivia_ranker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Abhay Prakash
 */
public class Prototype_Algo {    
    static String trainCorpusPath = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\top1000Movies\\cleanCorpus_train\\";
    static String testCorpusPath = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\top1000Movies\\cleanCorpus_test\\";
    static String savedPrototype = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\top1000Movies\\prototype.obj";
    static String wordFreqFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\top1000Movies\\wordFreq.txt";
    static String weightFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\top1000Movies\\weights.txt";
    static String originalTestFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\top1000Movies\\test_hunger.txt";
    static String rankedResultFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\top1000Movies\\ranked_hunger.txt";
    
    static HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
    static HashMap<String, String> repToOrig = new HashMap<String, String>();
    
    static Boolean weighted = false;
    
    public static void main(String[] args) throws IOException, ClassNotFoundException
    {
        File file = new File(wordFreqFile);//savedPrototype);
        // if file doesnt exists, then create it
        if (!file.exists()) {
            System.out.println("We need to make prototype vector");
            getPrototypeVector();
            //savePrototypeVector();
            printSortedByFreq();
        }
        else
        {
            System.out.println("already present prototype vector - reading that");
            //readPrototypeVector();
            readPrototypeVectorFromTxt();
        }
        
        RankTrivia();
        //printSortedByFreq();
        //test();
    }
    
    static void readPrototypeVectorFromTxt() throws FileNotFoundException, IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(wordFreqFile));
        String line;
        while((line = br.readLine() )!= null)
        {
            String keyValue[] = line.split("\t");
            wordCount.put(keyValue[0], Integer.parseInt(keyValue[1]));
        }
        br.close();
    }
    
    static void RankTrivia() throws IOException
    {
        GetCleanedMapping();
        HashMap<String, Double> simScore = new HashMap<>();
        for (Map.Entry<String, String> entry : repToOrig.entrySet())
        {
            String candidate = entry.getKey();
            double similarity = GetSimilarity(candidate);
            simScore.put(candidate, similarity);
        }
        
        Set<Entry<String, Double>> set = simScore.entrySet();
        List<Entry<String, Double>> list = new ArrayList<Entry<String, Double>>(set);
        Collections.sort( list, new Comparator<Map.Entry<String, Double>>()
        {
            public int compare( Map.Entry<String, Double> o1, Map.Entry<String, Double> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );
        
        FileWriter fw = new FileWriter(rankedResultFile);
        BufferedWriter bw = new BufferedWriter(fw);
        
        for(Map.Entry<String, Double> entry:list){
            bw.write(repToOrig.get(entry.getKey())+"\t"+entry.getValue()+"\n");
        }
        
        bw.close();
    }
    
    static double GetSimilarity(String s)
    {
        String words[] = s.split("\\s+");
        
        HashMap<String, Integer> candWordCount = new HashMap<>();
        
        for(String w : words)
        {
            if(!candWordCount.containsKey(w))
                candWordCount.put(w, 0);
            
            candWordCount.put(w, candWordCount.get(w)+1);
        }
        
        double rms = 0;
        double num = 0;
        for (Map.Entry<String, Integer> entry : candWordCount.entrySet())
        {
            int Bi = entry.getValue();
            int Ai = 0;
            if(wordCount.containsKey(entry.getKey()))
                Ai = wordCount.get(entry.getKey());
            
            num += Ai * Bi;
            
            rms += Bi*Bi;
        }
        rms = Math.sqrt(rms);
        double toRet = num / rms;
        return toRet;
    }
    
    static void GetCleanedMapping() throws FileNotFoundException, IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(originalTestFile));
        File dir = new File(testCorpusPath);
        File[] directoryListing = dir.listFiles();
        int i = 0;
        for (File child : directoryListing) {
            i++;
            String fileName = i+".txt";//child.getName();
            //System.out.println(fileName + " :: ");
            String fileContent = readFile(testCorpusPath+fileName, StandardCharsets.UTF_8);
            fileContent = fileContent.replaceAll("\n", "");
            fileContent = fileContent.trim();
            
            String original = br.readLine();
            repToOrig.put(fileContent, original);
        }
    }
    
    static void printSortedByFreq() throws IOException
    {
        Set<Entry<String, Integer>> set = wordCount.entrySet();
        List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);
        Collections.sort( list, new Comparator<Map.Entry<String, Integer>>()
        {
            public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );
        
        FileWriter fw = new FileWriter(wordFreqFile);
        BufferedWriter bw = new BufferedWriter(fw);
        
        for(Map.Entry<String, Integer> entry:list){
            bw.write(entry.getKey()+"\t"+entry.getValue()+"\n");
        }
        
        bw.close();
    }
    
    static void test()
    {
        System.out.println(wordCount.size());
        System.out.println(wordCount.get("scene"));
    }
    
    static void readPrototypeVector() throws IOException, ClassNotFoundException
    {
        FileInputStream fis = new FileInputStream(savedPrototype);
        ObjectInputStream ois = new ObjectInputStream(fis);
        wordCount = (HashMap) ois.readObject();
        ois.close();
        fis.close();
    }
    
    static void savePrototypeVector() throws FileNotFoundException, IOException
    {
        FileOutputStream fos = new FileOutputStream(savedPrototype);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(wordCount);
        oos.close();
        fos.close();
        System.out.println("Serialized HashMap data is saved");
    }
    
    static void getPrototypeVector() throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(weightFile));
        File dir = new File(trainCorpusPath);
        File[] directoryListing = dir.listFiles();
        String weight;
        int i =0;
        for (File child : directoryListing) {
            i++;
            weight = br.readLine();
            weight = weight.replace(",", "");
            weight = weight.replace("\"", "");
            String inputFile = i+".txt";//child.getName();
            ReadAndUpdateMap(inputFile, Integer.parseInt(weight));
        }
        br.close();
    }
    
    static String readFile(String path, Charset encoding) throws IOException 
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
    
    static void ReadAndUpdateMap(String fileName, int w) throws IOException
    {
        String fileContent = readFile(trainCorpusPath+fileName, StandardCharsets.UTF_8);
        fileContent = fileContent.replaceAll("\n", "");
        fileContent = fileContent.trim();
        ProcessString(fileContent, w);
        //System.out.println(fileContent);
    }
    
    static void ProcessString(String s, int wt)
    {
        //System.out.println("String: " + s);
        String words[] = s.split("\\s+");
        for(String w : words)
        {
            if(!wordCount.containsKey(w))
                wordCount.put(w, 0);
            
            if(weighted.equals(false))
                wt = 1;
            
            wordCount.put(w, wordCount.get(w) + wt);
        }
    }
}
