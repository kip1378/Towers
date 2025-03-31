package io.github.towerdefense;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Enemy {
    private Texture texture;
    private float x, y;
    private float speed = 1000f; // пикселей в секунду
    private float stateTime = 0f; // stateTime юзаем чтобы понять какой кадр анимации нужно показывать в данный момент, 0f первый кадр, 0.2f второй и т.д
    private Animation<TextureRegion> walkAnimation;

    private Animation<TextureRegion> attackAnimation;
    private boolean isAttacking = false;
    private TextureRegion[] attackFrames;

    private boolean isGameOver = false;
    private float attackStateTime = 0f;

    private int currentTargetIndex = 0;
    private float[][] path;





    public Enemy(Texture texture, float[][] path) { //конструктор
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
        createAttacAnimation();
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

    private void createAttacAnimation () {
        attackFrames = new TextureRegion[12];
        for (int i = 0; i < 12; i++) {
            Texture frame = new Texture(Gdx.files.internal("orc/Slashing/Orc_Slashing_" + i + ".png"));
            attackFrames[i] = new TextureRegion(frame); //обновляем кадр атаки
        }
        attackAnimation = new Animation<>(0.1f, attackFrames);
    }




    public void update(float delta) {

        stateTime += delta; //delta — это время между кадрами (обычно ~0.016 сек)

        if (currentTargetIndex >= path.length) return;
        if (isAttacking) {
            // уже атакует — не двигается
            return;
        }

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

        if (!isAttacking && currentTargetIndex == path.length - 1) {
             dx = path[currentTargetIndex][0] - x;
             dy = path[currentTargetIndex][1] - y;
             dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < 32f) { // половина клетки (64 / 2)
                isAttacking = true;
                attackStateTime = 0;
                System.out.println("ATTACK! Game Over.");
                TowerDefenseGame.triggerGameOver();
                 // Останавливаем движение
            }
        }

//        if (!isAttacking && currentTargetIndex == path.length - 1) { // path.length - 1 = последняя точка
//            float dx2 = path[currentTargetIndex][0] - x;
//            float dy2 = path[currentTargetIndex][1] - y;
//            float dist2 = (float) Math.sqrt(dx2 * dx2 + dy2 * dy2);
//
//            if (dist2 < 32f) { // ← будет ближе к замку
//                isAttacking = true;
//                attackStateTime = 0;
//                System.out.println("ATTACK! Game Over.");
//
//                return; // остановка
//            }
//        }
    }



    public void render(SpriteBatch batch) {

        TextureRegion currentFrame = walkAnimation.getKeyFrame(stateTime, true); //walkAnimation.getKeyFrame выбирает нужный кадр, true — означает зацикливание (анимация будет повторяться)

        if (isAttacking && attackAnimation != null) {
            attackStateTime += Gdx.graphics.getDeltaTime();
            currentFrame = attackAnimation.getKeyFrame(attackStateTime, true);
        } else {
            currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        }

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
