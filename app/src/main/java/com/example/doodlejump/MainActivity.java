package com.example.doodlejump;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private GameView gameView;
    private FrameLayout container;
    private TextView scoreText;
    private Button restartButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        prefs = getSharedPreferences("doodle_prefs", Context.MODE_PRIVATE);

        container = new FrameLayout(this);
        setContentView(container);

        gameView = new GameView(this, new GameView.GameListener() {
            @Override
            public void onScoreChanged(int score) {
                runOnUiThread(() -> scoreText.setText("Счёт: " + score));
            }

            @Override
            public void onGameOver(int finalScore, int highScore) {
                runOnUiThread(() -> {
                    restartButton.setVisibility(View.VISIBLE);
                    String device = Build.MANUFACTURER + " " + Build.MODEL;
                    GoogleSheetUploader.uploadScore(finalScore, highScore, device);
                });
            }
        });
        container.addView(gameView);

        scoreText = new TextView(this);
        scoreText.setTextColor(0xFFFFFFFF);
        scoreText.setTextSize(20);
        scoreText.setPadding(30, 30, 0, 0);
        container.addView(scoreText);

        TextView highScoreText = new TextView(this);
        highScoreText.setTextColor(0xFFFFD700);
        highScoreText.setTextSize(18);
        highScoreText.setPadding(30, 80, 0, 0);
        highScoreText.setText("Рекорд: " + prefs.getInt("high_score", 0));
        container.addView(highScoreText);

        restartButton = new Button(this);
        restartButton.setText("Заново");
        restartButton.setVisibility(View.GONE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        restartButton.setLayoutParams(params);
        restartButton.setOnClickListener(v -> {
            restartButton.setVisibility(View.GONE);
            gameView.resetGame();
        });
        container.addView(restartButton);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
}
