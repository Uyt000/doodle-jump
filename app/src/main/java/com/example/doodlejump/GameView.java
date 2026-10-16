package com.example.doodlejump;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {
    public interface GameListener {
        void onScoreChanged(int score);
        void onGameOver(int finalScore, int highScore);
    }

    private GameThread thread;
    private GameListener listener;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private Player player;
    private List<Platform> platforms;
    private int score;
    private int highScore;
    private boolean gameOver;
    private boolean isRunning;
    private Random random = new Random();

    private int screenWidth;
    private int screenHeight;

    private float worldOffsetY;
    private float tiltX;

    private Paint paint;
    private RectF tmpRect = new RectF();

    public GameView(Context context, GameListener listener) {
        super(context);
        this.listener = listener;
        getHolder().addCallback(this);
        paint = new Paint();
        paint.setFilterBitmap(false);

        SharedPreferences prefs = context.getSharedPreferences("doodle_prefs", Context.MODE_PRIVATE);
        highScore = prefs.getInt("high_score", 0);

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenWidth = getWidth();
        screenHeight = getHeight();
        initGame();
        thread = new GameThread(holder);
        thread.setRunning(true);
        thread.start();
    }

    private void initGame() {
        player = new Player(screenWidth / 2f - 24, screenHeight - 200);
        platforms = new ArrayList<>();
        score = 0;
        worldOffsetY = 0;
        gameOver = false;

        for (int i = 0; i < 10; i++) {
            float px = random.nextFloat() * (screenWidth - Platform.WIDTH);
            float py = screenHeight - i * 150 - 100;
            platforms.add(new Platform(px, py));
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) { }
        }
    }

    private class GameThread extends Thread {
        private SurfaceHolder holder;
        private boolean running;

        GameThread(SurfaceHolder holder) { this.holder = holder; }
        void setRunning(boolean running) { this.running = running; }

        @Override
        public void run() {
            long lastTime = System.nanoTime();
            while (running) {
                long now = System.nanoTime();
                float delta = (now - lastTime) / 1_000_000_000f;
                lastTime = now;
                if (delta > 0.05f) delta = 0.05f;

                if (!gameOver && isRunning) update(delta);

                Canvas canvas = null;
                try {
                    canvas = holder.lockCanvas();
                    if (canvas != null) {
                        synchronized (holder) { drawGame(canvas); }
                    }
                } finally {
                    if (canvas != null) holder.unlockCanvasAndPost(canvas);
                }

                try { Thread.sleep(16); } catch (InterruptedException e) { }
            }
        }
    }

    private boolean rectsOverlap(RectF a, RectF b) {
        return a.left < b.right && a.right > b.left && a.top < b.bottom && a.bottom > b.top;
    }

    private void update(float delta) {
        float speed = 400f;
        player.x += tiltX * speed * delta;

        if (player.x < -Player.WIDTH) player.x = screenWidth;
        if (player.x > screenWidth) player.x = -Player.WIDTH;

        player.vy += 1500f * delta;
        player.y += player.vy * delta;

        if (player.vy > 0) {
            RectF pb = player.getBounds();
            for (Platform p : platforms) {
                tmpRect.set(p.x, p.y, p.x + Platform.WIDTH, p.y + Platform.HEIGHT);
                if (rectsOverlap(pb, tmpRect)
                        && player.y + Player.HEIGHT - player.vy * delta <= p.y + 10) {
                    player.vy = -700f;
                    break;
                }
            }
        }

        float threshold = screenHeight * 0.4f;
        if (player.y < threshold) {
            float scroll = threshold - player.y;
            player.y = threshold;
            worldOffsetY += scroll;
            for (Platform p : platforms) p.y += scroll;

            while (platforms.size() < 12) {
                float px = random.nextFloat() * (screenWidth - Platform.WIDTH);
                float py = platforms.get(platforms.size() - 1).y - 120 - random.nextFloat() * 80;
                platforms.add(new Platform(px, py));
            }
            platforms.removeIf(p -> p.y > screenHeight + 200);
        }

        int newScore = (int) (worldOffsetY / 10);
        if (newScore != score) {
            score = newScore;
            if (listener != null) listener.onScoreChanged(score);
        }

        if (player.y > screenHeight + 100) {
            gameOver = true;
            if (score > highScore) {
                highScore = score;
                SharedPreferences prefs = getContext().getSharedPreferences("doodle_prefs", Context.MODE_PRIVATE);
                prefs.edit().putInt("high_score", highScore).apply();
            }
            if (listener != null) listener.onGameOver(score, highScore);
        }
    }

    private void drawGame(Canvas canvas) {
        canvas.drawColor(0xFF1a1a2e);

        paint.setColor(0xFF4CAF50);
        for (Platform p : platforms) {
            canvas.drawRect(p.x, p.y, p.x + Platform.WIDTH, p.y + Platform.HEIGHT, paint);
        }

        paint.setColor(0xFFFFEB3B);
        canvas.drawRect(player.x, player.y, player.x + Player.WIDTH, player.y + Player.HEIGHT, paint);
        paint.setColor(0xFF000000);
        canvas.drawRect(player.x + 6, player.y + 8, player.x + 10, player.y + 12, paint);
        canvas.drawRect(player.x + 18, player.y + 8, player.x + 22, player.y + 12, paint);

        if (gameOver) {
            paint.setColor(0xAA000000);
            canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
            paint.setColor(0xFFFFFFFF);
            paint.setTextSize(48);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Игра окончена", screenWidth / 2f, screenHeight / 2f - 60, paint);
            paint.setTextSize(32);
            canvas.drawText("Счёт: " + score, screenWidth / 2f, screenHeight / 2f, paint);
            canvas.drawText("Рекорд: " + highScore, screenWidth / 2f, screenHeight / 2f + 50, paint);
        }
    }

    public void resetGame() {
        initGame();
        isRunning = true;
    }

    public void resume() {
        isRunning = true;
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void pause() {
        isRunning = false;
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float raw = -event.values[0] / 9.81f;
            if (Math.abs(raw) < 0.1f) raw = 0;
            tiltX = Math.max(-1f, Math.min(1f, raw));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
