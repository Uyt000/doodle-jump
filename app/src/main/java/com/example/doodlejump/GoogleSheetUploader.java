package com.example.doodlejump;

import android.util.Log;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GoogleSheetUploader {
    // URL из Apps Script
    private static final String SCRIPT_URL = "package com.example.doodlejump;

import android.util.Log;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GoogleSheetUploader {
    // URL из Apps Script
    private static final String SCRIPT_URL = "https://script.google.com/macros/s/AKfycbziLnPA6VjjJo-_fNznv7grF1bhegUVvjgRlfiY0rCtXFH77DZu2OH5Fh51r056ABf4YA/exec";

    public static void uploadScore(String player, int score, int highScore, String deviceModel) {
        OkHttpClient client = new OkHttpClient();

        FormBody body = new FormBody.Builder()
                .add("player", player)
                .add("score", String.valueOf(score))
                .add("highScore", String.valueOf(highScore))
                .add("device", deviceModel)
                .build();

        Request request = new Request.Builder()
                .url(SCRIPT_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("SheetUpload", "Ошибка: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body() != null ? response.body().string() : "";
                Log.d("SheetUpload", "HTTP " + response.code() + " → " + resp);
            }
        });
    }
}";

    public static void uploadScore(String player, int score, int highScore, String deviceModel) {
        OkHttpClient client = new OkHttpClient();

        FormBody body = new FormBody.Builder()
                .add("player", player)
                .add("score", String.valueOf(score))
                .add("highScore", String.valueOf(highScore))
                .add("device", deviceModel)
                .build();

        Request request = new Request.Builder()
                .url(SCRIPT_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("SheetUpload", "Ошибка: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body() != null ? response.body().string() : "";
                Log.d("SheetUpload", "HTTP " + response.code() + " → " + resp);
            }
        });
    }
}
