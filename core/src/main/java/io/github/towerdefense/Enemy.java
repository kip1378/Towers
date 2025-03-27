package io.github.towerdefense;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Enemy {
    private Texture texture;
    private float x, y;
    private float speed = 100f; // пикселей в секунду

    private int currentTargetIndex = 0;
    private float[][] path;

    public Enemy(Texture texture, float[][] path) {
        this.texture = texture;
        this.path = path;

        // Начальная позиция — первая точка

        if (path.length > 0 && path[0] != null && path[0].length >= 2) {
            this.x = path[0][0];
            this.y = path[0][1];
            System.out.println("Enemy starts at: " + x + "," + y);
        } else {
            System.out.println("⚠️ WARNING: Invalid path, enemy at (0,0)");
            this.x = 0;
            this.y = 0;
        }
    }

//    public void update(float delta) {
//        if (currentTargetIndex >= path.length) return;
//
//        float targetX = path[currentTargetIndex][0];
//        float targetY = path[currentTargetIndex][1];
//
//        float dx = targetX - x;
//        float dy = targetY - y;
//        float dist = (float) Math.sqrt(dx * dx + dy * dy);
//
//        if (dist == 0) return; // защита от деления на ноль
//
//        if (dist < speed * delta) {
//            x = targetX;
//            y = targetY;
//            currentTargetIndex++;
//        } else {
//            x += dx / dist * speed * delta;
//            y += dy / dist * speed * delta;
//        }
//
//        if (Float.isNaN(x) || Float.isNaN(y)) {
//            System.out.println("NaN detected! Stopping movement.");
//            return;
//        }
//
//    }

    public void update(float delta) {
        if (currentTargetIndex >= path.length) return;

        float targetX = path[currentTargetIndex][0];
        float targetY = path[currentTargetIndex][1];

        float dx = targetX - x;
        float dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist == 0f) {
            currentTargetIndex++;
            return;
        }

        // если шаг слишком мал — прыгаем на точку
        if (dist < speed * delta) {
            x = targetX;
            y = targetY;
            currentTargetIndex++;
        } else {
            x += dx / dist * speed * delta;
            y += dy / dist * speed * delta;
        }

        if (Float.isNaN(x) || Float.isNaN(y)) {
            System.out.println("❌ NaN Detected: dx=" + dx + " dy=" + dy + " dist=" + dist);
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y);
//        System.out.println("Enemy at: " + x + "," + y);
    }
}
