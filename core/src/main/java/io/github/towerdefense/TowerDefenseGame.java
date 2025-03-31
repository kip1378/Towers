package io.github.towerdefense;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class TowerDefenseGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture tileRoad, tileEmpty;

    private Enemy enemy;
    private Texture enemyTexture;
    private float[][] path;



    // карта 25x14 клеток
    private final int[][] map = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    private float[][] generatePathFromMap() {
        List<float[]> pathList = new ArrayList<>();
        Set<String> visited = new HashSet<>();


        // Найдём стартовую точку — первый tile с 1 в любом столбце, начиная слева снизу
        int startCol = -1, startRow = -1;
        outer:
        for (int col = 0; col < map[0].length; col++) {
            for (int row = map.length - 1; row >= 0; row--) {
                if (map[row][col] == 1) {
                    startCol = col;
                    startRow = row;
                    break outer;
                }
            }
        }

        if (startCol == -1) return new float[0][]; // нет дороги

        int col = startCol, row = startRow;

        // Направления: вправо, вверх, вниз, влево
        int[][] directions = {
            {1, 0},  // →
            {0, -1}, // ↑
            {0, 1},  // ↓
            {-1, 0}  // ←
        };

        while (true) {
            pathList.add(new float[]{col * 64f, (map.length - 1 - row) * 64f});
            visited.add(col + "," + row);

            boolean moved = false;
            for (int[] d : directions) {
                int nc = col + d[0];
                int nr = row + d[1];
                if (nc >= 0 && nc < map[0].length && nr >= 0 && nr < map.length &&
                    map[nr][nc] == 1 && !visited.contains(nc + "," + nr)) {
                    col = nc;
                    row = nr;
                    moved = true;
                    break;
                }
            }

            if (!moved) break; // нет куда идти — конец маршрута
        }
        System.out.println("First path tile: " + pathList.get(0)[0] + "," + pathList.get(0)[1]);
        return pathList.toArray(new float[0][]);
    }


    @Override
    public void create() {
        batch = new SpriteBatch();
        tileRoad = new Texture("road.png");   // это нужно будет добавить
        tileEmpty = new Texture("empty.png"); // и это тоже

        path = generatePathFromMap();
        System.out.println("PATH SIZE = " + path.length);

        enemyTexture = new Texture("enemy.png");

        // Пример пути — массив координат по X и Y (пиксели)
        enemy = new Enemy(enemyTexture, path);
    }

    @Override
    public void render() {  //Автоматически вызывается LibGDX каждый кадр, т.е. ~60 раз в секунду.
        Gdx.gl.glClearColor(0, 0, 0, 1); // очищаем экран (цвет: чёрный), Без этого старые кадры накладывались бы на новые.
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        batch.begin(); //Начинает отрисовку спрайтов (текстур).
        for (int row = 0; row < map.length; row++) { //высота
            for (int col = 0; col < map[0].length; col++) { // ширина
                int tile = map[row][col];
                Texture texture = switch (tile) {
                    case 1 -> tileRoad;
                    default -> tileEmpty;
                };
                batch.draw(texture, col * 64, (map.length - 1 - row) * 64); //отрисовка
                //LibGDX рисует снизу вверх, а двумерный массив идёт сверху вниз, как в Excel.
                //Чтобы корректно отрисовать карту, нужно «перевернуть» ось Y. поэтому -1
            }
        }
        enemy.update(Gdx.graphics.getDeltaTime());
        enemy.render(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        tileRoad.dispose();
        tileEmpty.dispose();
        enemyTexture.dispose();
        //освобождает ресурсы
    }
}

// Всё это делается ~60 раз в секунду
