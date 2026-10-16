package com.example.doodlejump;

import android.graphics.RectF;

public class Player {
    public static final int WIDTH = 36;
    public static final int HEIGHT = 40;
    public float x, y;
    public float vy;

    public Player(float x, float y) {
        this.x = x;
        this.y = y;
        this.vy = 0;
    }

    public RectF getBounds() {
        return new RectF(x, y, x + WIDTH, y + HEIGHT);
    }
}
