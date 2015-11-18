package com.example.jwjiang.kamcordwatchview;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ProgressDialog progress;
    private ListView watchList;
    private ArrayList<WatchItem> list;
    private WatchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.loading));
        progress.setCancelable(false);
        progress.show();

        watchList = (ListView) findViewById(R.id.watchList);

        list = new ArrayList<WatchItem>();
        adapter = new WatchAdapter(this, true, list);
        watchList.setAdapter(adapter);

        startHttp(false);
    }

    // check if network is available
    public boolean checkNetwork() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = connMgr.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnected()) {
            return true;
        }
        else return false;
    }

    // return cache of bitmaps
    public BitmapCache getCache() {
        return BitmapCache.getInstance();
    }

    //
    public void startHttp(boolean gettingNextPage) {
        // don't do anything web-related if network is unavailable
        if (!checkNetwork()) return;
        progress.show();
        try {
            HttpHandler handler = HttpHandler.getInstance();
            Log.d("got instance", "succesful");
            if (gettingNextPage) {
                handler.getFeed(handler.getNextUrl(), this, gettingNextPage);
            }
            else handler.getFeed(null, this, gettingNextPage);
        } catch (Exception e) {
            Log.d("startHttp", e.getMessage());
        }
    }

    // create a JSONObject from String result returned by an AsyncTask in Httphandler
    // use the JSONObject to populate a watchList of WatchItems and populate the list view
    public void getWatchList(String result, boolean gettingNextPage) {
        if (!gettingNextPage) list.clear();
        else {
            // if getting a next_page, remove the Load More Videos item from the list temporarily
            // since it will cause a crash with its null values
            list.remove(list.size()-1);
        }
        try {
            JSONObject json = new JSONObject(result);
            if (json == null) {
                Log.d("json status", "is null");
                return;
            }
            Log.d("json status", "not null");

            // parse JSON object
            JSONObject response = json.optJSONObject("response");
            JSONObject outerVidArray = response.getJSONObject("video_list");
            JSONArray innerVidArray = outerVidArray.getJSONArray("video_list");
            int vidCount = innerVidArray.length();
            // get relevant info for each video object, and use create a WatchItem and add that
            // to the watchList
            for (int i = 0; i < vidCount; i++) {
                JSONObject video = innerVidArray.getJSONObject(i);
                String title = video.getString("title");
                String imgUrl = video.getJSONObject("thumbnails").getString("regular");
                String vidUrl = video.getString("video_url");
                WatchItem item = new WatchItem(vidUrl, imgUrl, title);
                list.add(item);
            }
            String nextPage = outerVidArray.getString("next_page");
            if (nextPage != null) {
                // add another item to be replaced by the Load More Videos button at the bottom
                list.add(new WatchItem(null, null, null));
                HttpHandler.getInstance().setNextUrl(nextPage);
                reloadUI(true);
            }
            else {
                reloadUI(false);
            }
            dismissProgress();
        } catch (Exception e) {
            Log.d("getWatchList", e.getMessage());
        }
    }

    // reload the list and scroll to the position from before reloading
    private void reloadUI(boolean hasNextPage) {
        int lastPosition = watchList.getFirstVisiblePosition();
        watchList = (ListView) findViewById(R.id.watchList);
        adapter = new WatchAdapter(this, hasNextPage, list);
        watchList.setAdapter(adapter);
        watchList.setSelection(lastPosition);
    }

    public void dismissProgress() {
        progress.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // empty the list and reload (basically like restarting the app, except with some bitmaps
        // still saved in cache)
        if (id == R.id.action_refresh) {
            list.clear();
            WatchAdapter adapter = new WatchAdapter(this, true, list);
            watchList.setAdapter(adapter);
            startHttp(false);
        }

        return super.onOptionsItemSelected(item);
    }
}
