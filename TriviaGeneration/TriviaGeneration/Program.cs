using System;
using System.IO;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using System.Diagnostics;
using System.Text.RegularExpressions;
using HtmlAgilityPack;

namespace TriviaGeneration
{
    class Program
    {
        //static System.IO.StreamWriter positiveDataFile;
        static void Main22(string[] args)
        {
            //string allfileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\Country\countryTrivia.txt";
            //positiveDataFile = new System.IO.StreamWriter(allfileName, true);
            
            List<string> urls = new List<string>();
            urls.Add(@"http://en.wikipedia.org/wiki/Lone_Survivor_%28film%29");
            urls.Add(@"http://en.wikipedia.org/wiki/Man_of_Steel_%28film%29");
            urls.Add(@"http://en.wikipedia.org/wiki/Final_Destination_%28film%29");
            urls.Add(@"http://en.wikipedia.org/wiki/The_Incredibles");
            urls.Add(@"http://en.wikipedia.org/wiki/Transformers:_Dark_of_the_Moon");
            urls.Add(@"http://en.wikipedia.org/wiki/The_Deer_Hunter");
            urls.Add(@"http://en.wikipedia.org/wiki/Who_Framed_Roger_Rabbit");
            urls.Add(@"http://en.wikipedia.org/wiki/Batman_Begins");
            urls.Add(@"http://en.wikipedia.org/wiki/Rio_2");
            urls.Add(@"http://en.wikipedia.org/wiki/Her_%28film%29");

            generateTextFile(urls);
            
            /*
            String fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\Country\punagr_country.txt";
            StreamReader r = new StreamReader(fileName);
            string line;
            while ((line = r.ReadLine()) != null)
            {
                line = line.Replace(' ', '_');
                String url = "http://en.wikipedia.org/wiki/" + line;
                String countryName = line;
                generateTextFile(url, countryName);
            }
            */
            //positiveDataFile.Flush();
            //positiveDataFile.Close();
            Console.ReadLine();
        }
        
        static void generateTextFile(List<string> urls)
        {
            foreach (string url in urls)
            {
                string fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\IMDb\movieTest\indivFiles\" + url.Remove(0, 29).Replace(":", "") +".txt";
                
                if (File.Exists(fileName))
                {
                    Console.WriteLine("Already done: " + url);
                    continue;
                }

                string text = getText(url);
                System.IO.StreamWriter textFile = new System.IO.StreamWriter(fileName);
                textFile.Write(text);
                textFile.Close();
                Console.WriteLine("done: " + url);
            }
        }

        static void generateTextFile(String url, String countryName)
        {
            string fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\Country\indiv_wiki\" + countryName + ".txt";

            if (File.Exists(fileName))
                return;

            string text = getText(url, countryName);
            System.IO.StreamWriter textFile = new System.IO.StreamWriter(fileName);
            textFile.Write(text);
            textFile.Close();
            Console.WriteLine("done: " + url);
        }

        static string getText(string url, string cn = "")
        {
            HtmlAgilityPack.HtmlWeb web = new HtmlWeb();
            string text="";

            try
            {
                HtmlAgilityPack.HtmlDocument doc = web.Load(url);

                foreach (HtmlNode row in doc.DocumentNode.SelectNodes("//p"))
                {
                    text += row.InnerText + "\n";
                    //Console.WriteLine("HERE: " + row.InnerText);
                    //positiveDataFile.WriteLine(cn + "\t" + row.InnerText);
                }

                string pattern = "citation needed";
                string replacement = "0";
                Regex rgx1 = new Regex(pattern);
                text = rgx1.Replace(text, replacement);

                pattern = "[[0-9]*]";
                replacement = "";
                Regex rgx = new Regex(pattern);
                text = rgx.Replace(text, replacement);
            }
            catch (Exception e)
            {
                Console.WriteLine("IGNORING: " + cn + "--------------------------");
            }
            return text;
        }
    }
}
