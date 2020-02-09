package invader;

import invader.entity.Enemy;
import invader.entity.Entity;
import invader.entity.Spaceship;
import invader.powerup.PowerUp;
import invader.powerup.SpaceshipSpeedPowerUp;
import invader.projectile.Laser;

import javafx.scene.Group;
import javafx.scene.Node;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Level {

    public static final String LEVEL_FILE_PATH = "resources/level_files/level_";
    public static final String LEVEL_FILE_EXTENSION = ".txt";
    public static final double ENEMY_SPACING = 10;
    public static final int ENEMIES_PER_ROW = 9;
    public static final int POINTS_PER_ENEMY_HIT = 25;
    public static final int ENEMY_SPEED_FACTOR_BY_LEVEL = 10;

    private int curSpaceshipLaserIdNumber = 0;
    private int curEnemyLaserIdNumber = 0;

    private Group root;
    private int levelNumber;
    private int rows;
    private Spaceship spaceship;
    private List<Laser> spaceshipLasers = new ArrayList<>();
    private List<List<Integer>> enemyIdentifiers = new ArrayList<>();
    private List<List<Enemy>> enemies = new ArrayList<>();
    private List<Laser> enemyLasers = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();

    public Level(Group root, int levelNumber){
        this.root = root;
        String levelFile = LEVEL_FILE_PATH + levelNumber + LEVEL_FILE_EXTENSION;
        readFile(levelFile);
        this.levelNumber = levelNumber;
        createEnemies();
    }

    public void clearLevel() {
        root.getChildren().remove(spaceship);
        root.getChildren().removeAll(enemyLasers);
        root.getChildren().removeAll(spaceshipLasers);
        for (List<Enemy> enemyRow : enemies) {
            root.getChildren().removeAll(enemyRow);
        }
    }

    public void addEnemiesAndSpaceshipToScene() {
        for (List<Enemy> enemyRow : enemies) root.getChildren().addAll(enemyRow);
        spaceship = new Spaceship(Spaceship.DEFAULT_X_POS, Spaceship.DEFAULT_Y_POS);
        root.getChildren().add(spaceship);
    }

    public void addLife() {
        spaceship.addLife();
        StatusDisplay.updateLifeCountDisplay(spaceship.getLives());
    }

    public void handleEntitiesAndLasers(double gameTimer, double elapsedTime) {
        updateNodePositionsOnStep(elapsedTime);
        handleEnemiesMovement();
        handleEnemyLasers(gameTimer);
        handleSpaceshipLasers();
        handlePowerUpsMovement(gameTimer);
    }

    private void updateNodePositionsOnStep(double elapsedTime) {
        for (List<Enemy> enemyRow : enemies) {
            for (Enemy enemy : enemyRow) {
                enemy.updatePositionOnStep(elapsedTime);
            }
        }
        for (PowerUp powerUp: powerUps) powerUp.updatePositionOnStep(elapsedTime);
        updateLaserPositionsOnStep(elapsedTime, enemyLasers);
        updateLaserPositionsOnStep(elapsedTime, spaceshipLasers);
    }

    private void updateLaserPositionsOnStep(double elapsedTime, List<Laser> lasers) {
        for (Laser laser : lasers) {
            laser.updatePositionOnStep(elapsedTime);
        }
    }

    public void attemptSpaceshipFire(double gameTimer) {
        attemptLaserFire(gameTimer, spaceship, spaceshipLasers, 1);
    }

    private void attemptLaserFire(double gameTimer, Entity entity, List<Laser> lasers, double timeBeforeNextShot) {
        if (gameTimer >= entity.getStartShootingTime()) {
            shootLaser(entity, lasers, timeBeforeNextShot);
        }
    }

    private void handleEnemiesMovement() {
        boolean reverseMovement = false;
        for (List<Enemy> enemyRow : enemies) {
            if (enemyRow.get(0).isOutOfXBounds() || enemyRow.get(enemyRow.size()-1).isOutOfXBounds()) {
                reverseMovement = true;
                break;
            }
        }
        if (reverseMovement) {
            for (List<Enemy> enemyRow : enemies) {
                for (Enemy enemy : enemyRow) {
                    enemy.reverseXDirection();
                }
            }
        }
    }

    private void handleEnemyLasers(double gameTimer) {
        for (List<Enemy> enemyRow : enemies) {
            for (Enemy enemy : enemyRow) {
                attemptLaserFire(gameTimer, enemy, enemyLasers, 50);
            }
        }
        if (handleLaserCollisions(enemyLasers, spaceship) != null) {
            spaceship.lowerLives();
            StatusDisplay.updateLifeCountDisplay(spaceship.getLives());
            if (spaceship.getLives() == 0) {
                clearLevel();
                StatusDisplay.createGameOverMenu(root);
            }
        }
    }

    private void handleSpaceshipLasers() {
        List<Enemy> enemiesToRemove = new ArrayList<>();
        if(enemies.size() == 0) {
            if (getLevelNumber() == Game.MAX_LEVEL) {
                StatusDisplay.createVictoryMenu(root);
            } else {
                StatusDisplay.createLevelIntermissionMenu(root);
            }
            return;
        }
        for (List<Enemy> enemyRow : enemies) {
            for (Enemy enemy : enemyRow) {
                Enemy enemyToRemove = (Enemy) handleLaserCollisions(spaceshipLasers, enemy);
                if (enemyToRemove != null) {
                    enemiesToRemove.add(enemyToRemove);
                    powerUps.add(enemy.getPowerUp());
                    root.getChildren().add(enemy.getPowerUp());
                    //enemy.getPowerUp().setActive();
                }
            }
        }
        removeInactiveEnemies(enemiesToRemove);
    }

    private void handlePowerUpsMovement(double gameTimer) {
        List<PowerUp> powerUpsToRemove = new ArrayList<>();
        for (PowerUp powerUp: powerUps) {
            if (powerUp.intersects(spaceship) || powerUp.isOutOfYBounds()) {
                if (powerUp.intersects(spaceship)) {
                    powerUp.activate(gameTimer, spaceship);
                }
                powerUpsToRemove.add(powerUp);
            }
        }
        root.getChildren().removeAll(powerUpsToRemove);
    }

    private void removeInactiveEnemies(List<Enemy> enemiesToRemove) {
        root.getChildren().removeAll(enemiesToRemove);
        for(List<Enemy> enemyRow : enemies) {
            enemyRow.removeAll(enemiesToRemove);
        }
        List<List<Enemy>> enemyRowsToRemove = new ArrayList<>();
        for(List<Enemy> enemyRow : enemies) {
            if (enemyRow.size() == 0) enemyRowsToRemove.add(enemyRow);
        }
        enemies.removeAll(enemyRowsToRemove);
    }

    private Entity handleLaserCollisions(List<Laser> lasers, Entity entity) {
        List<Laser> lasersToRemove = new ArrayList<>();
        boolean removeEntity = false;
        for (Laser laser : lasers) {
            if (laser.intersects(entity) || laser.isOutOfYBounds()) {
                lasersToRemove.add(laser);
                if (laser.intersects(entity)) {
                    removeEntity = true;
                    if (entity.getClass() == Enemy.class) {
                        StatusDisplay.updatePointsDisplay(POINTS_PER_ENEMY_HIT);
                    }
                }
            }
        }
        lasers.removeAll(lasersToRemove);
        root.getChildren().removeAll(lasersToRemove);
        return removeEntity ? entity : null;
    }

    private void shootLaser(Entity entityShooting, List<Laser> lasers, double timeBeforeNextShot) {
        boolean isEnemy = entityShooting.getClass() == Enemy.class;
        Laser laser = new Laser(entityShooting.getX() + Entity.NON_BOSS_WIDTH/2,
                entityShooting.getY(), isEnemy, isEnemy ? curEnemyLaserIdNumber++ : curSpaceshipLaserIdNumber++);
        lasers.add(laser);
        root.getChildren().add(laser);
        entityShooting.addToStartShootingTime(timeBeforeNextShot);
    }

    private void createEnemies() {
        // get height of first brick row to ensure they are centered
        double yPos = Game.GAME_HEIGHT/2.0 - Enemy.HEIGHT*rows/2.0;
        for (int i = 0; i < enemyIdentifiers.size(); i++) {
            List<Enemy> tempRow = new ArrayList<>();
            double xPos = (Game.GAME_WIDTH - enemyIdentifiers.get(0).size() * (ENEMY_SPACING + Enemy.WIDTH) - ENEMY_SPACING)/2;
            for (int j = 0; j < enemyIdentifiers.get(0).size(); j++) {


                SpaceshipSpeedPowerUp curPowerUp = new SpaceshipSpeedPowerUp(xPos + Enemy.WIDTH/2, yPos);

                Enemy curEnemy = new Enemy(xPos, yPos, ENEMY_SPEED_FACTOR_BY_LEVEL*levelNumber,
                        0, enemyIdentifiers.get(i).get(j), j + i*Level.ENEMIES_PER_ROW, curPowerUp);
                /*
                if (!powerUpGrid.get(i*BRICKS_PER_ROW + j).equals("none")) {
                    curBrick.setPowerUp(powerUpGrid.get(i*BRICKS_PER_ROW + j));
                }
                */
                tempRow.add(curEnemy);
                xPos += Enemy.WIDTH + ENEMY_SPACING;
            }
            yPos += Enemy.HEIGHT;
            enemies.add(tempRow);
        }
    }

    public void moveSpaceship(boolean toRight) {
        spaceship.setX(spaceship.getX() + spaceship.getXSpeedOnKeyPress() * (toRight ? 1 : -1));
        spaceship.wrap();
    }

    private void readFile(String levelFile) {
        try {
            File file = new File(levelFile);
            Scanner myReader = new Scanner(file);
            enemyIdentifiers = new ArrayList<>();
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String [] cleanRow = data.split(",");
                List<Integer> row = new ArrayList<>();
                for (String brick : cleanRow) {
                    row.add(Integer.parseInt(brick));
                }
                enemyIdentifiers.add(row);
            }
            rows = this.enemyIdentifiers.size();
            //assignPowerUpsToBricks();
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while reading level layout txt file: " + levelFile);
            e.printStackTrace();
        }
    }

    public int getLevelNumber() {
        return levelNumber;
    }

}
