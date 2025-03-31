package io.github.towerdefense;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Enemy {
    private Texture texture;
    private float x, y;
    private float speed = 100f; // пикселей в секунду
    private float stateTime = 0f; // stateTime юзаем чтобы понять какой кадр анимации нужно показывать в данный момент, 0f первый кадр, 0.2f второй и т.д
    private Animation<TextureRegion> walkAnimation;

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
        this.walkAnimation = createWalkAnimation();

        // по конвенции ДЖАВА игрулек в конструкторе сначала инициализируют базовые поля (path, x, y, texture)
        //Потом создают анимации, эффекты, компоненты, которые могут их использовать
    }

    private boolean facingLeft = false;


    private Animation<TextureRegion> createWalkAnimation() {
        TextureRegion[] frames = new TextureRegion[9];

        for (int i = 0; i < 9; i++) {
            Texture texture = new Texture(Gdx.files.internal("orc/walk/Orc_Walking_" + i + ".png")); // gdx.files libGDX-ая фигня указывает путь к файлам говорит «Загрузи файл из папки core/assets/...» при сборке проекта
            frames[i] = new TextureRegion(texture);
        }

        return new Animation<>(0.1f, frames); // создается анимация, 0.1 секунды на кадр
    }

    public void update(float delta) {

        stateTime += delta; //delta — это время между кадрами (обычно ~0.016 сек)

        if (currentTargetIndex >= path.length) return;

        float targetX = path[currentTargetIndex][0];
        float targetY = path[currentTargetIndex][1];

        float dx = targetX - x;
        float dy = targetY - y;

        if (Math.abs(dx) > Math.abs(dy)) {  //определяем куда смотрит, если враг в основном движется по X, то смотрим: dx < 0 → идёт влево → facingLeft = true, dx > 0 → идёт вправо → facingLeft = false, y еще не трогаю
            facingLeft = dx < 0;
        }

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
            System.out.println("NaN Detected: dx=" + dx + " dy=" + dy + " dist=" + dist);
        }
    }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = walkAnimation.getKeyFrame(stateTime, true); //walkAnimation.getKeyFrame выбирает нужный кадр, true — означает зацикливание (анимация будет повторяться)
        float drawX = x - (120-64) / 2f;
        // float drawY = y - (120-64) / 2f; не нужно, т.к. сместить надо только по иксу

        if (facingLeft) {
            batch.draw(currentFrame, drawX + 120, y, -120, 120); // зеркалим по X
            //LibGDX рисует текстуру начиная с левого верхнего угла, а если ширина -120, он начинает рисовать в обратную сторону — сдвигаем вправо на 120.

        } else {
            batch.draw(currentFrame, drawX, y, 120, 120); //рисуем прямо
        }
    }
}
