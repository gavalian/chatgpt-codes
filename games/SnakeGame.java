import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;

public class SnakeGame extends JFrame {

    public SnakeGame() {
        // Add the game panel to the JFrame
        add(new GamePanel());
        setResizable(false);
        pack(); // Sizes the frame so that all its contents are at or above their preferred sizes.
        setTitle("Snake Game");
        setLocationRelativeTo(null); // Centers the window on the screen.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        // Launch the GUI on the Event Dispatch Thread
        EventQueue.invokeLater(() -> {
            JFrame ex = new SnakeGame();
            ex.setVisible(true);
        });
    }
}

/**
 * The GamePanel class contains the game logic, drawing, and keyboard controls.
 */
class GamePanel extends JPanel implements ActionListener {

    // Constants for the game board
    private final int SCREEN_WIDTH = 600;
    private final int SCREEN_HEIGHT = 600;
    private final int UNIT_SIZE = 25;  // Size of the grid unit (and snake part)
    private final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    private final int DELAY = 75;      // Timer delay (in milliseconds)

    // Arrays to hold the x and y coordinates of all snake segments
    private final int x[] = new int[GAME_UNITS];
    private final int y[] = new int[GAME_UNITS];

    // Initial snake properties
    private int bodyParts = 6;
    private int applesEaten;
    private int appleX;
    private int appleY;
    private char direction = 'R'; // R = right, L = left, U = up, D = down
    private boolean running = false;
    private javax.swing.Timer timer;
    private Random random;

    public GamePanel() {
        random = new Random();
        // Set the size and background of the game panel
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(new MyKeyAdapter());
        startGame();
    }

    /** Starts the game by creating the first apple and starting the timer. */
    public void startGame() {
        newApple();
        running = true;
        timer = new javax.swing.Timer(DELAY, this);
        timer.start();
    }

    /** Paints the game components. */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /** Draws the grid, apple, snake, and score. */
    public void draw(Graphics g) {
        if (running) {
            // Optionally, draw a grid (for visual aid)
            for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
                g.setColor(Color.darkGray);
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
                g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
            }

            // Draw the apple
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            // Draw the snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    // Draw the head in a brighter color
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    // Draw the body with a slightly different green
                    g.setColor(new Color(45, 180, 0));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }

            // Draw the score at the top center of the screen
            g.setColor(Color.red);
            g.setFont(new Font("Ink Free", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten,
                         (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2,
                         g.getFont().getSize());
        } else {
            gameOver(g);
        }
    }

    /** Randomly positions a new apple on the game board. */
    public void newApple() {
        appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
    }

    /** Moves the snake by shifting the body parts and moving the head in the current direction. */
    public void move() {
        // Shift the coordinates of each body part to the position of the previous one
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        // Update the head's coordinates based on the current direction
        switch (direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }
    }

    /** Checks if the snake's head has reached the apple. */
    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    /** Checks for collisions with the snake's body or the screen boundaries. */
    public void checkCollisions() {
        // Check if the head collides with the body
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }

        // Check if the head touches the left border
        if (x[0] < 0) {
            running = false;
        }
        // Check if the head touches the right border
        if (x[0] >= SCREEN_WIDTH) {
            running = false;
        }
        // Check if the head touches the top border
        if (y[0] < 0) {
            running = false;
        }
        // Check if the head touches the bottom border
        if (y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        // Stop the timer if the game is no longer running
        if (!running) {
            timer.stop();
        }
    }

    /** Displays the "Game Over" screen along with the final score. */
    public void gameOver(Graphics g) {
        // Display the score
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten,
                     (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2,
                     g.getFont().getSize());

        // Display "Game Over" text
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over",
                     (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2,
                     SCREEN_HEIGHT / 2);
    }

    /** The game loop: called on each timer tick. */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    /** KeyAdapter to listen for arrow key inputs to change the snake's direction. */
    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;
            }
        }
    }
}
