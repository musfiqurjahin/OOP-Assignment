import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {

    private final int WIDTH = 600;
    private final int HEIGHT = 600;
    private final int UNIT = 20;
    private final int GAME_UNITS = (WIDTH * HEIGHT) / (UNIT * UNIT);
    private final int DELAY = 120;

    private final int x[] = new int[GAME_UNITS];
    private final int y[] = new int[GAME_UNITS];

    private int bodyParts = 6;
    private int foodX, foodY;
    private int score = 0;

    private char direction = 'R';  // U D L R
    private boolean running = false;
    private Timer timer;
    private Random random;

    SnakeGame() {
        random = new Random();
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(this);
        startGame();
    }

    public void startGame() {
        newFood();
        running = true;
        score = 0;
        bodyParts = 6;
        direction = 'R';
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            // Food
            g.setColor(Color.red);
            g.fillOval(foodX, foodY, UNIT, UNIT);

            // Snake Body
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                } else {
                    g.setColor(new Color(45, 180, 0));
                }
                g.fillRect(x[i], y[i], UNIT, UNIT);
            }

            // Score
            g.setColor(Color.white);
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            g.drawString("Score: " + score, 10, 20);

        } else {
            gameOver(g);
        }
    }

    public void newFood() {
        foodX = random.nextInt((WIDTH / UNIT)) * UNIT;
        foodY = random.nextInt((HEIGHT / UNIT)) * UNIT;
    }

    public void move() {

        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U' -> y[0] -= UNIT;
            case 'D' -> y[0] += UNIT;
            case 'L' -> x[0] -= UNIT;
            case 'R' -> x[0] += UNIT;
        }
    }

    public void checkFood() {
        if (x[0] == foodX && y[0] == foodY) {
            bodyParts++;
            score++;
            newFood();
        }
    }

    public void checkHit() {
        // Hit own body
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
            }
        }

        // Hit wall
        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
        }
    }

    public void gameOver(Graphics g) {
        // Score
        g.setColor(Color.white);
        g.setFont(new Font("SansSerif", Font.BOLD, 30));
        FontMetrics fm = getFontMetrics(g.getFont());
        g.drawString("Score: " + score, (WIDTH - fm.stringWidth("Score: " + score)) / 2, HEIGHT / 3);

        // Game Over
        g.setColor(Color.red);
        g.setFont(new Font("SansSerif", Font.BOLD, 40));
        FontMetrics fm2 = getFontMetrics(g.getFont());
        g.drawString("GAME OVER", (WIDTH - fm2.stringWidth("GAME OVER")) / 2, HEIGHT / 2);

        // Options
        int option = JOptionPane.showOptionDialog(
                this,
                "Your Score: " + score + "\n\nPlay Again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Play Again", "Exit"},
                "Play Again"
        );

        if (option == 0) {
            resetGame();
        } else {
            System.exit(0);
        }
    }

    private void resetGame() {
        for (int i = 0; i < bodyParts; i++) {
            x[i] = 0;
            y[i] = 0;
        }
        startGame();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkFood();
            checkHit();
        }
        repaint();
    }

    // Key Controls
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> {
                if (direction != 'R') direction = 'L';
            }
            case KeyEvent.VK_RIGHT -> {
                if (direction != 'L') direction = 'R';
            }
            case KeyEvent.VK_UP -> {
                if (direction != 'D') direction = 'U';
            }
            case KeyEvent.VK_DOWN -> {
                if (direction != 'U') direction = 'D';
            }
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    // Main Window
    public static void main(String[] args) {

        JFrame frame = new JFrame("Snake Game - Java");
        SnakeGame game = new SnakeGame();

        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
