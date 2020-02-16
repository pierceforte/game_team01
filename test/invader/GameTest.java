package invader;

import invader.entity.Boss;
import invader.entity.Enemy;
import invader.entity.Spaceship;
import invader.level.EnemyLevel;
import invader.level.Level;
import invader.powerup.PowerUp;
import invader.powerup.SpaceshipSpeedPowerUp;
import invader.projectile.Laser;
import invader.projectile.Projectile;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest extends DukeApplicationTest {
    private final String LEFTMOST_BOTTOM_ENEMY = "#enemy27";
    private final String ENEMY_ABOVE_SPACESHIP = "#enemy31";
    private final Game myGame = new Game();

    private Scene myScene;
    private Spaceship mySpaceship;
    private List<List<Enemy>> myEnemies = new ArrayList<>();
    private Enemy myEnemy27;
    private Enemy myEnemy31;
    private Projectile mySpaceshipProjectile;
    private Level myLevel;

    @Override
    public void start (Stage stage) {
        // create game's scene with all shapes in their initial positions and show it
        myScene = myGame.setupScene(Game.GAME_WIDTH, Game.GAME_HEIGHT, Game.BACKGROUND);
        stage.setScene(myScene);
        stage.show();
        // advance past start menu
        press(myScene, KeyCode.SPACE);

        Platform.runLater(() -> {
            myLevel = myGame.getCurLevel();
            // find individual items within game by ID (must have been set in your code using setID())
            mySpaceship = lookup("#spaceship").query();
            for (int row = 0; row < 4; row++) {
                myEnemies.add(new ArrayList<>());
                for (int col = 0; col < EnemyLevel.ENEMIES_PER_ROW; col++) {
                    myEnemies.get(row).add(lookup("#enemy" + (col + row*EnemyLevel.ENEMIES_PER_ROW)).query());
                }
            }
            // leftmost enemy in bottom row is enemy27, such that when "D" is pressed it will be destroyed
            myEnemy27 = lookup(LEFTMOST_BOTTOM_ENEMY).query();
            // when a projectile is fired from the spaceship's default position, it will hit enemy31
            myEnemy31 = lookup(ENEMY_ABOVE_SPACESHIP).query();
            // fire laser from spaceship
            press(myScene, KeyCode.SPACE);
            // need to wait for scene to update after key press in application thread
            Platform.runLater(() -> mySpaceshipProjectile = lookup("#spaceshipLaserProjectile0").query());
        });
    }

    @Test
    public void testSpaceshipSpeedUpPowerUpActivation() {
        // press cheat key to drop powerUp
        press(myScene, KeyCode.F);
        // we say cheatPowerUp0 because it is the 0th power up added to the game with the cheat key "A"
        PowerUp myPowerUp = lookup("#cheatPowerUp0").query();
        // position the powerUp one step prior to hitting spaceship
        myPowerUp.setY(mySpaceship.getY() - 10);
        // set powerUp such that it will only be in effect for a sing›le step
        myPowerUp.setTimeActive(Game.SECOND_DELAY);
        // assert that powerUp is in scene and spaceship speed on key press is at the default
        assertTrue(isNodeInMyScene(myPowerUp));
        assertEquals(Spaceship.DEFAULT_X_SPEED_ON_KEY_PRESS, mySpaceship.getXSpeedOnKeyPress());
        step();
        // assert that powerUp has been removed from scene, spaceship speed on key press is
        // now set to increased speed, and powerUp is active
        assertFalse(isNodeInMyScene(myPowerUp));
        assertEquals(SpaceshipSpeedPowerUp.INCREASED_SPEED, mySpaceship.getXSpeedOnKeyPress());
        assertTrue(myPowerUp.isActive());
        // step to deactivate powerUp (since it is only set to last for one step)
        step();
        //  assert that spaceship speed on key press is set to default and powerUp is no longer active
        assertEquals(Spaceship.DEFAULT_X_SPEED_ON_KEY_PRESS, mySpaceship.getXSpeedOnKeyPress());
        assertFalse(myPowerUp.isActive());
    }

    @Test
    public void testDestroyFirstEnemyCheatKey() {
        // first enemy is defined as leftmost enemy in the bottom row. this enemy's id is enemy27

        // check if enemy is on scene before key press
        assertTrue(isNodeInMyScene(myEnemy27));
        // press D, which is the cheat code to destroy first enemy
        press(myScene, KeyCode.D);
        // check if enemy27 has been destroyed
        assertFalse(isNodeInMyScene(myEnemy27));
    }

    @Test
    public void testRandomPowerUpCheatKey() {
        // assert that an exception is thrown when querying power up before it is created;
        // we say cheatPowerUp0 because it is the 0th power up added to the game with the cheat key "A"
        assertThrows(org.testfx.service.query.EmptyNodeQueryException.class, () -> lookup("#cheatPowerUp0").query());
        // press cheat key to drop powerUp
        press(myScene, KeyCode.A);
        // assert that powerUp has been created and added to scene
        assertTrue(isNodeInMyScene(lookup("#cheatPowerUp0").query()));
    }

    @Test
    public void testJumpToLevelCheatKeys() {
        // the first expected level is level 1
        int expectedLevelNumber = 1;
        for (KeyCode code : KeyHandler.KEY_CODES_1_THROUGH_9) {
            // press cheat key to jump to level
            press(myScene, code);
            // need to reassign our level to the new level created in game
            myLevel = myGame.getCurLevel();
            // assert that new level is correct
            assertEquals(expectedLevelNumber, myLevel.getLevelNumber());
            // if expected level is not max level, increment for next test
            if (expectedLevelNumber < Game.MAX_LEVEL) expectedLevelNumber++;
        }
    }

    @Test
    public void testSkipLevelCheatKey() {
        // assert that level is level 1 at start
        assertEquals(1, myLevel.getLevelNumber());
        // press cheat key to skip to next level
        press(myScene, KeyCode.S);
        // need to reassign our level to the new level created in game
        myLevel = myGame.getCurLevel();
        // assert that level is now level 2
        assertEquals(2, myLevel.getLevelNumber());
    }

    @Test
    public void testAddLifeCheatKey() {
        // assert that spaceship has 3 (default) lives
        assertEquals(3, mySpaceship.getLives());
        // press cheat key to add life
        press(myScene, KeyCode.L);
        // assert that spaceship has gained 1 life
        assertEquals(4, mySpaceship.getLives());
    }

    @Test
    public void testSpaceshipLifeLoss() {
        // set enemy's start shooting time to 0 so it shoots immediately
        myEnemy31.setStartShootingTime(0);
        // step to initiate laser fire from enemy31
        step();
        // since all other enemies cannot fire until 1 second after game begins,
        // we know enemy31's laser is the 0th laser and can query it as such
        Projectile myEnemy31Projectile = lookup("#evilLaserProjectile0").query();
        // position the laser one step prior to hitting spaceship
        myEnemy31Projectile.setY(mySpaceship.getY() - 6*Laser.Y_SPEED*Game.SECOND_DELAY);
        // assert that laser is in scene and spaceship has 3 (default) lives before collision
        assertTrue(isNodeInMyScene(myEnemy31Projectile));
        assertEquals(3, mySpaceship.getLives());
        step();
        // assert that laser has been removed from scene and spaceship has 2 lives after collision
        assertFalse(isNodeInMyScene(myEnemy31Projectile));
        assertEquals(2, mySpaceship.getLives());
    }

    @Test
    public void testEnemiesBounceOffRightWall() {
        // check collision off right wall
        testEnemiesReverseXDirection(EnemyLevel.ENEMIES_PER_ROW-1,
                Game.GAME_WIDTH - Enemy.WIDTH - Game.SECOND_DELAY, true);
    }

    @Test
    public void testEnemiesBounceOffLeftWall() {
        // check collision off left wall
        testEnemiesReverseXDirection(0, Game.SECOND_DELAY,
                false);
    }

    @Test
    public void testLaserDisappearsIfOutOfBounds() {
        // check if laser is on scene before being out of bounds
        assertTrue(isNodeInMyScene(mySpaceshipProjectile));
        // position the laser one step prior to being out of bounds
        mySpaceshipProjectile.setY(Game.GAME_HEIGHT - 20 + Laser.Y_SPEED*Game.SECOND_DELAY);
        // step so laser is out of bounds
        step();
        // check if the laser has been removed from scene upon being out of bounds
        assertFalse(isNodeInMyScene(mySpaceshipProjectile));
    }

    @Test
    public void testLaserCollisionWithEnemy() {
        // check if laser and enemy are on scene before collision
        assertTrue(isNodeInMyScene(myEnemy31));
        assertTrue(isNodeInMyScene(mySpaceshipProjectile));
        // position the laser one step prior to hitting enemy31
        mySpaceshipProjectile.setX(myEnemy31.getX());
        mySpaceshipProjectile.setY(myEnemy31.getY() + 9.5*Laser.Y_SPEED*Game.SECOND_DELAY);
        // step to initiate collision
        step();
        // check if both enemy31 and the laser have been removed from scene upon collision
        assertFalse(isNodeInMyScene(myEnemy31));
        assertFalse(isNodeInMyScene(mySpaceshipProjectile));
    }

    @Test
    public void testSpaceshipInitialPosition () {
        assertEquals(Spaceship.DEFAULT_X_POS, mySpaceship.getX());
        assertEquals(Spaceship.DEFAULT_Y_POS, mySpaceship.getY());
        assertEquals(Spaceship.WIDTH, mySpaceship.getFitWidth());
        assertEquals(Spaceship.HEIGHT, mySpaceship.getFitHeight());
    }

    @Test
    public void testSpaceshipMoveLeftAndRight () {
        // test movement to right
        testSpaceshipMove(KeyCode.RIGHT, Spaceship.DEFAULT_X_POS +
                mySpaceship.getXSpeedOnKeyPress(), Spaceship.DEFAULT_X_POS);
        // test movement to left
        testSpaceshipMove(KeyCode.LEFT, Spaceship.DEFAULT_X_POS -
                mySpaceship.getXSpeedOnKeyPress(), Spaceship.DEFAULT_X_POS);
    }

    @Test
    public void testSpaceshipWrap () {
        // test wrap from right to left side of screen
       testSpaceshipMove(KeyCode.RIGHT, 0, Game.GAME_WIDTH - Spaceship.WIDTH);
       // test wrap from left to right side of screen
       testSpaceshipMove(KeyCode.LEFT, Game.GAME_WIDTH - mySpaceship.WIDTH, 0);
    }

    @Test
    public void testEnemiesInitialPosition() {
        int rows = 4;
        double yPos = Game.GAME_HEIGHT/2.0 - Enemy.HEIGHT*rows/2.0;
        for (int row = 0; row < rows; row++) {
            double xPos = (Game.GAME_WIDTH - EnemyLevel.ENEMIES_PER_ROW * (EnemyLevel.ENEMY_SPACING + Enemy.WIDTH)
                    - EnemyLevel.ENEMY_SPACING)/2;
            for (int col = 0; col < EnemyLevel.ENEMIES_PER_ROW; col++) {
                Enemy curEnemy = myEnemies.get(row).get(col);
                // check that enemy is in expected position
                assertEquals(curEnemy.getX(), xPos);
                assertEquals(curEnemy.getY(), yPos);
                xPos += Enemy.WIDTH + EnemyLevel.ENEMY_SPACING;
            }
            yPos += Enemy.HEIGHT;
        }
    }

    @Test
    public void testClearPageWhenBeatLevel() {
        myLevel.setLevelNumber(1);
        myLevel.setLevelLost(false);
        myLevel.getEvilEntities().clear();
        step();

        assertEquals("LEVEL COMPLETE!\n\n\n" +
                "PRESS S TO ADVANCE\n\n" +
                "PRESS R TO RESTART LEVEL\n\n" +
                "PRESS 1-9 TO CHANGE LEVEL", StatusDisplay.getMenuText().getText());
        assertEquals(0.0, StatusDisplay.getMenuBackground().getX());
        assertEquals(0.0, StatusDisplay.getMenuBackground().getY());
        assertEquals(400.0, StatusDisplay.getMenuBackground().getWidth());
        assertEquals(700.0, StatusDisplay.getMenuBackground().getHeight());
    }

    @Test
    public void testClearPageWhenBeatGame() {
        myLevel.setLevelNumber(Game.MAX_LEVEL);
        myLevel.setLevelLost(false);
        myLevel.getEvilEntities().clear();
        step();

        assertEquals("YOU WIN!\n\n\nPRESS E TO SAVE YOUR SCORE\nAND RESET POINTS\n\n" +
                "PRESS R TO RESTART LEVEL\n\n" +
                "PRESS 1-9 TO CHANGE LEVEL", StatusDisplay.getMenuText().getText());
        assertEquals(0.0, StatusDisplay.getMenuBackground().getX());
        assertEquals(0.0, StatusDisplay.getMenuBackground().getY());
        assertEquals(400.0, StatusDisplay.getMenuBackground().getWidth());
        assertEquals(700.0, StatusDisplay.getMenuBackground().getHeight());
    }

    @Test
    public void testEnemyImageChangingLevel1To2() {
        Image level1Img = myLevel.getEvilEntities().get(0).get(0).getImage();
        press(myScene, KeyCode.S);
        myLevel = myGame.getCurLevel();
        Image level2Img = myLevel.getEvilEntities().get(0).get(0).getImage();
        assertFalse(level1Img == level2Img);
    }

    @Test
    public void testEnemyImageChangingLevel2To3() {
        press(myScene, KeyCode.S);
        myLevel = myGame.getCurLevel();
        Image level2Img = myLevel.getEvilEntities().get(0).get(0).getImage();
        press(myScene, KeyCode.S);
        myLevel = myGame.getCurLevel();
        Image level3Img = myLevel.getEvilEntities().get(0).get(0).getImage();
        assertFalse(level2Img == level3Img);
    }

    @Test
    public void testEnemyImageChangeWhenHit() {
        press(myScene, KeyCode.S);
        press(myScene, KeyCode.SPACE);
        myLevel = myGame.getCurLevel();
        myEnemy31 = lookup(ENEMY_ABOVE_SPACESHIP).query();
        mySpaceshipProjectile = lookup("#spaceshipLaserProjectile0").query();

        // Check if image changed after getting hit
        Image imgBefore = myEnemy31.getImage();

        mySpaceshipProjectile.setX(myEnemy31.getX());
        mySpaceshipProjectile.setY(myEnemy31.getY() + 9.5*Laser.Y_SPEED*Game.SECOND_DELAY);
        step();

        Image imgAfter = myEnemy31.getImage();

        assertFalse(imgBefore == imgAfter);
    }

    @Test
    public void testMissilePowerUp() {
        press(myScene, KeyCode.S);
        press(myScene, KeyCode.S);
        press(myScene, KeyCode.M);
        for (Node node : myGame.getRoot().getChildren()) System.out.println(node.getId());
        PowerUp myPowerUp = lookup("#cheatPowerUp0").query();
        myPowerUp.setY(mySpaceship.getY() - 10);
        myPowerUp.setTimeActive(Game.SECOND_DELAY);

        step();

        press(myScene, KeyCode.SPACE);
        myLevel = myGame.getCurLevel();
        myEnemy31 = lookup(ENEMY_ABOVE_SPACESHIP).query();
        mySpaceshipProjectile = lookup("#spaceshipMissileProjectile0").query();

        int lifeBefore = myEnemy31.getLives();
        assertEquals(3, lifeBefore);

        mySpaceshipProjectile.setX(myEnemy31.getX());
        mySpaceshipProjectile.setY(myEnemy31.getY() + 9.5*Laser.Y_SPEED*Game.SECOND_DELAY);
        step();

        int lifeAfter = myEnemy31.getLives();

        assertEquals(lifeBefore - mySpaceshipProjectile.getDamage(), lifeAfter);
    }

    @Test
    public void testBossDeath() {
        // go to boss level (level 4)
        press(myScene, KeyCode.DIGIT4);
        // get boss by id
        Boss myBoss = lookup("#boss").query();
        // set boss's lives to 1 life
        myBoss.setLives(1);
        // assert that boss does not lose life after collision because it is invulnerable
        testBossCollision(myBoss, 0, 1);
        // step until spaceship can shoot again (~1 second)
        for (int i = 0; i < 1/myGame.SECOND_DELAY; i++) {
            step();
        }
        // make boss vulnerable
        myBoss.switchVulnerabilityStatus();
        // assert that boss now loses life after collision
        testBossCollision(myBoss, 1, 0);

    }

    private void testBossCollision(Boss myBoss, int projectileIdNumber, int expectedLives) {
        // fire laser
        press(myScene, KeyCode.SPACE);
        mySpaceshipProjectile = lookup("#spaceshipLaserProjectile" + projectileIdNumber).query();
        // reposition spaceship laser one step before hitting boss
        mySpaceshipProjectile.setX(myBoss.getX() + myBoss.getFitWidth()/2);
        mySpaceshipProjectile.setY(myBoss.getY() + myBoss.getFitHeight());
        // step to initiate collision
        step();
        assertEquals(expectedLives, myBoss.getLives());
    }

    private void step() {
        javafxRun(() -> myGame.step());
    }

    private boolean isNodeInMyScene(Node node) {
        return myGame.getRoot().getChildren().contains(node);
    }

    private void testSpaceshipMove(KeyCode code, double expectedPosition, double startingXPos) {
        // set initial position
        mySpaceship.setX(startingXPos);
        // move spaceship left or right one step by "pressing" the key
        press(myScene, code);
        // then check its position has changed properly
        assertEquals(expectedPosition, mySpaceship.getX());
    }

    private void testEnemiesReverseXDirection(int enemyColumn, double startingXPos, boolean isStartingXSpeedPositive) {
        // get first or last enemy in first row
        Enemy curEnemy = myEnemies.get(0).get(enemyColumn);
        // set enemy to xPos one step before colliding with wall
        curEnemy.setX(startingXPos);
        // if checking left bound, set enemy's direction to left
        if (!isStartingXSpeedPositive) curEnemy.reverseXDirection();
        // assert that the enemy's speed is in direction of wall before collision
        assertTrue(curEnemy.getXSpeed() * (isStartingXSpeedPositive ? 1 : -1) > 0);
        // step so enemy collides with wall
        step();
        // assert that enemy's speed is in opposite direction of wall after collision
        assertTrue(curEnemy.getXSpeed() * (isStartingXSpeedPositive ? 1 : -1) < 0);
    }

}
