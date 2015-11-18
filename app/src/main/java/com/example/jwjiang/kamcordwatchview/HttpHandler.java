package com.example.jwjiang.kamcordwatchview;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by jwjiang on 11/11/15.
 */
public class HttpHandler {

    private static final String DEVICE_TOKEN = "hi kamcord!";
    private static final String API_URL = "https://app.kamcord.com/app/v3/feeds/featured_feed";
    private static final String PAGE_SUFFIX = "?page=";
    private static final String CONTENT_TYPE = "application/json";
    private static String NEXT_URL = null;
    private HttpsURLConnection connection;


    private static HttpHandler hInstance = null;

    private HttpHandler() {

    }

    public static HttpHandler getInstance() {
        if (hInstance == null) {
            hInstance = new HttpHandler();
        }
        return hInstance;
    }

    public void getFeed(String url, final Context mContext, boolean gettingNextPage) {
        try {
            final boolean next = gettingNextPage;
            // method to get the feed contents in the background and return a json
            class getHttpInBg extends AsyncTask<String,Void,String> {
                @Override
                protected String doInBackground(String... params) {
                    String url = params[0];
                    try {
                        if (url == null) {
                            url = API_URL;
                            connection = (HttpsURLConnection) new URL(url).openConnection();
                        } else {
                            connection = (HttpsURLConnection) new URL(url).openConnection();
                        }
                    } catch (Exception e) {
                        String msg = e.getMessage();
                        if (msg == null) {
                            msg = "Opening connection failed";
                        }
                        Log.d("HttpHandler exception", msg);
                        return null;
                    }
                    try {
                        Log.d("connection open", url);
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("device-token", DEVICE_TOKEN);
                        connection.setRequestProperty("Content-Type", CONTENT_TYPE);
                        connection.setReadTimeout(10000);
                        connection.setConnectTimeout(10000);
                        connection.setDoInput(true);
                    } catch (Exception e) {
                        String msg = e.getMessage();
                        if (msg == null) {
                            msg = "Setting parameters failed";
                        }
                        Log.d("HttpsURLConnection err", msg);
                    }

                    try {
                        InputStream is = connection.getInputStream();
                        Log.d("got input stream", "yes");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                        int responseCode = connection.getResponseCode();
                        Log.d("response code", responseCode + "");
                        StringBuilder builder = new StringBuilder();
                        String content = "";
                        while ((content = reader.readLine()) != null) {
                            builder.append(content + "\n");
                        }
                        reader.close();
                        connection.disconnect();
                        return builder.toString();
                    } catch (Exception e) {
                        String msg = e.getMessage();
                        if (msg == null) {
                            msg = "Reading response failed";
                        }
                        Log.d("HttpsURLConnection err", msg);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(String result) {
                    try {
                        ((MainActivity)mContext).getWatchList(result, next);
                    } catch (Exception e) {
                        Log.d("Making JSONObject", e.getMessage());
                    }
                }
            }
            new getHttpInBg().execute(url);
        } catch (Exception e) {
            Log.d("getFeed exception", e.getMessage());
            return;
        }
    }


    public void setNextUrl(String nextUrl) {
        NEXT_URL = API_URL + PAGE_SUFFIX + nextUrl;
    }

    public String getNextUrl() {
        return NEXT_URL;
    }
}
