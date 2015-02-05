using HtmlAgilityPack;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TriviaGeneration
{
    class EntityLinking_scraping
    {
        static StreamWriter writer;
        static void Main(string[] args)
        {
            String presentFile = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\IMDb\anotherSelected5k\MORE_DATA\temp\presentLinks.txt";
            StreamReader pr = new StreamReader(presentFile);
            String pLink;
            HashSet<String> present = new HashSet<string>();
            
            while ((pLink = pr.ReadLine()) != null)
            {
                present.Add(pLink.Trim());
            }

            Console.WriteLine("Present : " + present.Count);

            String fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\IMDb\anotherSelected5k\MORE_DATA\temp\movieURLs.txt";
            String writeFile = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\IMDb\anotherSelected5k\MORE_DATA\temp\entityLinks_new.txt";
            StreamReader r = new StreamReader(fileName);
            writer = new StreamWriter(writeFile);
            string URL; int nn = 1;
            while ((URL = r.ReadLine()) != null)
            {
                URL = URL.Trim();
                Console.Write(nn++);
                try
                {
                    if (!present.Contains(URL))
                    {
                        ProcessMovie(URL);
                        Console.WriteLine(" Done: " + URL);
                    }
                    else
                    {
                        Console.WriteLine("Already Present");
                    }
                }
                catch (Exception e)
                {
                    Console.WriteLine(" IGNORING: " + URL);
                }
            }
            writer.Flush();
            writer.Close();
            Console.WriteLine("Done");
            Console.ReadLine();
        }
        static void ProcessMovie(String URL)
        {
            URL = URL + "fullcredits";
            Console.WriteLine("Trying URL: " + URL);
            HtmlAgilityPack.HtmlWeb web = new HtmlWeb();
            HtmlAgilityPack.HtmlDocument doc = web.Load(URL);
            String movieID = URL.Replace("fullcredits", "");
            int count = 0;
            foreach (HtmlNode row in doc.DocumentNode.SelectNodes("//h4[@class=\"dataHeaderWithBorder\"]"))
            {
                if (count == 5)
                    break;

                count++;
                String candidate = row.InnerText.Replace("&nbsp;", "").Trim();
                //Console.WriteLine("CAND: " + candidate);
                if (candidate.Contains("Cast") && !candidate.Contains("By") && !candidate.Contains("by") && !candidate.Contains("ing"))
                {
                    String entityType = "entity_Cast";
                    foreach (HtmlNode child in doc.DocumentNode.SelectNodes(row.NextSibling.NextSibling.XPath + "//td[@class=\"itemprop\"]"))
                    {
                        writer.WriteLine(movieID + "\t" + entityType + "\t" + child.InnerText.Trim());
                        HtmlNode sibling = child.NextSibling.NextSibling.NextSibling.NextSibling;
                        String[] characters = sibling.InnerText.Trim().Split('/');
                        foreach (String cc in characters)
                        {
                            //Console.WriteLine(movieID + "\t" + "entity_Character" + "\t" + cc.Trim());
                            writer.WriteLine(movieID + "\t" + "entity_Character" + "\t" + cc.Trim());
                            //Console.ReadLine();
                        }
                    }
                    //Console.ReadLine();
                }
                else if (candidate.Equals("Directed by") || candidate.Equals("Writing Credits") || candidate.Equals("Produced by") || candidate.Equals("Cinematography by"))
                {
                    String entityType = "entity_" + candidate.Replace(' ', '_');
                    foreach (HtmlNode child in doc.DocumentNode.SelectNodes(row.NextSibling.NextSibling.XPath + "//td[@class=\"name\"]"))
                    {
                        writer.WriteLine(movieID + "\t" + entityType + "\t" + child.InnerText.Trim());
                    }
                }
            }
        }
    }
}
