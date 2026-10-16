package com.example.doodlejump;

import android.graphics.RectF;

public class Platform {
    public static final int WIDTH = 80;
    public static final int HEIGHT = 16;
    public float x, y;

    public Platform(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public RectF getBounds() {
        return new RectF(x, y, x + WIDTH, y + HEIGHT);
    }
}
