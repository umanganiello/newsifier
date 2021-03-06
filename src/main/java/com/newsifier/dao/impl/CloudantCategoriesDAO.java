package com.newsifier.dao.impl;

import com.cloudant.client.org.lightcouch.TooManyRequestsException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.newsifier.dao.interfaces.CategoriesDAO;
import com.newsifier.utils.Logger;
import com.newsifier.watson.bean.NewsNLU;
import com.newsifier.watson.bean.NewsWithKeywords;

import java.util.ArrayList;
import java.util.List;

import static com.newsifier.dao.impl.CloudantDAOUtils.*;

public class CloudantCategoriesDAO implements CategoriesDAO {

    @Override
    public void insertCategories(List<NewsNLU> news) {
        for (NewsNLU newsNLU : news) {
            insertCategories(newsNLU);
        }
    }

    @Override
    public void insertCategories(NewsNLU newsNLU) {

        for (String cat : newsNLU.getCategories()) {

            createConnectionWithCloudant();

            StringBuilder keywordsConcat = new StringBuilder();
            for (String s : newsNLU.getKeywords()) {
                String mod = s.replaceAll("\\s+", "_");
                keywordsConcat.append(mod).append(" ");
            }
            keywordsConcat = keywordsConcat.deleteCharAt(keywordsConcat.length() - 1);

            NewsWithKeywords newsNLUByCat = new NewsWithKeywords(newsNLU.getUrlNews(), keywordsConcat.toString());

            JsonObject cloudantCats = new JsonObject();
            cloudantCats.addProperty("_id", cat);
            JsonArray newsforCatArray = new JsonArray();

            JsonObject newsforCatMap = new JsonObject();
            newsforCatMap.addProperty("uri", newsNLUByCat.getUri());
            newsforCatMap.addProperty("keywords", newsNLUByCat.getKeywords());
            newsforCatArray.add(newsforCatMap);

            cloudantCats.add("news", newsforCatArray);

            boolean done = false;

            while (!done) {
                try {

                    getDbCategories().save(cloudantCats);

                    Logger.log("Created cloudant document for category : " + cat);
                    Logger.webLog("Created cloudant document for category : " + cat);
                    done = true;

                } catch (com.cloudant.client.org.lightcouch.DocumentConflictException e) {

                    //Read the existing document
                    JsonObject read = jsonObjectreaderFromCloudantId(cat, getDbCategories());
                    CategoryDB newsFromCloudant = new Gson().fromJson(read, CategoryDB.class);

                    //Add the new news to existing list
                    newsFromCloudant.addNews(newsNLUByCat);

                    //Remove old document
                    getDbCategories().remove(read);

                    //Remove revision id for the new creation
                    newsFromCloudant.set_rev(null);

                    while (true) {
                        try {
                            //Save the updated document
                            getDbCategories().save(newsFromCloudant);
                            Logger.log("Added keywords for category : " + cat);
                            Logger.webLog("Added keywords for category : " + cat);
                            break;

                        } catch (com.cloudant.client.org.lightcouch.CouchDbException e1) {
                            Logger.logErr(e1.getMessage());

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e2) {
                                e2.printStackTrace();
                            }
                        }
                    }
                    done = true;

                } catch (com.cloudant.client.org.lightcouch.CouchDbException e1) {
                    Logger.logErr(e1.getMessage());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }


    @Override
    public List<NewsWithKeywords> getNewsbyCat(String cat) {

        try {
            JsonObject read = jsonObjectreaderFromCloudantId(cat, getDbCategories());
            CategoryDB newsFromCloudant = new Gson().fromJson(read, CategoryDB.class);
            return newsFromCloudant.getNewslist();
        }
        catch (TooManyRequestsException e){
            Logger.webLog("Error: too_many_requests. Reason: You've exceeded your current limit of 20 requests per second for lookup class\n");
            Logger.logErr("Error: too_many_requests. Reason: You've exceeded your current limit of 20 requests per second for lookup class\n");
            return new ArrayList<>();
        }
    }

    @Override
    public String newsToCSV(String cat) {
        StringBuilder csvFile = new StringBuilder();
        List<NewsWithKeywords> newsNLUByCats = getNewsbyCat(cat);
        for (NewsWithKeywords newsNLUByCat : newsNLUByCats) {
            csvFile.append(newsNLUByCat.getKeywords()).append(",").append(cat).append("\n");
        }
        return csvFile.toString();
    }

    @Override
    public List<String> allCategories() {

        List<String> allcat = new ArrayList<>();

        //Query for retrieve all documents of db
        JsonObject read = jsonObjectreaderFromCloudantId("_all_docs", getDbCategories());
        JsonArray jsonElements = read.getAsJsonArray("rows");

        for (JsonElement jsonElement : jsonElements) {
            String id = jsonElement.getAsJsonObject().get("id").getAsString();
            allcat.add(id);
        }

        return allcat;
    }


    public CloudantCategoriesDAO() {
        createConnectionWithCloudant();
    }
}
