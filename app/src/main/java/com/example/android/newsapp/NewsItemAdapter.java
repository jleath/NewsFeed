package com.example.android.newsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * An adapter subclass to insert Book objects into ListViews.
 */
public class NewsItemAdapter extends ArrayAdapter<NewsItem> {


    public NewsItemAdapter(Activity context, ArrayList<NewsItem> news) {
        super(context, 0, news);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        // build a new view if we don't have any lying around
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        // have to declare this as final so that we can use it in inner anonymous class to build
        // the web browser intent feature
        final NewsItem currentNewsItem = getItem(position);

        TextView title = (TextView) listItemView.findViewById(R.id.title_text_view);
        TextView author = (TextView) listItemView.findViewById(R.id.section_text_view);
        ImageView image = (ImageView) listItemView.findViewById(R.id.thumbnail);

        title.setText(currentNewsItem.getTitle());
        author.setText(currentNewsItem.getSection());

        // Use Picasso to grab the thumbnail from the given url, resize, crop and load into view
        Picasso.with(this.getContext()).load(currentNewsItem.getImageResourceUrl()).resize(125, 125).centerCrop().into(image);

        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWebPage(getContext(), currentNewsItem.getContentUrl());
            }
        });

        return listItemView;
    }

    /**
     * Fire off an intent to load a web browser with the given url.
     */
    private void openWebPage(Context context, String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }
}
