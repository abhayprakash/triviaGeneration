using HtmlAgilityPack;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TriviaGeneration
{
    class NameID_Scrapper
    {
        static string URL = @"http://www.imdb.com/search/name?gender=male,female&ref_=nv_cel_m_3&start=XX";
        /*
        static void Main()
        {
            foreach (string s in GetNameIDsForTop(1000))
            {
                Console.WriteLine(s);
            }
            Console.ReadLine();
        }
        */
        static HashSet<string> alreadyOccured = new HashSet<string>();
                
        public static List<String> GetNameIDsForTop(int n)
        {
            List<string> toret = new List<string>();

            string fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\IMDb\top1000Celebs\list.txt";

            if (File.Exists(fileName))
            {
                StreamReader r = new StreamReader(fileName);
	            string line;
	            while ((line = r.ReadLine()) != null)
	            {
		            toret.Add(line);
	            }
                Console.WriteLine("already present - sending list");
                return toret;
            }

            Console.WriteLine("Oh, I need to scrap the list first :/");
            System.IO.StreamWriter textFile = new System.IO.StreamWriter(fileName);
            
            for (int Number = 1; Number < n; Number += 50)
            {
                URL = URL.Replace("XX", Number.ToString());
                HtmlAgilityPack.HtmlWeb web = new HtmlWeb();
                HtmlAgilityPack.HtmlDocument doc = web.Load(URL);

                foreach (var s in doc.DocumentNode.SelectNodes("//tr/td/a/@href"))
                {
                    string cand = s.GetAttributeValue("href", "NOT");
                    if (cand.StartsWith("/name/nm"))
                    {
                        if (!alreadyOccured.Contains(cand))
                            toret.Add(cand);
                        alreadyOccured.Add(cand);
                        textFile.Write(cand + "\n");
                    }
                }
            }
            textFile.Close();
            return toret;
        }
    }
}
