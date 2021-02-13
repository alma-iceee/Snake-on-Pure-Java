package main;

import java.awt.*;
import java.awt.image.BufferStrategy;

public class Game extends Canvas implements Runnable {
    private boolean isRunning = false;
    private Thread thread;

    private final int GAME_SIZE = 640;
    private final int WIDTH = GAME_SIZE + 14;
    private final int HEIGHT = GAME_SIZE + 37;
    private final int BLOCK_SIZE = 32;

    private int[] x;
    private int[] y;
    private int size;

    private int foodX;
    private int foodY;

    private enum Direction{
        right,
        left,
        up,
        down
    }

    private Direction direction;

    public Game() {
        new Window(new Dimension(WIDTH, HEIGHT), "snake", this);

        init();

        start();
    }

    private void init() {

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
        double amountOfTicks = 10.0;
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

    public void endGame(){
        for (int i = 4; i < size; i++){
            if (x[0] == x[i] && y[0] == y[i])
                stop();
        }
    }

    public void move(){
        if (direction == Direction.right){
            x[0]++;
            x[0] %= 20;
        }
        if (direction == Direction.left){
            x[0]--;
            if (x[0] < 0)
                x[0] = 19;
        }
        if (direction == Direction.up){
            y[0]--;
            if (y[0] < 0)
                x[0] = 19;
        }
        if (direction == Direction.down){
            y[0]++;
            y[0] %= 20;
        }
        for (int i = size-1; i > 0; i--){
            x[i] = x[i-1];
            y[i] = y[i-1];
        }
    }


    public void eat(){
        if (x[0] == foodX && y[0] == foodY){
            size++;
        }
    }

    private void tick() {

    }

    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GAME_SIZE, GAME_SIZE);

        g.dispose();
        bs.show();
    }

    public static void main(String[] args) {
        new Game();
    }
}
