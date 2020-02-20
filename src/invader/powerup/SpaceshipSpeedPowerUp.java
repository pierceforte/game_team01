package invader.powerup;

import invader.entity.Spaceship;

/**
 * @author Pierce Forte
 * @author Jeff Kim
 * Specific Speed power up class that inherits the abstract class Powerup
 * Increases the speed of the spaceship on the screen
 */

public class SpaceshipSpeedPowerUp extends PowerUp {
    public static final String IMG_NAME = "fastpower.gif";
    public static final int INCREASED_SPEED = 25;
    public static final int TIME_ACTIVE = 8;

    /**
     * Constructor
     * @param xPos: x position of the speed powerup
     * @param yPos: y position of the speed power up
     * @param id: id later used for testing
     */
    public SpaceshipSpeedPowerUp(double xPos, double yPos, String id) {
        super(xPos, yPos, IMG_NAME, id);
        setTimeActive(TIME_ACTIVE);
    }

    @Override
    public void activate(double gameTimer, Spaceship spaceship) {
        spaceship.setXSpeedOnKeyPress(INCREASED_SPEED);
        setTimeWhenActivated(gameTimer);
    }

    @Override
    public void deactivate(double gameTimer, Spaceship spaceship) {
        spaceship.setXSpeedOnKeyPress(Spaceship.DEFAULT_X_SPEED_ON_KEY_PRESS);
    }

    @Override
    public void reapplyPowerUp(double gameTimer, Spaceship spaceship) {
        spaceship.setXSpeedOnKeyPress(INCREASED_SPEED);
    }

}
