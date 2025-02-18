import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class DinoGame extends JFrame {
    public DinoGame() {
        setTitle("Dino Game - Jump Over the Cacti!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new GamePanel());
        pack();
        setLocationRelativeTo(null); // Center the window.
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DinoGame());
    }
}

/**
 * The GamePanel class handles game updates, drawing, and input.
 */
class GamePanel extends JPanel implements ActionListener, KeyListener {

    private final int PANEL_WIDTH = 800;
    private final int PANEL_HEIGHT = 400;
    private final int GROUND_Y = 300; // Y-coordinate of the ground.
    private javax.swing.Timer timer;
    private final int DELAY = 20;  // milliseconds (about 50 FPS)
    
    private Dinosaur dino;
    private ArrayList<Cactus> cacti;
    private boolean gameOver;
    
    // Variables for cactus spawning.
    private int spawnTimer;
    private Random random;

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        initGame();
    }
    
    // Initialize or reset the game.
    private void initGame() {
        dino = new Dinosaur(50, GROUND_Y - 50, 50, 50, GROUND_Y);
        cacti = new ArrayList<>();
        gameOver = false;
        spawnTimer = 0;
        random = new Random();
        
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        timer = new javax.swing.Timer(DELAY, this);
        timer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the ground.
        g.setColor(Color.GREEN.darker());
        g.fillRect(0, GROUND_Y, PANEL_WIDTH, PANEL_HEIGHT - GROUND_Y);
        
        // Draw the dinosaur.
        dino.draw(g);
        
        // Draw each cactus.
        for (Cactus cactus : cacti) {
            cactus.draw(g);
        }
        
        // If the game is over, display a message.
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String msg = "Game Over! Press R to Restart.";
            FontMetrics fm = g.getFontMetrics();
            int msgWidth = fm.stringWidth(msg);
            g.drawString(msg, (PANEL_WIDTH - msgWidth) / 2, PANEL_HEIGHT / 2);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            // Update the dinosaur.
            dino.update();
            
            // Update cacti and check for collisions.
            Iterator<Cactus> iter = cacti.iterator();
            while (iter.hasNext()) {
                Cactus cactus = iter.next();
                cactus.update();
                // Remove the cactus if it moves off the left edge.
                if (cactus.getX() + cactus.getWidth() < 0) {
                    iter.remove();
                }
                // Check for collision between the dinosaur and a cactus.
                if (cactus.getBounds().intersects(dino.getBounds())) {
                    gameOver = true;
                    timer.stop();
                }
            }
            
            // Spawn new cactus at random intervals.
            spawnTimer += DELAY;
            // Spawn roughly every 1.5 to 2.5 seconds.
            if (spawnTimer >= 1500 + random.nextInt(1000)) {
                int cactusWidth = 20 + random.nextInt(10);   // Width between 20 and 30.
                int cactusHeight = 40 + random.nextInt(20);    // Height between 40 and 60.
                // Position the cactus at the right edge, on the ground.
                Cactus cactus = new Cactus(PANEL_WIDTH, GROUND_Y - cactusHeight, cactusWidth, cactusHeight, 5);
                cacti.add(cactus);
                spawnTimer = 0;
            }
        }
        repaint();
    }
    
    // Handle key presses.
    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                dino.jump();
            }
        } else {
            if (e.getKeyCode() == KeyEvent.VK_R) {
                initGame();
            }
        }
    }
    
    @Override public void keyReleased(KeyEvent e) { }
    @Override public void keyTyped(KeyEvent e) { }
}

/**
 * The Dinosaur class represents the player’s character.
 */
class Dinosaur {
    private int x, y, width, height;
    private int groundY;
    private double velocityY;
    private final double gravity = 0.6;
    private final double jumpStrength = -12;
    
    public Dinosaur(int x, int y, int width, int height, int groundY) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.groundY = groundY;
        velocityY = 0;
    }
    
    // Update the dinosaur’s vertical position.
    public void update() {
        y += velocityY;
        velocityY += gravity;
        
        // If the dinosaur lands, reset its position.
        if (y >= groundY - height) {
            y = groundY - height;
            velocityY = 0;
        }
    }
    
    // Initiate a jump if the dinosaur is on the ground.
    public void jump() {
        if (y >= groundY - height) {
            velocityY = jumpStrength;
        }
    }
    
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

/**
 * The Cactus class represents an obstacle.
 */
class Cactus {
    private int x, y, width, height;
    private int speed;
    
    public Cactus(int x, int y, int width, int height, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }
    
    // Move the cactus leftward.
    public void update() {
        x -= speed;
    }
    
    public void draw(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, width, height);
    }
    
    public int getX() {
        return x;
    }
    
    public int getWidth() {
        return width;
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

