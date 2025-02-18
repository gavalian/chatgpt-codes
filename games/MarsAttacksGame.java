import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Main class that creates the game window.
 */
public class MarsAttacksGame extends JFrame {

    public MarsAttacksGame() {
        add(new GamePanel());
        setTitle("Mars Attacks Arcade Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null); // Center the window.
        setResizable(false);
    }

    public static void main(String[] args) {
        // Launch the game on the Event Dispatch Thread.
        EventQueue.invokeLater(() -> {
            MarsAttacksGame game = new MarsAttacksGame();
            game.setVisible(true);
        });
    }
}

/**
 * The GamePanel class handles drawing, game logic, and user input.
 */
class GamePanel extends JPanel implements ActionListener, KeyListener {

    // Panel dimensions.
    private final int PANEL_WIDTH = 800;
    private final int PANEL_HEIGHT = 600;
    // Game loop timer delay (in milliseconds).
    private final int DELAY = 20; // ~50 FPS

    // The player ship.
    private Player player;
    // Lists to hold active bullets and aliens.
    private ArrayList<Bullet> bullets;
    private ArrayList<Alien> aliens;

    // Score and game state.
    private int score;
    private boolean gameOver;

    // Input flags.
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    // Timer to control the game loop.
    private javax.swing.Timer timer;
    // Variables to control alien spawning.
    private int alienSpawnCounter = 0;
    private int alienSpawnDelay = 50; // spawn an alien every 50 ticks (approximately every second)
    private Random random = new Random();

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        initGame();
    }

    /**
     * Initializes or resets the game state.
     */
    private void initGame() {
        // Create the player at the bottom center.
        player = new Player(PANEL_WIDTH / 2 - 20, PANEL_HEIGHT - 60, 40, 40);
        bullets = new ArrayList<>();
        aliens = new ArrayList<>();
        score = 0;
        gameOver = false;
        alienSpawnCounter = 0;

        // Start (or restart) the game timer.
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        timer = new javax.swing.Timer(DELAY, this);
        timer.start();
    }

    /**
     * Main drawing method.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * Draws the player, bullets, aliens, and score. Displays a Game Over screen if needed.
     */
    private void draw(Graphics g) {
        if (!gameOver) {
            // Draw the player.
            player.draw(g);

            // Draw all bullets.
            for (Bullet b : bullets) {
                b.draw(g);
            }

            // Draw all aliens.
            for (Alien a : aliens) {
                a.draw(g);
            }

            // Draw the score.
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.drawString("Score: " + score, 10, 20);
        } else {
            // Display Game Over message.
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String msg = "Game Over";
            FontMetrics fm = getFontMetrics(g.getFont());
            g.drawString(msg, (PANEL_WIDTH - fm.stringWidth(msg)) / 2, PANEL_HEIGHT / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            String restartMsg = "Press R to Restart";
            fm = getFontMetrics(g.getFont());
            g.drawString(restartMsg, (PANEL_WIDTH - fm.stringWidth(restartMsg)) / 2, PANEL_HEIGHT / 2 + 40);
        }
        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Called on every timer tick to update game state.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            updateGame();
        }
        repaint();
    }

    /**
     * Updates positions of the player, bullets, and aliens; spawns new aliens; and checks for collisions.
     */
    private void updateGame() {
        // Update player movement.
        if (leftPressed) {
            player.move(-player.getSpeed(), 0, PANEL_WIDTH);
        }
        if (rightPressed) {
            player.move(player.getSpeed(), 0, PANEL_WIDTH);
        }

        // Update bullets.
        ArrayList<Bullet> bulletsToRemove = new ArrayList<>();
        for (Bullet b : bullets) {
            b.update();
            if (b.getY() < 0) {
                bulletsToRemove.add(b);
            }
        }
        bullets.removeAll(bulletsToRemove);

        // Update aliens.
        for (Alien a : aliens) {
            a.update();
            // If an alien reaches the bottom, the game is over.
            if (a.getY() > PANEL_HEIGHT) {
                gameOver = true;
                timer.stop();
            }
        }

        // Spawn new aliens at fixed intervals.
        alienSpawnCounter++;
        if (alienSpawnCounter >= alienSpawnDelay) {
            spawnAlien();
            alienSpawnCounter = 0;
        }

        // Check for collisions between bullets and aliens.
        ArrayList<Bullet> removeBullets = new ArrayList<>();
        ArrayList<Alien> removeAliens = new ArrayList<>();
        for (Bullet b : bullets) {
            for (Alien a : aliens) {
                if (b.getBounds().intersects(a.getBounds())) {
                    removeBullets.add(b);
                    removeAliens.add(a);
                    score += 10;
                }
            }
        }
        bullets.removeAll(removeBullets);
        aliens.removeAll(removeAliens);

        // Check for collisions between aliens and the player.
        for (Alien a : aliens) {
            if (a.getBounds().intersects(player.getBounds())) {
                gameOver = true;
                timer.stop();
            }
        }
    }

    /**
     * Spawns a new alien at a random horizontal position above the top edge.
     */
    private void spawnAlien() {
        int alienWidth = 40;
        int alienHeight = 40;
        int x = random.nextInt(PANEL_WIDTH - alienWidth);
        int y = -alienHeight;
        aliens.add(new Alien(x, y, alienWidth, alienHeight));
    }

    // KeyListener methods:

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used.
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (!gameOver) {
            if (key == KeyEvent.VK_LEFT) {
                leftPressed = true;
            }
            if (key == KeyEvent.VK_RIGHT) {
                rightPressed = true;
            }
            if (key == KeyEvent.VK_SPACE) {
                // Shoot a bullet from the center-top of the player ship.
                int bulletWidth = 5;
                int bulletHeight = 10;
                int bx = player.getX() + player.getWidth() / 2 - bulletWidth / 2;
                int by = player.getY();
                bullets.add(new Bullet(bx, by, bulletWidth, bulletHeight));
            }
        } else {
            if (key == KeyEvent.VK_R) {
                // Restart the game.
                initGame();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (key == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }
}

/**
 * The Player class represents the user-controlled ship.
 */
class Player {
    private int x, y, width, height;
    private int speed = 5;

    public Player(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Moves the player horizontally while keeping within the panel bounds.
     */
    public void move(int dx, int dy, int panelWidth) {
        x += dx;
        if (x < 0) {
            x = 0;
        }
        if (x + width > panelWidth) {
            x = panelWidth - width;
        }
    }

    /**
     * Draws the player as a blue rectangle.
     */
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getSpeed() {
        return speed;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
}

/**
 * The Bullet class represents projectiles shot by the player.
 */
class Bullet {
    private int x, y, width, height;
    private int speed = 7;

    public Bullet(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Moves the bullet upward.
     */
    public void update() {
        y -= speed;
    }

    /**
     * Draws the bullet as a yellow rectangle.
     */
    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillRect(x, y, width, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getY() {
        return y;
    }
}

/**
 * The Alien class represents enemy aliens.
 */
class Alien {
    private int x, y, width, height;
    private int speed = 2;

    public Alien(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Moves the alien downward.
     */
    public void update() {
        y += speed;
    }

    /**
     * Draws the alien as a red oval.
     */
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(x, y, width, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getY() {
        return y;
    }
}
