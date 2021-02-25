package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;

public class Game extends Canvas implements Runnable {
    private boolean isRunning = false;
    private Thread thread;

    // You can set preferred screen size (width and height)
    private static final int GAME_SIZE = 640;

    // These constants shouldn't be changed
    private static final int WIDTH = GAME_SIZE + 14;
    private static final int HEIGHT = GAME_SIZE + 37;

    private final int rectangleWidth = GAME_SIZE / 2;
    private final int rectangleHeight = GAME_SIZE / 10;
    private final int rectangleLocationX = GAME_SIZE / 2 - GAME_SIZE / 4;
    private final int rectangleLocationY = GAME_SIZE / 10 + 10;
    private final int menuSize = HEIGHT / 10;
    private final int textSize = HEIGHT / 20;
    private Color playRectangleColor = Color.LIGHT_GRAY;
    private Color continueRectangleColor = Color.GRAY;
    private Color settingsRectangleColor = Color.LIGHT_GRAY;
    private Color aboutRectangleColor = Color.LIGHT_GRAY;
    private Color quitRectangleColor = Color.LIGHT_GRAY;

    // You can set preferred game block size (snake's body, food, etc.)
    private final int BLOCK_SIZE = 32;

    private double amountOfTicks = 5.0;

    // Snake coordinates
    private final int[] snakeX = new int[(GAME_SIZE / BLOCK_SIZE) * (GAME_SIZE / BLOCK_SIZE)];
    private final int[] snakeY = new int[(GAME_SIZE / BLOCK_SIZE) * (GAME_SIZE / BLOCK_SIZE)];

    // Starting snake's body size
    private int size;

    // Player's score
    private int score;
    private int updatesInfo;
    private int framesInfo;

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
    private Direction direction;
    private Direction nextDirection;

    private enum State {
        Menu,
        Game,
        Pause,
        Settings,
        About,
        End
    }
    private State state = State.Menu;

    // Game constructor
    public Game() {
        new Window(new Dimension(WIDTH, HEIGHT), "snake", this);

        init();

        initGame();

        start();
    }

    private void init() {
        addKeyListener(new KeyInput());
        addMouseListener(new MouseInput());
        addMouseMotionListener(new MouseInput());
    }

