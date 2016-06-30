package com.example.android.newsapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // The base URL for querying the guardian app
    private static final String GUARDIAN_NEWS_URL = "http://content.guardianapis.com/search?q=";
    // The options string to append to the url with keywords
    private static final String NEWS_OPTION_STRING = "&api-key=test&show-fields=thumbnail&order-by=newest";
    // InputStream for retrieving JSON data from the guardian api
    private InputStream is;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView search = (ImageView) findViewById(R.id.search_image_view);
        search.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String keywords = "";
                // Check that there is a valid network connection
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    ListView listView = (ListView) findViewById(R.id.list);
                    // reset the adapter to null
                    listView.setAdapter(null);

                    // get the keywords the user entered and format them for querying the api
                    EditText searchField = (EditText) findViewById(R.id.search_box);
                    keywords = searchField.getText().toString().replace(' ', '+');

                    // handle zero-length user input, which would cause the app to crash
                    if (keywords.length() == 0) {
                        Toast.makeText(MainActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                    } else {
                        // run asynctask to get data
                        new FetchNewsTask().execute(getSearchQuery(keywords));
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class FetchNewsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return fetchNews(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            ArrayList<NewsItem> news = buildNewsList(result);
            // Grab the no_results_found textview so we can change it between gone and visible based
            // on whether we found any books or not
            TextView no_results = (TextView) findViewById(R.id.no_results_text_view);
            if (news != null) {
                no_results.setVisibility(TextView.GONE);
                NewsItemAdapter adapter = new NewsItemAdapter(MainActivity.this, news);
                ListView listView = (ListView) findViewById(R.id.list);
                listView.setAdapter(adapter);
            } else {
                no_results.setVisibility(TextView.VISIBLE);
            }
        }
    }

    /**
     * Returns the full url used to query the google books api with the keywords that the user entered.
     */
    private String getSearchQuery(String keywords) {
        return GUARDIAN_NEWS_URL + keywords + NEWS_OPTION_STRING;
    }

    /**
     * Download and fetch the book JSONObjects as a string from the Guardian API.
     * Returns null if the method fails to produce a string to use.
     */
    private String fetchNews(String query) {
        try {
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // configure connection
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // start the query
            conn.connect();

            is = conn.getInputStream();
            return readResponse(is);
        } catch (IOException e) {
            Log.e("fetchNews", e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e("fetchNews", e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Convert the Input Stream returned by the FetchBookTask into a string.
     */
    private String readResponse(InputStream is) {
        StringBuilder builder = new StringBuilder();
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));
        // Read the inputstream one line at a time, storing the full string in a bufferedreader
        try {
            String line = responseReader.readLine();
            while (line != null) {
                builder.append(line);
                line = responseReader.readLine();
            }
        } catch (IOException e) {
            Log.e("readResponse", e.getMessage());
        }
        return builder.toString();
    }

    /**
     * Parse the string to retrieve the JSONObjects it represents, and return an ArrayList
     * of Books constructed from the JSONObjects.
     */
    private ArrayList<NewsItem> buildNewsList(String data) {
        ArrayList<NewsItem> news = new ArrayList<NewsItem>();

        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jArray = jsonObject.getJSONObject("response").getJSONArray("results");
            // Iterate through our JSONArray structure, working with on volumeInfo at a time
            // Each volumeInfo represents one book
            for (int i = 0; i < jArray.length(); ++i) {
                // Grab the title
                String title = jArray.getJSONObject(i).getString("webTitle");
                // Grab the section
                String section = jArray.getJSONObject(i).getString("sectionName");
                // Grab the thumbnail
                String imgUrl = jArray.getJSONObject(i).getJSONObject("fields").getString("thumbnail");
                // Grab the website address for the article
                String contentUrl = jArray.getJSONObject(i).getString("webUrl");
                news.add(new NewsItem(imgUrl, title, section, contentUrl));
            }
            return news;
        } catch (JSONException e) {
            Log.e("buildNewsList", e.getMessage());
            return null;
        }
    }
}
