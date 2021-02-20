package main;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;

public class Game extends Canvas implements Runnable {
    private boolean isRunning = false;
    private Thread thread;

    private final int GAME_SIZE = 640;
    private final int WIDTH = GAME_SIZE + 14;
    private final int HEIGHT = GAME_SIZE + 37;
    private final int BLOCK_SIZE = 8;

    private final int[] x = new int[(GAME_SIZE / BLOCK_SIZE)*(GAME_SIZE / BLOCK_SIZE)];
    private final int[] y = new int[(GAME_SIZE / BLOCK_SIZE)*(GAME_SIZE / BLOCK_SIZE)];
    private int size;
    private int score = 0;

    private int foodX;
    private int foodY;

    private enum Direction {
        Right,
        Left,
        Up,
        Down,
        None
    }


    private Direction direction = Direction.Right;
    private Direction nextDirection = Direction.None;

    public Game() {
        new Window(new Dimension(WIDTH, HEIGHT), "snake", this);

        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

        init();

        start();
    }

    // TODO: 2/13/2021 organize 
    private void init() {
        addKeyListener(new TAdapter());
        setFocusable(true);

        locateFood();

        size = 5;

        int j = 0;
        for (int i = 0; i < size; i++) {
            x[i] = (GAME_SIZE / BLOCK_SIZE)/2 - j;
            y[i] = (GAME_SIZE / BLOCK_SIZE)/2;
            j++;
        }
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
        double amountOfTicks = 1.0;
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
        if (nextDirection != Direction.None) {
            direction = nextDirection;
        }
        if (direction == Direction.Right) {
            x[0]++;
            x[0] %= GAME_SIZE / BLOCK_SIZE;
        }

        if (direction == Direction.Left) {
            x[0]--;

            if (x[0] < 0) {
                x[0] = GAME_SIZE / BLOCK_SIZE-1;
            }
        }

        if (direction == Direction.Up) {
            y[0]--;

            if (y[0] < 0) {
                y[0] = GAME_SIZE / BLOCK_SIZE-1;
            }
        }

        if (direction == Direction.Down) {
            y[0]++;
            y[0] %= GAME_SIZE / BLOCK_SIZE;
        }

        for (int i = size - 1; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
    }

    // TODO: 2/13/2021  game over screen
    private synchronized void gameOver() {
        for (int i = 2; i < size; i++) {
            if (x[0] == x[i] && y[0] == y[i]) {
                stop();
            }
        }
    }

    private synchronized void checkFood() {
        if (x[0] == foodX && y[0] == foodY) {
            size++;
            score+=10;
            System.out.println(score);
            locateFood();
        }
    }

    /**
     * TODO: 2/13/2021 intersection with body check (test needed)
     */
    private void locateFood() {
        boolean found = false;
        while (!found) {
            int count = 0;
            foodX = (int) (Math.random() * (GAME_SIZE / BLOCK_SIZE));
            foodY = (int) (Math.random() * (GAME_SIZE / BLOCK_SIZE));
            for (int i = 0; i < size; i++){
                if (foodX != x[i] && foodY != y[i]){ count++; }
            }
            if (count == size){ found = true; }
        }
    }

    // TODO: 2/13/2021 fix self eat bug
    private class TAdapter extends KeyAdapter {
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

    private void menu(){

    }

    private void endGame(){

    }

    private void difficulty(){

    }

    private void inGame(){
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.YELLOW);
        g.fillRect(foodX * BLOCK_SIZE, foodY * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);

        g.setColor(Color.RED);
        for (int i = 0; i < size; i++) {
            g.fillRect(x[i] * BLOCK_SIZE, y[i] * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
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
