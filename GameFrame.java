import javax.swing.JFrame;

public class GameFrame extends JFrame{
	
	GameFrame(){
		
		
		this.add(new GamePanel());
		this.setTitle("SNAKE GAME");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.pack();
		this.setVisible(true);
		this.setLocationRelativeTo(null);
	}
	
}
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.Point;

public class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNIT = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int DELAY = 90;
    final int x[] = new int[GAME_UNIT];
    final int y[] = new int[GAME_UNIT];
    int appleEaten;
    int appleX;
    int appleY;
    int bodyparts = 6;
    char direction = 'R';
    boolean running = false;
    Random random;
    Timer timer;
    int level = 1;
    ArrayList<Point> obstacles = new ArrayList<>(); // Stores obstacle positions

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }

    public void startGame() {
        // Reset game state
        obstacles.clear();
        level = 1;
        for (int i = 0; i < bodyparts; i++) {
            x[i] = 100 - i * UNIT_SIZE;
            y[i] = 100;
        }
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
        this.requestFocusInWindow();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
        if (!running) {
            gameOver(g);
        }
    }

    public void draw(Graphics g) {

        // Draw apple
        g.setColor(Color.red);
        g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

        // Draw snake
        for (int i = 0; i < bodyparts; i++) {
            if (i == 0) {
                g.setColor(Color.green);
            } else {
                g.setColor(new Color(48, 100, 0));
            }
            g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
        }

        // Draw obstacles in Level 2+
        if (level >= 2) {
            g.setColor(Color.blue);
            for (Point obstacle : obstacles) {
                g.fillRect(obstacle.x, obstacle.y, UNIT_SIZE, UNIT_SIZE);
            }
        }

        drawScore(g);
        drawLevel(g);
    }

    public void newApple() {
        appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
    }

    public void addObstacle() {
        int obstacleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        int obstacleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
        obstacles.add(new Point(obstacleX, obstacleY));
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyparts++;
            appleEaten += 100;

            // Increase level every 1000 points
            if (appleEaten % 1000 == 0) {
                level++;
                increaseDifficulty();
            }

            // Add obstacle in Level 2+
            if (level >= 2) {
                addObstacle();
            }

            newApple();
        }
    }

    public void increaseDifficulty() {
        int newDelay = DELAY - (level * 10);
        if (newDelay < 20) newDelay = 20;
        timer.setDelay(newDelay);
    }

    public void checkCollisions() {
        // Collision with body
        for (int i = bodyparts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
            }
        }

        // Collision with borders
        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        // Collision with obstacles (Level 2+)
        if (level >= 2) {
            for (Point obstacle : obstacles) {
                if (x[0] == obstacle.x && y[0] == obstacle.y) {
                    running = false;
                    break;
                }
            }
        }

        if (!running) {
            timer.stop();
        }
    }

    public void move() {
        for (int i = bodyparts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        switch (direction) {
            case 'U' -> y[0] -= UNIT_SIZE;
            case 'D' -> y[0] += UNIT_SIZE;
            case 'L' -> x[0] -= UNIT_SIZE;
            case 'R' -> x[0] += UNIT_SIZE;
        }
    }

    public void gameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);

        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        g.drawString("Final Score: " + appleEaten, (SCREEN_WIDTH - metrics.stringWidth("Final Score: " + appleEaten)) / 2, SCREEN_HEIGHT / 2 + 50);
        g.drawString("Level: " + level, (SCREEN_WIDTH - metrics.stringWidth("Level: " + level)) / 2, SCREEN_HEIGHT / 2 + 100);
    }

    public void drawScore(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Score: " + appleEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: " + appleEaten)) / 2, metrics.getHeight());
    }

    public void drawLevel(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        g.drawString("Level: " + level, 20, 40);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                    if (direction != 'R') direction = 'L';
                }
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                    if (direction != 'L') direction = 'R';
                }
                case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                    if (direction != 'D') direction = 'U';
                }
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                    if (direction != 'U') direction = 'D';
                }
            }
        }
    }
} 
