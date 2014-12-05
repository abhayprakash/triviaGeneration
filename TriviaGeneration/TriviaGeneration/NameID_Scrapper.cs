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
        //static string preURL = @"http://www.imdb.com/search/name?gender=male,female&ref_=nv_cel_m_3&start=";
        //static string preURL = @"http://www.imdb.com/search/title?count=100&start=";
        static string preURL = @"http://www.imdb.com/search/title?count=100&ref_=nv_ch_mm_1&start=NUMBER&title_type=feature,tv_series,tv_movie";
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

            string fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\IMDb\selected1000Movies\list.txt";

            if (File.Exists(fileName))
            {
                StreamReader r = new StreamReader(fileName);
	            string line;
	            while ((line = r.ReadLine()) != null)
	            {
                    String[] s = line.Split(new char[]{'\t'});
		            toret.Add(s[0]);
	            }
                Console.WriteLine("already present - sending list");
                return toret;
            }

            Console.WriteLine("Oh, I need to scrap the list first :/");
            System.IO.StreamWriter textFile = new System.IO.StreamWriter(fileName);
            
            for (int Number = 1; Number < n; Number = Number + 100)
            {
                string URL = preURL.Replace("NUMBER", Number.ToString());
                Console.WriteLine("scraping now : " + URL);
                HtmlAgilityPack.HtmlWeb web = new HtmlWeb();
                HtmlAgilityPack.HtmlDocument doc = web.Load(URL);

                foreach (var s in doc.DocumentNode.SelectNodes("//tr/td/a/@href"))
                {
                    string cand = s.GetAttributeValue("href", "NOT");
                    string title = s.GetAttributeValue("title", "NOT");
                    if (cand.StartsWith("/title/tt"))
                    {
                        //Console.WriteLine("here " + cand);
                        //cand = cand.Remove(17);
                        if (!alreadyOccured.Contains(cand))
                        {
                            toret.Add(cand);
                            alreadyOccured.Add(cand);
                            textFile.Write(cand + "\t" + title + "\n");
                        }
                    }
                }
            }
            textFile.Close();
            Console.WriteLine("Returning " + toret.Count + "in list");
            return toret;
        }
    }
}
