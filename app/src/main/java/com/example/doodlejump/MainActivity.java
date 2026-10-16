package com.example.doodlejump;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private GameView gameView;
    private FrameLayout container;
    private TextView scoreText;
    private TextView playerText;
    private Button restartButton;
    private SharedPreferences prefs;
    private String playerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        prefs = getSharedPreferences("doodle_prefs", Context.MODE_PRIVATE);

        playerName = prefs.getString("player_name", "");

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
                    GoogleSheetUploader.uploadScore(playerName, finalScore, highScore, device);
                });
            }
        });
        container.addView(gameView);

        scoreText = new TextView(this);
        scoreText.setTextColor(0xFFFFFFFF);
        scoreText.setTextSize(20);
        scoreText.setPadding(30, 30, 0, 0);
        container.addView(scoreText);

        playerText = new TextView(this);
        playerText.setTextColor(0xFF00E5FF);
        playerText.setTextSize(16);
        playerText.setPadding(30, 60, 0, 0);
        playerText.setText("Игрок: " + (playerName.isEmpty() ? "—" : playerName));
        container.addView(playerText);

        TextView highScoreText = new TextView(this);
        highScoreText.setTextColor(0xFFFFD700);
        highScoreText.setTextSize(18);
        highScoreText.setPadding(30, 90, 0, 0);
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

        Button changePlayerButton = new Button(this);
        changePlayerButton.setText("Сменить игрока");
        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params2.gravity = Gravity.TOP | Gravity.END;
        params2.topMargin = 20;
        params2.rightMargin = 20;
        changePlayerButton.setLayoutParams(params2);
        changePlayerButton.setOnClickListener(v -> showNameDialog());
        container.addView(changePlayerButton);

        if (playerName.isEmpty()) {
            showNameDialog();
        }
    }

    private void showNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите имя игрока");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Например: Иван");
        input.setText(playerName);
        input.setPadding(40, 40, 40, 40);
        builder.setView(input);

        builder.setCancelable(false);
        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) name = "Аноним";
            playerName = name;
            prefs.edit().putString("player_name", name).apply();
            playerText.setText("Игрок: " + name);
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> {
            if (playerName.isEmpty()) {
                playerName = "Аноним";
                prefs.edit().putString("player_name", playerName).apply();
                playerText.setText("Игрок: " + playerName);
            }
        });

        builder.show();
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
