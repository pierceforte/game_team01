package invader.powerup;

import invader.entity.Spaceship;

public class BurstFirePowerUp extends PowerUp {

    public static final String IMG_NAME = "burstpower.gif";
    public static final int TIME_ACTIVE = 6;

    /**
     * Constructor
     */
    public BurstFirePowerUp(double xPos, double yPos, String id) {
        super(xPos, yPos, IMG_NAME, id);
        setTimeActive(TIME_ACTIVE);
    }

    @Override
    public void activate(double gameTimer, Spaceship spaceship) {
        spaceship.setBurstFirePowerUp(true);
        setTimeWhenActivated(gameTimer);
    }

    @Override
    public void deactivate(double gameTimer, Spaceship spaceship) {
        spaceship.setBurstFirePowerUp(false);
    }

    @Override
    public void reapplyPowerUp(double gameTimer, Spaceship spaceship) {
        spaceship.setBurstFirePowerUp(true);
    }
}