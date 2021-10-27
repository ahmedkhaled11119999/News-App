package com.example.newspapersapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    SQLiteDatabase database;
    ArrayAdapter<String> arrayAdapter;
    ListView listView;
    ArrayList<String> titles = new ArrayList<String>();
    ArrayList<String> content = new ArrayList<String>();
    ProgressBar progressBar;

    public class DownloadNews extends AsyncTask<String, Void,String>{

        @Override
        protected String doInBackground(String... urls) {
            String result="";
            URL url;
            HttpURLConnection urlConnection;
            try{
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while(data != -1){
                    char c = (char) data;
                    result += c;
                    data = reader.read();
                }
                //Log.i("Article Data JSON",result);
                JSONArray articleIds = new JSONArray(result);
                int idsLength = 1;
                if(articleIds.length() < idsLength){
                    idsLength = articleIds.length();
                }
                for(int i = 0; i < idsLength; i++){
                    String articleId = articleIds.getString(i);
                    String articleJSONData = "";
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/"+ articleId +".json?print=pretty");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    inputStream = urlConnection.getInputStream();
                    reader = new InputStreamReader(inputStream);
                    data = reader.read();
                    while(data != -1){
                        char c = (char) data;
                        articleJSONData += c;
                        data = reader.read();
                    }
                    JSONObject article = new JSONObject(articleJSONData);
                    String articleTitle = article.getString("title");
                    String articleURL = article.getString("url");
                    String articleContent = "";
                    url = new URL(articleURL);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    inputStream = urlConnection.getInputStream();
                    reader = new InputStreamReader(inputStream);
                    data = reader.read();
                    while(data != -1){
                        char c = (char) data;
                        articleContent += c;
                        data = reader.read();
                    }
                    String sql = "INSERT INTO articles (articleId,title,content) VALUES (?,?,?)";
                    SQLiteStatement statement = database.compileStatement(sql);
                    statement.bindString(1,articleId);
                    statement.bindString(2,articleTitle);
                    statement.bindString(3,articleContent);
                    statement.execute();
                    Log.i("count",String.valueOf(idsLength));

                }
            }
            catch(Exception e){
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            progressBar.setVisibility(View.INVISIBLE);
            updateList();
            String sql = "SELECT COUNT(*) FROM articles";
            SQLiteStatement statement = database.compileStatement(sql);
            long count = statement.simpleQueryForLong();
            Log.i("how many records",String.valueOf(count));
            super.onPostExecute(s);
        }
    }

    public void updateList(){
        Cursor c = database.rawQuery("SELECT * FROM articles",null);
        int contentIndex = c.getColumnIndex("content");
        int titleIndex = c.getColumnIndex("title");
        Log.i("True??",String.valueOf(c.moveToFirst()));
        if (c.moveToFirst()) {
            titles.clear();
            content.clear();

            do {

                titles.add(c.getString(titleIndex));
                content.add(c.getString(contentIndex));

            } while (c.moveToNext());

            arrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        database = this.openOrCreateDatabase("Articles", Context.MODE_PRIVATE,null);
        database.execSQL("CREATE TABLE IF NOT EXISTS articles (articleId INT(15),title VARCHAR, content VARCHAR, id INTEGER PRIMARY KEY)");
//        database.execSQL("DELETE FROM articles");

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,titles);
        listView.setAdapter(arrayAdapter);
//        updateList();

        DownloadNews downloadNews = new DownloadNews();
        String articlesId = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";
        try {
            downloadNews.execute(articlesId);
        }catch (Exception e){
            e.printStackTrace();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,WebViewActivity.class);
                intent.putExtra("content",content.get(position));
                startActivity(intent);
            }
        });

    }
}