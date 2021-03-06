package com.newsifier.dao.impl;

import com.cloudant.client.org.lightcouch.TooManyRequestsException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.newsifier.dao.interfaces.NewsDAO;
import com.newsifier.rss.bean.Feed;
import com.newsifier.rss.bean.News;
import com.newsifier.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.newsifier.dao.impl.CloudantDAOUtils.*;

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

        try {
            getDbMaster().save(cloudantNews);
            Logger.log("Created cloudant document for feed : " + f.getName());
            Logger.webLog("Created cloudant document for feed : " + f.getName());

        } catch (com.cloudant.client.org.lightcouch.DocumentConflictException e) {

            //Reading the existing document
            JsonObject read = jsonObjectreaderFromCloudantId(f.getName(), getDbMaster());
            FeedDB newsFromCloudant = new Gson().fromJson(read, FeedDB.class);

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

            Logger.log("Added news for feed : " + f.getName());
            Logger.webLog("Added news for feed : " + f.getName());

        }
    }

    @Override
    public List<News> getNews(Feed f) {

        try {
            JsonObject read = jsonObjectreaderFromCloudantId(f.getName(), getDbMaster());
            FeedDB newsFromCloudant = new Gson().fromJson(read, FeedDB.class);
            return newsFromCloudant.getNewslist();
        }
        catch (TooManyRequestsException e){
            Logger.webLog("Error: too_many_requests. Reason: You've exceeded your current limit of 20 requests per second for lookup class\n");
            Logger.logErr("Error: too_many_requests. Reason: You've exceeded your current limit of 20 requests per second for lookup class\n");
            return new ArrayList<>();
        }
    }

    public CloudantNewsDAO() {
        createConnectionWithCloudant();
    }
}
