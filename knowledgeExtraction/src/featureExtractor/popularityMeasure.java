/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package featureExtractor;

import static featureExtractor.EntityLinker.In_entityDictionary;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
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
    static String In_entities = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\anotherSelected5k\\ForScoringPopularity.txt";
    static String Out_resultFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\anotherSelected5k\\freebasePopularity.txt";
            
    static String key = "AIzaSyAVVKmcTUk2lZc7sFJetpkWuTCxYAauHc0";           
    
    public static void main(String[] args) throws IOException, ParseException
    {
        String json = searchTest( "Aamir Khan", "&scoring=entity");
        System.out.println(ParseJSON_getScore(json));
    }
    
    public static void main122(String[] args) throws IOException{
        FileWriter fw = new FileWriter(Out_resultFile);
        BufferedWriter bw = new BufferedWriter(fw);
        
        FileReader inputFile = new FileReader(In_entities);
        BufferedReader bufferReader = new BufferedReader(inputFile);
        String line;
        line = bufferReader.readLine();
        while((line = bufferReader.readLine()) != null)
        {
            String[] row = line.split("\t");
            String json = searchTest( row[1], "&scoring=entity");
            double score = 0;
            try{
                score = ParseJSON_getScore(json);
            }catch(Exception e){
                score = 0;
            }
            bw.write(row[0] + "\t" + row[1] + "\t" + score + "\n");
            System.out.println(row[0]);
        }
        bw.close();
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
