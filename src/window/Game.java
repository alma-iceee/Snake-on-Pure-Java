package window;

import java.awt.*;
import java.awt.image.BufferStrategy;

public class Game extends Canvas implements Runnable {
    private boolean isRunning = false;
    private Thread thread;

    public Game() {
        new Window(new Dimension(600,600), "snake", this);

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
        g.fillRect(0, 0, 600, 600);

        g.dispose();
        bs.show();
    }

    public static void main(String[] args) {
        new Game();
    }
}
