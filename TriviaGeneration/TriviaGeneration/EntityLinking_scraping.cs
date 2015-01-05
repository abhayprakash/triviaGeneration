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
        static void Main(string[] args)
        {
            String fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\IMDb\anotherSelected5k\movie_URL.txt";
            String writeFile = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\IMDb\anotherSelected5k\entityLinks.txt";
            StreamReader r = new StreamReader(fileName);
            StreamWriter writer = new StreamWriter(writeFile);
            string URL; int nn = 1;
            while ((URL = r.ReadLine()) != null)
            {
                Console.WriteLine(nn++);
                URL = @"http://www.imdb.com/title/tt1877832/fullcredits";
                HtmlAgilityPack.HtmlWeb web = new HtmlWeb();
                HtmlAgilityPack.HtmlDocument doc = web.Load(URL);
                String movieID = URL.Replace("fullcredits", "");
                foreach (HtmlNode row in doc.DocumentNode.SelectNodes("//h4[@class=\"dataHeaderWithBorder\"]"))
                {
                    String candidate = row.InnerText.Replace("&nbsp;", "").Trim();
                    if (candidate.Contains("Cast"))
                    {
                        String entityType = "entity_Cast";
                        foreach (HtmlNode child in doc.DocumentNode.SelectNodes(row.NextSibling.NextSibling.XPath + "//td[@class=\"itemprop\"]"))
                        {
                            writer.WriteLine(movieID + "\t" + entityType + "\t" + child.InnerText.Trim());
                            HtmlNode sibling = child.NextSibling.NextSibling.NextSibling.NextSibling;
                            writer.WriteLine(movieID + "\t" + "entity_Character" + "\t" + sibling.InnerText.Trim());
                        }
                        //Console.ReadLine();
                    }
                    else
                    {
                        String entityType = "entity_" + candidate.Replace(' ', '_');
                        foreach (HtmlNode child in doc.DocumentNode.SelectNodes(row.NextSibling.NextSibling.XPath + "//td[@class=\"name\"]"))
                        {
                            writer.WriteLine(movieID + "\t" + entityType + "\t" + child.InnerText.Trim());
                        }
                    }
                }
            }
            writer.Flush();
            writer.Close();
        }
    }
}
