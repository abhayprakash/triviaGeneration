/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tablefromdbpedia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Abhay Prakash
 */
public class nameToId_Scrapper {
    private static String url;
    public static void main(String[] args) throws FileNotFoundException, IOException{
        String folderPath = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\top1000Celebs\\Celeb_New\\";
        String in_fileName = folderPath + "celebList.txt";
        String out_fileName = folderPath + "celebURLs.txt";
        
        FileWriter fw = new FileWriter(out_fileName);
        BufferedWriter bw = new BufferedWriter(fw);
        
        FileReader inputFile = new FileReader(in_fileName);
        BufferedReader bufferReader = new BufferedReader(inputFile);
        String celebName;
        //input = bufferReader.readLine();
        while((celebName = bufferReader.readLine()) != null)
        {
            System.out.println(celebName);
            try{
            WriteResourceURL(celebName, bw);
            }catch(Exception e)
            {
                System.out.println("some r e: " + e.getMessage());
            }
            //System.in.read();
        }
        bw.close();
    }
    static void WriteResourceURL(String celeb, BufferedWriter bw) throws MalformedURLException, IOException, ParserConfigurationException, SAXException, TransformerConfigurationException, TransformerException
    {
        String pre_apiURL = "http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryClass=person&MaxHits=1&QueryString=";        
        String apiURL = pre_apiURL + celeb + "";
        apiURL = apiURL.replaceAll(" ","%20");
        //System.out.println("url "+apiURL);
        
        URL url = new URL(apiURL);
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(url.openStream());
        
        String uri = "";
        try{
        NodeList nl = document.getElementsByTagName("URI");
        uri = nl.item(0).getTextContent();
        }catch(Exception e)
        {
            System.out.println("Ignored for " + celeb);
        }
        bw.write(uri + "\n");
        //System.out.println("child content " + nl.item(0).getTextContent());
        bw.flush();
    }
}
