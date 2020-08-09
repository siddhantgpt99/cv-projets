package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;

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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> contents = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    SQLiteDatabase articleDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        articleDB = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);
        articleDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleID INTEGER, title VARCHAR, content VARCHAR)");

        DownloadTask task = new DownloadTask();
        try{
            //task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        }catch(Exception e) {
            e.printStackTrace();
        }


        ListView listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                intent.putExtra("content", contents.get(position));
                startActivity(intent);
            }
        });

        updateListView();
    }

    public void updateListView(){
        Cursor c = articleDB.rawQuery("SELECT * FROM articles", null);
        int titleIndex =c.getColumnIndex("title");
        int contentIndex = c.getColumnIndex("content");

        if(c.moveToFirst()){
            titles.clear();
            contents.clear();

            do{
                titles.add(c.getString(titleIndex));
                contents.add(c.getString(contentIndex));
            }while(c.moveToNext());
            arrayAdapter.notifyDataSetChanged();
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            URL url;
            HttpURLConnection urlConnection = null;
            String result="";
            try{
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data!=-1) {
                    char current = (char) data;
                    result+=current;
                    data = reader.read();
                }

                JSONArray jsonArray = new JSONArray(result);
                int number = 20;
                if(jsonArray.length()<20) {
                    number = jsonArray.length();
                }

                articleDB.execSQL("DELETE FROM articles");
                for(int i=0;i<number;i++) {
                    String articleID = jsonArray.getString(i);
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/"+articleID+".json?print=pretty");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    String articleData = "";
                    in = urlConnection.getInputStream();
                    reader = new InputStreamReader(in);
                    data = reader.read();
                    while(data!=-1) {
                        char c = (char) data;
                        articleData+=c;
                        data = reader.read();
                    }
                    JSONObject jsonObject = new JSONObject(articleData);

                    if(!jsonObject.isNull("title") && !jsonObject.isNull("url")) {
                        String articleTitle = jsonObject.getString("title");
                        url = new URL(jsonObject.getString("url"));
                        urlConnection = (HttpURLConnection) url.openConnection();
                        String articleContent = "";
                        in = urlConnection.getInputStream();
                        reader = new InputStreamReader(in);
                        data = reader.read();
                        while(data!=-1) {
                            char c = (char) data;
                            articleContent+=c;
                            data = reader.read();
                        }

                        Log.i("RESULT", articleContent);

                        String sql = "INSERT INTO articles (articleID, title, content) VALUES (?, ?, ?)";
                        SQLiteStatement sqLiteStatement = articleDB.compileStatement(sql);
                        sqLiteStatement.bindString(1,articleID);
                        sqLiteStatement.bindString(2,articleTitle);
                        sqLiteStatement.bindString(3,articleContent);
                        sqLiteStatement.execute();
                    }
                }


                return result;

            }catch(Exception e){
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            updateListView();
        }
    }

}
