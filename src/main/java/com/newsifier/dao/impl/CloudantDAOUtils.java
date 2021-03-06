package com.newsifier.dao.impl;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.newsifier.utils.Credentials;
import com.newsifier.utils.Logger;

import static com.newsifier.utils.Utils.getCredentials;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Provides methods to manage Cloudant 
 */
public class CloudantDAOUtils {

    private static Database dbMaster;
    private static Database dbCategories;
    private static CloudantClient cloudantClient;

    protected static Database getDbMaster() {
        return dbMaster;
    }

    protected static Database getDbCategories() {
        return dbCategories;
    }

    /**
     * Reads a document from a Cloudant by ID.
     * Returns the retrieved document as JsonObject
     */
    protected static JsonObject jsonObjectreaderFromCloudantId(String id, Database d) {
        JsonObject output;
        InputStream is = d.find(id);
        BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        int i;
        char c;
        String doc = "";
        try {
            while ((i = in.read()) != -1) {
                c = (char) i;
                doc += c;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonParser parser = new JsonParser();
        output = parser.parse(doc).getAsJsonObject();
        return output;
    }

    /**
     *  Creates and returns an object representing a Cloudant Client
     */
    private static CloudantClient getCloudantClient() {
        JsonObject credentials = getCredentials("cloudantNoSQLDB", Credentials.getUsernameDbCloudant(), Credentials.getPasswordDbCloudant());

        String username = credentials.get("username").getAsString();
        String password = credentials.get("password").getAsString();

        try {
            cloudantClient = ClientBuilder.url(new URL("https://" + username + ".cloudant.com"))
                    .username(username)
                    .password(password)
                    .build();
            return cloudantClient;
        } catch (MalformedURLException ex) {
            Logger.logErr(ex.getMessage());
        }

        return null;
    }

    protected static void createConnectionWithCloudant() {

        while (true) {
            try {
                // Create a new CloudantClient instance
                cloudantClient = getCloudantClient();

                // Show the server version
                //Logger.log("Server Version: " + client.serverVersion());

                // Get a List of all the databases this Cloudant account
                //  List<String> databases = client.getAllDbs();
                //  Logger.log("All my databases : ");
                //  for (String dbMaster : databases) {
                //      Logger.log(dbMaster);
                //  }

                // Create a new database.
                dbMaster = cloudantClient.database("newsifier_db", true);
                dbCategories = cloudantClient.database("newsifier_db_categories", true);

                return;

            } catch (com.cloudant.client.org.lightcouch.CouchDbException ex) {
                Logger.logErr("Error retrieving server response");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }
    }


    public static void eraseDatabase(){
        createConnectionWithCloudant();
        cloudantClient.deleteDB(dbMaster.info().getDbName());
        cloudantClient.deleteDB(dbCategories.info().getDbName());
        Logger.webLog("\n\nCloudant documents erased");
        Logger.log("Cloudant documents erased");
    }
}
