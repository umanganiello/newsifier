package com.newsifier.dao.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.newsifier.dao.interfaces.NewsDAO;
import com.newsifier.rss.bean.Feed;
import com.newsifier.rss.bean.News;

import java.util.List;

import static com.newsifier.dao.impl.CloudantUtilsDAO.*;

public class CloudantNewsDAO implements NewsDAO {

    @Override
    public void insertNews(List<News> newsList, Feed f) {
        JsonObject cloudantNews = new JsonObject();
        cloudantNews.addProperty("_id", f.getName());
        JsonArray newsArray = new JsonArray();

        for (News news : newsList) {
            JsonObject newsMap = new JsonObject();
            newsMap.addProperty("title", news.getTitle());
            newsMap.addProperty("uri", String.valueOf(news.getUri()));
            newsArray.add(newsMap);
        }
        cloudantNews.add("news", newsArray);
        createConnectionWithDBMaster();

        try {
            getDbMaster().save(cloudantNews);
            System.out.println("Created document news for the feed " + f.getName() + " saved");

        } catch (com.cloudant.client.org.lightcouch.DocumentConflictException e) {

            //Reading the existing document
            JsonObject read = jsonObjectreaderFromCloudantId(f.getName(), getDbMaster());
            NewsDB newsFromCloudant = new Gson().fromJson(read, NewsDB.class);

            //Added the new feed to existing list
            for (News news : newsList) {
                newsFromCloudant.addNews(news);
            }

            //Remove old document
            getDbMaster().remove(read);

            //Remove revision id for the new creation
            newsFromCloudant.set_rev(null);

            //Save the updated document
            getDbMaster().save(newsFromCloudant);

            System.out.println("Added new news for the feed " + f.getName() + " saved");
        }
    }

    @Override
    public List<News> getNews(Feed f) {
        createConnectionWithDBMaster();

        JsonObject read = jsonObjectreaderFromCloudantId(f.getName(), getDbMaster());
        NewsDB newsFromCloudant = new Gson().fromJson(read, NewsDB.class);
        return newsFromCloudant.getNewslist();
    }
}
