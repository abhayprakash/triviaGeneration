/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tablefromdbpedia;

import java.io.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.*;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;


/**
 *
 * @author Abhay Prakash
 */
public class TableFromDBPedia {
    static HashMap<String, HashMap<String, ArrayList<String> > > Table = new HashMap<String, HashMap<String, ArrayList<String>>>();
    static ArrayList<String> allColumns = new ArrayList<String>();
    
    static String inputFolder = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data_DBPedia\\Input\\";
    static String resultTableFolder = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data_DBPedia\\Output\\";
    
    static void PrintTable(String FileName) throws IOException
    {
        String resultTableFile = resultTableFolder + FileName + ".txt";
        File file = new File(resultTableFile);
 
        // if file doesnt exists, then create it
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        
        String row = "Entity";
        for(int i = 0; i < allColumns.size(); i++)
        {
            row += "\t" + allColumns.get(i);
        }
        bw.write(row + "\n");
        for(String entityName: Table.keySet())
        {
            row = entityName;
            for(String columnName: allColumns)
            {
                if(Table.get(entityName).containsKey(columnName) == false)
                    row += "\tNA";
                else
                {
                    if(Table.get(entityName).get(columnName).size() == 1)
                    {
                        row += "\t" + Table.get(entityName).get(columnName).get(0);
                    }
                    else
                    {
                        String value = "{" + Table.get(entityName).get(columnName).get(0);
                        for(int i = 1; i < Table.get(entityName).get(columnName).size(); i++)
                        {
                            value += "|" + Table.get(entityName).get(columnName).get(i);
                        }
                        value += "}";
                        row += "\t" + value;
                    }
                }
            }
            bw.write(row + "\n");
        }
        bw.close();
    }
    
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        ArrayList<String> ListOfTypeOfEntities = new ArrayList<>();
        ListOfTypeOfEntities.add("BollywoodActresses");
        ListOfTypeOfEntities.add("Actor");
        ListOfTypeOfEntities.add("Cricketers");
        ListOfTypeOfEntities.add("Film");
        ListOfTypeOfEntities.add("FootballPlayers");
        ListOfTypeOfEntities.add("ResearchProjects");
        ListOfTypeOfEntities.add("Scientists");
        
        for(String type: ListOfTypeOfEntities)
        {
            Table.clear();
            allColumns.clear();
            String filePath_ListOfEntities = inputFolder.replace("Input\\", "") + type + ".txt";
        
            BufferedReader br = new BufferedReader(new FileReader(filePath_ListOfEntities));
            String entityURL;
            while ((entityURL = br.readLine()) != null) {
                String entityID = entityURL.replace("http://dbpedia.org/resource/", "");
                try{
                    GetRowForEntityURL(entityID);
                }catch(Exception e)
                {
                    System.out.println("Ignored: " + entityID + " Error: " + e.getMessage());
                }
            }
            PrintTable(type);
            System.out.println("DONE: " + type + "========================================");
        }
    }
    
    static void DownloadFile(String entityID) throws MalformedURLException, IOException
    {
        File downloadAt = new File(inputFolder + entityID + ".txt");
        if(downloadAt.exists())
            return;
        URL url = new URL("http://dbpedia.org/data/" + entityID + ".rdf");
        FileUtils.copyURLToFile(url, downloadAt);
    }
    
    static void GetRowForEntityURL(String entityID) throws IOException, ParserConfigurationException, SAXException
    {
        System.out.println(entityID);
        Table.putIfAbsent(entityID, new HashMap<String, ArrayList<String> >());
        DownloadFile(entityID);
        String xml_FilePath = inputFolder + entityID + ".txt";
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        Document document = builder.parse(new File(xml_FilePath));
        document.getDocumentElement().normalize();
        //Element root = document.getDocumentElement();
        
        NodeList nodeList = document.getElementsByTagName("rdf:Description");
        String entityPage = "http://dbpedia.org/resource/" + entityID;
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE){
                Element eElement = (Element) node;
                if(eElement.getAttribute("rdf:about").equals(entityPage))
                {
                    NodeList childList = eElement.getChildNodes();
                    for(int j = 0; j < childList.getLength(); j++)
                    {
                        Node child = childList.item(j);
                        if (child.getNodeType() == Node.ELEMENT_NODE)
                        {
                            Element eChild = (Element) child;
                            String childName = eChild.getNodeName();
                            if(childName.startsWith("dbpprop:"))
                            {
                                String columnName = childName.replace("dbpprop:", "");
                                if(!eChild.getTextContent().isEmpty())
                                {
                                    //System.out.println(columnName + " :: " + eChild.getTextContent());
                                    if(!allColumns.contains(columnName))
                                        allColumns.add(columnName);                                    
                                    Table.get(entityID).putIfAbsent(columnName, new ArrayList<>());
                                    Table.get(entityID).get(columnName).add(eChild.getTextContent());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