    private void initGame() {
        direction = Direction.Right;
        nextDirection = Direction.None;
        size = 2;
        score = 0;

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
        int updates = 0;
        int frames = 0;

        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / (1000000000 / amountOfTicks);
            lastTime = now;
            while (delta >= 1) {
                tick();
                updates++;
                delta--;
            }
            render();
            frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                updatesInfo = updates;
                framesInfo = frames;
                frames = 0;
                updates = 0;
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

    private synchronized void gameOver() {
        for (int i = 2; i < size; i++) {
            if (snakeX[0] == snakeX[i] && snakeY[0] == snakeY[i]) {
                initGame();
                state = State.Menu;
            }
        }
    }

    private synchronized void checkFood() {
        if (snakeX[0] == foodX && snakeY[0] == foodY) {
            size++;
            snakeX[size - 1] = snakeX[size - 2];
            snakeY[size - 1] = snakeY[size - 2];
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
            if (state == State.Game) {
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

                if (key == KeyEvent.VK_ESCAPE) {
                    state = State.Pause;
                    playRectangleColor = Color.GRAY;
                    continueRectangleColor = Color.LIGHT_GRAY;
                }
            }
        }
    }

    private class MouseInput extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            if (state == State.Menu || state == State.Pause) {
                if (x >= rectangleLocationX && x <= rectangleLocationX + rectangleWidth) {
                    if (y >= rectangleLocationY * 2 && y <= rectangleLocationY * 2 + rectangleHeight && state == State.Menu) {
                        state = State.Game;
                    }

                    if (y >= rectangleLocationY * 3 && y <= rectangleLocationY * 3 + rectangleHeight && state == State.Pause) {
                        state = State.Game;
                    }
                }

                if (state == State.Menu || state == State.Pause) {
                    if (x >= rectangleLocationX && x <= rectangleLocationX + rectangleWidth) {
                        if (y >= rectangleLocationY * 4 && y <= rectangleLocationY * 4 + rectangleHeight) {
                            state = State.Settings;
                        }

                        if (y >= rectangleLocationY * 5 && y <= rectangleLocationY * 5 + rectangleHeight) {
                            state = State.About;
                        }

                        if (y >= rectangleLocationY * 6 && y <= rectangleLocationY * 6 + rectangleHeight) {
                            System.exit(0);
                        }
                    }
                }
            } else if (state == State.Settings) {
                if (x >= rectangleLocationX && x <= rectangleLocationX + rectangleWidth) {
                    if (y >= rectangleLocationY * 2 && y <= rectangleLocationY * 2 + rectangleHeight) {
                        amountOfTicks++;
                    }
                    if (y >= rectangleLocationY * 5 && y <= rectangleLocationY * 5 + rectangleHeight) {
                        amountOfTicks--;
                    }
                    if (y >= rectangleLocationY * 6 && y <= rectangleLocationY * 6 + rectangleHeight) {
                        state = State.Menu;
                    }
                }
            } else if (state == State.About) {

            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (state == State.Menu || state == State.Pause) {
                int x = e.getX();
                int y = e.getY();

                if (state == State.Menu) {
                    playRectangleColor = Color.LIGHT_GRAY;
                    continueRectangleColor = Color.GRAY;
                } else if (state == State.Pause) {
                    playRectangleColor = Color.GRAY;
                    continueRectangleColor = Color.LIGHT_GRAY;
                }

                settingsRectangleColor = Color.LIGHT_GRAY;
                aboutRectangleColor = Color.LIGHT_GRAY;
                quitRectangleColor = Color.LIGHT_GRAY;

                if (x >= rectangleLocationX && x <= rectangleLocationX + rectangleWidth) {
                    if (y >= rectangleLocationY * 2 && y <= rectangleLocationY * 2 + rectangleHeight && state == State.Menu) {
                        playRectangleColor = Color.WHITE;
                    }

                    if (y >= rectangleLocationY * 3 && y <= rectangleLocationY * 3 + rectangleHeight && state == State.Pause) {
                        continueRectangleColor = Color.WHITE;
                    }

                    if (y >= rectangleLocationY * 4 && y <= rectangleLocationY * 4 + rectangleHeight) {
                        settingsRectangleColor = Color.WHITE;
                    }

                    if (y >= rectangleLocationY * 5 && y <= rectangleLocationY * 5 + rectangleHeight) {
                        aboutRectangleColor = Color.WHITE;
                    }

                    if (y >= rectangleLocationY * 6 && y <= rectangleLocationY * 6 + rectangleHeight) {
                        quitRectangleColor = Color.WHITE;
                    }
                }
            } else if (state == State.Settings) {

            } else if (state == State.About) {

            }
        }
    }

    private static class Window {
        public Window(Dimension size, String title, Game game) {
            JFrame frame = new JFrame(title);
            frame.setPreferredSize(size);
            frame.setMaximumSize(size);
            frame.setMinimumSize(size);

            frame.add(game);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }

    private void tick() {
        if (state == State.Game) {
            move();
            checkFood();
            gameOver();
        }
    }

    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (state == State.Menu) {
            menuRender(g);
        } else if (state == State.Game) {
            gameRender(g);

            Font fnt1 = new Font("arial", Font.BOLD, textSize / 2);
            g.setFont(fnt1);

            g.setColor(Color.WHITE);

            g.drawString("SCORE: " + score, getWidth() - 100, 20);

            g.setColor(Color.DARK_GRAY);

            g.drawString("TICK RATE: " + updatesInfo + " FPS: " + framesInfo, getWidth() - 180, getHeight() - 10);

        } else if (state == State.Pause) {
            menuRender(g);
        } else if (state == State.Settings) {
            settingsRender(g);
        } else if (state == State.About) {

        }

