package com.example.newspapersapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        Intent intent = getIntent();
        String html = intent.getStringExtra("content");
//        String newHtml = Base64.encodeToString(html.getBytes(), Base64.DEFAULT);
//        Log.i("CONTENT",newHtml);
//        webView.loadData(html,"text/html","UTF-8");
        webView.loadDataWithBaseURL(null,html,"text/html","UTF-8",null);


    }
}