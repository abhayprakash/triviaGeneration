/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package featureExtractor;

import static featureExtractor.EntityLinker.In_entityDictionary;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 *
 * @author Abhay Prakash
 */
public class popularityMeasure {
    static String In_entities = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\top1000Celebs\\celebList.txt";
    static String Out_resultFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\top1000Celebs\\celebPopularity.txt";
            
    static String key = "AIzaSyAVVKmcTUk2lZc7sFJetpkWuTCxYAauHc0";           
    
    static HashMap<String, Double> knownScore_table = new HashMap<String, Double>();
    
    public static void main22(String[] args) throws IOException, ParseException
    {
        String json = searchTest( "15 August", "");//&scoring=entity");
        System.out.println(json);
        System.out.println(ParseJSON_getScore(json));
    }
    
    public static void main(String[] args) throws IOException{
        //ReadKnownPopularityScores();
        FileWriter fw = new FileWriter(Out_resultFile);
        BufferedWriter bw = new BufferedWriter(fw);
        
        FileReader inputFile = new FileReader(In_entities);
        BufferedReader bufferReader = new BufferedReader(inputFile);
        String line;
        while((line = bufferReader.readLine()) != null)
        {
            String[] row = line.split("\t");
            double score = 0;
            String entityName = row[0].toLowerCase().trim();
            System.out.println("Searching for : " + entityName);
            if(knownScore_table.containsKey(entityName))
            {
                //System.out.println("Already known for: " + entityName);
                score = knownScore_table.get(entityName);
            }
            else{
                System.out.println("Not known for: " + entityName);
                String json = searchTest( entityName, "&scoring=entity");
                try{
                    score = ParseJSON_getScore(json);
                }catch(Exception e){
                    score = 0;
                }
                System.out.println("Putting : " + entityName);
                knownScore_table.put(entityName, score);
            }
            bw.write(row[0] + "\t" + score + "\n");
            System.out.println(row[0]);
        }
        bw.close();
    }
    
    static void ReadKnownPopularityScores() throws FileNotFoundException, IOException
    {
        String known = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\anotherSelected5k\\knownPopularity.txt";
        FileReader inputFile = new FileReader(known);
        BufferedReader bufferReader = new BufferedReader(inputFile);
        String line;                                                                                                                                          
        while((line = bufferReader.readLine()) != null)
        {
            String[] row = line.split("\t");
            knownScore_table.put(row[0].toLowerCase().trim(), Double.parseDouble(row[1]));
        }
        System.out.println("Read: " + knownScore_table.size());
    }
    
    static double ParseJSON_getScore(String s) throws ParseException
    {
        JSONObject json = (JSONObject)new JSONParser().parse(s);
        JSONArray results = (JSONArray) json.get("result");
        JSONObject resultObject = (JSONObject) results.get(0);
        return (double) resultObject.get("score");
    }
    
    static String searchTest(String query, String params) throws IOException
    {        
           String service_url = "https://www.googleapis.com/freebase/v1/search";      

           String url = service_url    + "?query=" + URLEncoder.encode(query, "UTF-8")
                                        + params 
                                        + "&key=" + key;     

           HttpClient httpclient = new DefaultHttpClient();   
           HttpResponse response = httpclient.execute(new HttpGet(url));  
           return EntityUtils.toString(response.getEntity());
    }
}
