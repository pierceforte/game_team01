package invader.projectile;

public class Missile extends Projectile{// extends Laser {
    public static final double WIDTH = 10;
    public static final double HEIGHT = 20;
    public static final String BOMB_IMG_NAME = "missile.png";
    public static final String TYPE = "Missile";
    public static final int DAMAGE = 3;

    /**
     * Constructor
     * @param xPos
     * @param yPos
     * @param isEvil
     * @param rotation
     * @param idNumber
     */
    public Missile(double xPos, double yPos, boolean isEvil, double rotation, int idNumber) {
        super(xPos, yPos, DEFAULT_X_SPEED, DEFAULT_Y_SPEED, WIDTH, HEIGHT, isEvil, BOMB_IMG_NAME, rotation, idNumber, TYPE);
        setDamage(DAMAGE);
    }
}
