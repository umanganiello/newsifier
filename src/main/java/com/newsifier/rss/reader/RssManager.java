package com.newsifier.rss.reader;

import com.newsifier.rss.bean.Feed;
import com.newsifier.rss.bean.News;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class extracts the feed/news structure
 */
 public class RssManager {

     public static void printNews(Feed f){
         ArrayList<News> newsFromFeed = (ArrayList<News>) readerNews(f, 100000000);
         for (News news : newsFromFeed) {
             System.out.println(" Title: " + news.getTitle() + " url: "+news.getUri());
         }

     }


    public static List<News> readerNews(Feed f, int limit) {

        int i = 1;
        ArrayList<News> newsArrayList = new ArrayList<>();
        try {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(f.getUrl()));

            ArrayList<SyndEntry> s = (ArrayList<SyndEntry>) feed.getEntries();
            for (SyndEntry syndEntry : s) {
                if (i>limit)
                {
                    break;
                }
                else {
                    newsArrayList.add(new News(syndEntry.getTitle(), syndEntry.getUri()));
                    i++;
                }
            }

        } catch (FeedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newsArrayList;
    }
}
