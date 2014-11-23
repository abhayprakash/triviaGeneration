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
import java.net.HttpURLConnection;
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
    
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, InterruptedException {
        ArrayList<String> ListOfTypeOfEntities = new ArrayList<>();
        //ListOfTypeOfEntities.add("BollywoodActresses");
        //ListOfTypeOfEntities.add("Actor");
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
            ArrayList<Thread> threads = new ArrayList<>();
            while ((entityURL = br.readLine()) != null) {
                String entityID = entityURL.replace("http://dbpedia.org/resource/", "");
                
                Thread t = new Thread(new Runnable() {
                    public void run()
                    {
                        try{
                            GetRowForEntityURL(entityID);
                        }catch(Exception e)
                        {
                            System.out.println("Ignored: " + entityID + " Error: " + e.getMessage());
                        }
                    }
                });
                t.start();//.run();
                threads.add(t);
            }
            for(int i = 0; i < threads.size(); i++)
                threads.get(i).join();
            System.out.println("***********Threads Joined******************");
            PrintTable(type);
            System.out.println("DONE: " + type + "========================================");
        }
    }
    /*
    static void DownloadFile(String entityID) throws MalformedURLException, IOException
    {
        File downloadAt = new File(inputFolder + entityID + ".txt");
        if(downloadAt.exists())
            return;
        URL url = new URL("http://dbpedia.org/data/" + entityID + ".rdf");
        FileUtils.copyURLToFile(url, downloadAt);
    }
    */
    
    public static void downloadFile(String entityID) throws IOException {
        String fileURL = "http://dbpedia.org/data/" + entityID + ".rdf";
        String saveDir = inputFolder;
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
 
        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }
 
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;
             
            // opens an output stream to save into file
            saveFilePath.replace(".rdf", ".txt");
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
 
            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            outputStream.close();
            inputStream.close();
 
            //System.out.println("File downloaded");
        } else {
            System.out.println("Download Failed. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }
    
    static String GetValueFromLink(String link)
    {
        return link.replace("http://dbpedia.org/resource/", "");
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
                                else
                                {
                                    //rdf:resource
                                    String linkValue = eChild.getAttribute("rdf:resource");
                                    String value = GetValueFromLink(linkValue);
                                    if(!value.isEmpty())
                                    {
                                        if(!allColumns.contains(columnName))
                                        allColumns.add(columnName);                                    
                                        Table.get(entityID).putIfAbsent(columnName, new ArrayList<>());
                                        Table.get(entityID).get(columnName).add(value);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
