package com.example.epinavbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    public static String username;
    public static String image;
    public static String refreshToken;

    private OkHttpClient httpClient;
    private static final String TAG = MainActivity.class.getSimpleName();

    public static String accessToken;
    public static Context contextOfApplication;
    public static Context getContextOfApplication()
    {
        return contextOfApplication;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        contextOfApplication = getApplicationContext();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }

        WebView imgurWebView = (WebView) findViewById(R.id.LoginWebView);
        imgurWebView.setBackgroundColor(Color.TRANSPARENT);
        imgurWebView.loadUrl("https://api.imgur.com/oauth2/authorize?client_id=4ec810250ea92fb&response_type=token");
        imgurWebView.getSettings().setJavaScriptEnabled(true);
        imgurWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("https://imgur.com/")) {
                    splitUrl(url, view);
                    sendAvatarRequest();
                    Intent profile = new Intent(MainActivity.this, Routes.class);
                    startActivity(profile);
                } else {
                    view.loadUrl(url);
                }
                return true;
            }
        });
    }
    private void splitUrl(String url, WebView view) {
        String[] outerSplit = url.split("\\#")[1].split("\\&");
       // String username = null;
//        String accessToken = null;
        //String refreshToken = null;

        int index = 0;
        for (String s : outerSplit) {
            String[] innerSplit = s.split("\\=");
            switch (index) {
                // Access Token
                case 0:
                    accessToken = innerSplit[1];
                    break;

                // Refresh Token
                case 3:
                    refreshToken = innerSplit[1];
                    break;

                // Username
                case 4:
                    username = innerSplit[1];
                    break;
                default:
            }
            index++;
        }
    }

    private void sendAvatarRequest()
    {
        httpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/account/"+ MainActivity.username + "/avatar")
                .header("Authorization", "Bearer" + " " + MainActivity.accessToken)
                .get()
                .build();
        httpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "An error occured " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    String imageUrl = data.getJSONObject("data").getString("avatar");
                    image = imageUrl;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
