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
            urls.Add(@"http://en.wikipedia.org/wiki/Jennifer_Aniston");
            urls.Add(@"http://en.wikipedia.org/wiki/Jessica_Alba");
            urls.Add(@"http://en.wikipedia.org/wiki/Emma_Stone");
            urls.Add(@"http://en.wikipedia.org/wiki/Cameron_Diaz");
            urls.Add(@"http://en.wikipedia.org/wiki/Scarlett_Johansson");
            urls.Add(@"http://en.wikipedia.org/wiki/Taylor_Swift");
            urls.Add(@"http://en.wikipedia.org/wiki/Julia_Roberts");
            urls.Add(@"http://en.wikipedia.org/wiki/Emma_Watson");
            urls.Add(@"http://en.wikipedia.org/wiki/Angelina_Jolie");
            urls.Add(@"http://en.wikipedia.org/wiki/Owen_Wilson");
            

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
                string fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\wikiText\HollywoodActors\input\" + url.Remove(0, 29).Replace(":", "") + ".txt";
                
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
