package main;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;

public class Game extends Canvas implements Runnable {
    private boolean isRunning = false;
    private Thread thread;

    // You can set preferred screen size (width and height)
    private final int GAME_SIZE = 640;

    // These constants shouldn't be changed
    private final int WIDTH = GAME_SIZE + 14;
    private final int HEIGHT = GAME_SIZE + 37;

    // You can set preferred game block size (snake's body, food, etc.)
    private final int BLOCK_SIZE = 64 * 2;

    private double amountOfTicks = 1.0;

    // Snake coordinates
    private final int[] snakeX = new int[(GAME_SIZE / BLOCK_SIZE) * (GAME_SIZE / BLOCK_SIZE)];
    private final int[] snakeY = new int[(GAME_SIZE / BLOCK_SIZE) * (GAME_SIZE / BLOCK_SIZE)];

    // Starting snake's body size
    private int size = 2;

    // Player's score
    private int score = 0;

    // Food coordinates
    private int foodX;
    private int foodY;

    // Snake direction
    private enum Direction {
        Right,
        Left,
        Up,
        Down,
        None
    }
    private Direction direction = Direction.Right;
    private Direction nextDirection = Direction.None;

    private enum State {
        Menu,
        Game
    }

    // Game constructor
    public Game() {
        new Window(new Dimension(WIDTH, HEIGHT), "snake", this);

        init();

        start();
    }

    // Initializing keyboard adapter, snake and food
    private void init() {
        addKeyListener(new KeyInput());

        for (int i = 0; i < size; i++) {
            snakeX[i] = (GAME_SIZE / BLOCK_SIZE) / 2 - i;
            snakeY[i] = (GAME_SIZE / BLOCK_SIZE) / 2;
        }

        locateFood();
    }

    private synchronized void start() {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    private synchronized void stop() {
        isRunning = false;

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        this.requestFocus();
        long lastTime = System.nanoTime();
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();

        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                tick();
                delta--;
            }
            render();

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
            }
        }

        stop();
    }

    private synchronized void move() {
        for (int i = size - 1; i > 0; i--) {
            snakeX[i] = snakeX[i - 1];
            snakeY[i] = snakeY[i - 1];
        }

        if (nextDirection != Direction.None) {
            direction = nextDirection;
        }

        if (direction == Direction.Right) {
            snakeX[0]++;

            snakeX[0] %= GAME_SIZE / BLOCK_SIZE;
        }

        if (direction == Direction.Left) {
            snakeX[0]--;

            if (snakeX[0] < 0) {
                snakeX[0] = GAME_SIZE / BLOCK_SIZE - 1;
            }
        }

        if (direction == Direction.Up) {
            snakeY[0]--;

            if (snakeY[0] < 0) {
                snakeY[0] = GAME_SIZE / BLOCK_SIZE - 1;
            }
        }

        if (direction == Direction.Down) {
            snakeY[0]++;

            snakeY[0] %= GAME_SIZE / BLOCK_SIZE;
        }
    }

    // TODO: 2/13/2021  game over screen
    private synchronized void gameOver() {
        for (int i = 2; i < size; i++) {
            if (snakeX[0] == snakeX[i] && snakeY[0] == snakeY[i]) {
                stop();
            }
        }
    }

    private synchronized void checkFood() {
        if (snakeX[0] == foodX && snakeY[0] == foodY) {
            size++;
            snakeX[size - 1] = -1;
            snakeY[size - 1] = -1;
            score += 10;
            locateFood();
        }
    }

    /**
     * TODO: 2/13/2021 optimize
     */
    private synchronized void locateFood() {
        int[] freeX = new int[(GAME_SIZE / BLOCK_SIZE) * (GAME_SIZE / BLOCK_SIZE) - size + 1];
        int[] freeY = new int[(GAME_SIZE / BLOCK_SIZE) * (GAME_SIZE / BLOCK_SIZE) - size + 1];

        int x = 0;
        int y = 0;
        boolean freeCoordinate = true;
        int k = 0;

        for (int i = 0; i < (GAME_SIZE / BLOCK_SIZE) * (GAME_SIZE / BLOCK_SIZE); i++) {
            for (int j = 0; j < size; j++) {
                if (x == snakeX[j] && y == snakeY[j]) {
                    freeCoordinate = false;
                    break;
                }
            }

            if (freeCoordinate) {
                freeX[k] = x;
                freeY[k] = y;
                k++;
            }

            x++;

            if (x == GAME_SIZE / BLOCK_SIZE) {
                x = 0;
                y++;
            }

            freeCoordinate = true;
        }

        int randomCoordinate = (int) (Math.random() * ((GAME_SIZE / BLOCK_SIZE) * (GAME_SIZE / BLOCK_SIZE) - size));
        foodX = freeX[randomCoordinate];
        foodY = freeY[randomCoordinate];
    }

    private class KeyInput extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_A && direction != Direction.Right) {
                nextDirection = Direction.Left;
            }

            if (key == KeyEvent.VK_D && direction != Direction.Left) {
                nextDirection = Direction.Right;
            }

            if (key == KeyEvent.VK_W && direction != Direction.Down) {
                nextDirection = Direction.Up;
            }

            if (key == KeyEvent.VK_S && direction != Direction.Up) {
                nextDirection = Direction.Down;
            }
        }
    }

    private void tick() {
        checkFood();
        move();
        gameOver();
    }

    //todo: 2/20/21 need to make menu/engame/difficulty screens

    private void menu() {
        
    }

    private void endGame() {

    }

    private void difficulty() {

    }

    private void inGame() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.GREEN);
        g.fillRect(foodX * BLOCK_SIZE, foodY * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);

        g.setColor(Color.RED);
        for (int i = 0; i < size; i++) {
            g.fillRect(snakeX[i] * BLOCK_SIZE, snakeY[i] * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
        }

        g.dispose();
        bs.show();
    }

    private void render() {
        inGame();
    }

    public static void main(String[] args) {
        new Game();
    }
}