        g.dispose();
        bs.show();
    }

    private void menuRender(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        Font fnt0 = new Font("arial", Font.BOLD, menuSize);

        g.setColor(Color.WHITE);
        Rectangle menu = new Rectangle(rectangleLocationX, 0, rectangleWidth, rectangleHeight * 3);
        drawCenteredString(g, "MENU", menu, fnt0);


        Font fnt1 = new Font("arial", Font.BOLD, textSize);

        g.setColor(playRectangleColor);
        Rectangle playButton = new Rectangle(rectangleLocationX, rectangleLocationY * 2, rectangleWidth, rectangleHeight);
        drawCenteredString(g, "PLAY", playButton, fnt1);
        g2d.draw(playButton);

        g.setColor(continueRectangleColor);
        Rectangle continueButton = new Rectangle(rectangleLocationX, rectangleLocationY * 3, rectangleWidth, rectangleHeight);
        drawCenteredString(g, "CONTINUE", continueButton, fnt1);
        g2d.draw(continueButton);

        g.setColor(settingsRectangleColor);
        Rectangle settingsButton = new Rectangle(rectangleLocationX, rectangleLocationY * 4, rectangleWidth, rectangleHeight);
        drawCenteredString(g, "SETTINGS", settingsButton, fnt1);
        g2d.draw(settingsButton);


        g.setColor(aboutRectangleColor);
        Rectangle aboutButton = new Rectangle(rectangleLocationX, rectangleLocationY * 5, rectangleWidth, rectangleHeight);
        drawCenteredString(g, "ABOUT", aboutButton, fnt1);
        g2d.draw(aboutButton);

        g.setColor(quitRectangleColor);
        Rectangle quitButton = new Rectangle(rectangleLocationX, rectangleLocationY * 6, rectangleWidth, rectangleHeight);
        drawCenteredString(g, "QUIT", quitButton, fnt1);
        g2d.draw(quitButton);
    }

    private void settingsRender(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        Font fnt0 = new Font("arial", Font.BOLD, menuSize);

        g.setColor(Color.WHITE);
        Rectangle settings = new Rectangle(rectangleLocationX, 0, rectangleWidth, rectangleHeight * 3);
        drawCenteredString(g, "Settings", settings, fnt0);

        Rectangle speed = new Rectangle(rectangleLocationX, rectangleLocationY * 3, rectangleWidth, rectangleHeight * 2);
        drawCenteredString(g, String.valueOf((int) amountOfTicks), speed, fnt0);


        Font fnt1 = new Font("arial", Font.BOLD, textSize);

        g.setColor(playRectangleColor);
        Rectangle speedUp = new Rectangle(rectangleLocationX, rectangleLocationY * 2, rectangleWidth, rectangleHeight);
        drawCenteredString(g, "SPEED UP", speedUp, fnt1);
        g2d.draw(speedUp);


        g.setColor(aboutRectangleColor);
        Rectangle speedDown = new Rectangle(rectangleLocationX, rectangleLocationY * 5, rectangleWidth, rectangleHeight);
        drawCenteredString(g, "SPEED DOWN", speedDown, fnt1);
        g2d.draw(speedDown);

        g.setColor(quitRectangleColor);
        Rectangle quitButton = new Rectangle(rectangleLocationX, rectangleLocationY * 6, rectangleWidth, rectangleHeight);
        drawCenteredString(g, "BACK", quitButton, fnt1);
        g2d.draw(quitButton);
    }

    private void gameRender(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(foodX * BLOCK_SIZE, foodY * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);

        g.setColor(Color.RED);
        for (int i = 0; i < size; i++) {
            g.fillRect(snakeX[i] * BLOCK_SIZE, snakeY[i] * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
        }
    }

    // https://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java
    private void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        FontMetrics metrics = g.getFontMetrics(font);

        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();

        g.setFont(font);

        g.drawString(text, x, y);
    }

    public static void main(String[] args) {
        new Game();
    }
}
