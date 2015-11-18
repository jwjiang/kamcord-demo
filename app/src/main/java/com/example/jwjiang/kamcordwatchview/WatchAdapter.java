package com.example.jwjiang.kamcordwatchview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by jwjiang on 11/11/15.
 */
public class WatchAdapter extends ArrayAdapter<WatchItem> {

    private Context mContext;
    private int size;
    private ArrayList<WatchItem> items;
    private static LayoutInflater inflater = null;
    private BitmapCache cache;
    private boolean hasNextPage;

    public WatchAdapter(Context context, boolean hasNextPage, ArrayList<WatchItem> items) {
        super(context, 0, items);
        this.hasNextPage = hasNextPage;
        mContext = context;
        size = items.size();
        this.items = items;
        cache = ((MainActivity) mContext).getCache();
        inflater = LayoutInflater.from(mContext);
    }

    public WatchItem getItem(int position) {
        return items.get(position);
    }

    public int getCount() {
        return size;
    }

    @Override
    public boolean isEnabled (int position) {
        return false;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // If getting a next_page and this is the last item in the list, then create a TextView from
        // item_load_more that calls startHttp from MainActivity
        if (position == getCount()-1 && hasNextPage) {
            convertView = inflater.inflate(R.layout.item_load_more, parent, false);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)mContext).startHttp(true);
                }
            });
            convertView.setEnabled(true);
            return convertView;
        }

        // Get the watch item for this position
        final WatchItem item = getItem(position);

        convertView = inflater.inflate(R.layout.item_watch, parent, false);

        // populate views
        TextView title = (TextView) convertView.findViewById(R.id.title);
        ImageView thumbnail = (ImageView) convertView.findViewById(R.id.image);
        thumbnail.setTag(item.getThumbUrl());
        final String url = item.getThumbUrl();
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri view_uri = Uri.parse(item.getVidUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, view_uri);
                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.startActivity(intent);
                }
            }
        });

        // set title and attempt to get image from cache
        // if not present in cache, get image from URL to set background thumbnail
        title.setText(item.getTitle());
        Bitmap bitmap = cache.getBitmap(url);
        if (bitmap != null) {
            thumbnail.setImageBitmap(bitmap);
            return convertView;
        }

        // method to get the image in the background and return a Bitmap
        // would probably do this with Picasso if not for the Android-only restriction
        class getImageInBg extends AsyncTask<ImageView,Void,Bitmap> {
            ImageView image = null;

            @Override
            protected Bitmap doInBackground(ImageView... params) {
                try {
                    Bitmap bitmap;
                    this.image = params[0];
                    InputStream in = new URL(url).openStream();
                    bitmap = BitmapFactory.decodeStream(in);
                    return bitmap;
                } catch (Exception e) {
                    Log.d("getImageInBg", e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (!image.getTag().toString().equals(url)) {
                    return;
                }
                if (result != null & image != null) {
                    image.setImageBitmap(result);
                    cache.putBitmap(url, result);
                }
                else return;
            }
        }
        try {
            new getImageInBg().execute(thumbnail);

        } catch (Exception e) {
            Log.d("getImageInBg and set", e.getMessage());
        }

        // return completed convertView (if getting image failed, background should be black)
        return convertView;
    }


}
