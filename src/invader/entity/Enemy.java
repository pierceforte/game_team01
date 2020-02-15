package invader.entity;

import invader.powerup.PowerUp;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Jeff Kim
 * started 2/4/20
 */
public class Enemy extends Entity {
    public static final int HEIGHT = 30;
    public static final int WIDTH = 30;
    public static final String ENEMY_IMG_PREFIX = "enemy";
    public static final String ENEMY_IMG_EXTENSION = ".png";
    public static final int TIME_BETWEEN_SHOTS = 50;
    public static final int EARLIEST_START_FIRING_TIME = 1;
    public static final int LATEST_START_FIRING_TIME = 40;

    private PowerUp powerUp;
    private boolean hasPowerUp = false;

    public Enemy(double xPos, double yPos, double xSpeed, double ySpeed, int lives, int idNumber, PowerUp powerUp) {
        super(xPos, yPos, xSpeed, ySpeed, WIDTH, HEIGHT, ENEMY_IMG_PREFIX + lives + ENEMY_IMG_EXTENSION);
        setLives(lives);
        this.setId("enemy" + idNumber);
        addToStartShootingTime(ThreadLocalRandom.current().nextInt(EARLIEST_START_FIRING_TIME, LATEST_START_FIRING_TIME));
        if (powerUp != null) {
            this.powerUp = powerUp;
            hasPowerUp = true;
        }
    }

    public PowerUp getPowerUp() {
        return powerUp;
    }

    public boolean hasPowerUp() {
        return hasPowerUp;
    }

}
