package com.example.meaning4;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

public class wordAPI extends AsyncTask<String, Void, String> {

    String app_id = "f36b5505";
    String app_key = "7b2750c26a975f04569270524cbab202";
    String language = "en-gb";
    String word_id = "hello";

    public OkHttpClient client = new OkHttpClient();


    @Override
    protected String doInBackground(String... strings) {

        String url = "https://od-api.oxforddictionaries.com:443/api/v2/entries/" + language + "/" + strings[0].toLowerCase();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("app_id", app_id)
                .addHeader("app_key", app_key)
                .addHeader("Content-type", "application/json").build();
        Log.i("res", request.toString());
        try{
            Response response = client.newCall(request).execute();

            String reu = response.body().string();
            Log.i("res", reu);
            return reu;
        }catch (Exception e){
            Log.i("result", String.valueOf(e));
            Log.i("result", "error1");
        }

        return null;

    }
}
