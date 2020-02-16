package invader;

import invader.entity.Enemy;
import invader.entity.Entity;
import invader.entity.Spaceship;
import invader.projectile.Bomb;
import invader.projectile.Laser;

import invader.projectile.Projectile;
import javafx.scene.Group;
import javafx.scene.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class Level {

    public static final String LEVEL_FILE_PATH = "resources/level_files/level_";
    public static final String LEVEL_FILE_EXTENSION = ".txt";
    public static final int SPACESHIP_LASER_ROTATION = 0;

    protected int curSpaceshipProjectileIdNumber = 0;
    protected boolean levelLost = false;
    protected Game myGame;
    protected Group root;
    protected int levelNumber;
    protected Spaceship spaceship;
    protected List<Projectile> spaceshipProjectiles = new ArrayList<>();
    protected List<Projectile> evilEntityProjectiles = new ArrayList<>();

    public Level(Group root, int levelNumber, Game myGame){
        this.root = root;
        String levelFile = LEVEL_FILE_PATH + levelNumber + LEVEL_FILE_EXTENSION;
        readFile(levelFile);
        this.levelNumber = levelNumber;
        createEvilEntities();
        addEntitiesToScene();
        this.myGame = myGame;
        StatusDisplay.updateLevelNumberDisplay(levelNumber);
        StatusDisplay.updateLifeCountDisplay(spaceship.getLives());
    }

    public void moveSpaceship(boolean toRight) {
        spaceship.setX(spaceship.getX() + spaceship.getXSpeedOnKeyPress() * (toRight ? 1 : -1));
        spaceship.wrap();
    }

    public void addLife() {
        spaceship.addLife();
        StatusDisplay.updateLifeCountDisplay(spaceship.getLives());
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public void setLevelLost(boolean levelLost) {
        this.levelLost = levelLost;
    }

    public abstract List<List<Enemy>> getEvilEntities();

    public abstract void clearLevel();

    public abstract void addEntitiesToScene();

    public abstract void handleEntitiesAndLasers(double gameTimer, double elapsedTime);

    public abstract void attemptLevelVictory();

    public abstract void addPowerUp(double gameTimer);

    public abstract void destroyFirstEnemy();

    protected abstract void updateNodePositionsOnStep(double elapsedTime);

    protected void updateProjectilePositionsOnStep(double elapsedTime, List<Projectile> projectiles) {
        for (Projectile projectile : projectiles) {
            projectile.updatePositionOnStep(elapsedTime);
        }
    }

    protected void handleProjectileCollisionWithSpaceship(List<Projectile> evilEntityProjectiles, Spaceship spaceship) {
        if (handleProjectileCollisions(evilEntityProjectiles, spaceship)) {
            StatusDisplay.updateLifeCountDisplay(spaceship.getLives());
            if (spaceship.getLives() == 0) {
                levelLost = true;
                myGame.setMenuActive();
                clearLevel();
                StatusDisplay.createGameOverMenu(root);
            }
        }
    }

    protected boolean handleProjectileCollisions(List<Projectile> projectiles, Entity entity) {
        boolean isCollision = false;
        List<Projectile> projectilesToRemove = new ArrayList<>();
        for (Projectile projectile : projectiles) {
            if (projectile.intersects(entity) || projectile.isOutOfYBounds()) {
                projectilesToRemove.add(projectile);
                if (projectile.intersects(entity)) {
                    entity.removeLives(projectile.getDamage());
                    StatusDisplay.updatePointsDisplay(entity.getPointsPerHit());
                    isCollision = true;
                }
            }
        }
        projectiles.removeAll(projectilesToRemove);
        root.getChildren().removeAll(projectilesToRemove);
        return isCollision;
    }

    protected void attemptSpaceshipFire(double gameTimer) {
        attemptProjectileFire(gameTimer, spaceship, spaceshipProjectiles, 1, SPACESHIP_LASER_ROTATION, curSpaceshipProjectileIdNumber);
    }

    protected void attemptProjectileFire(double gameTimer, Entity entity, List<Projectile> projectiles, double timeBeforeNextShot, double rotation,
                                         int idNumber) {
        if (gameTimer >= entity.getStartShootingTime()) {
            shootLaser(entity, projectiles, timeBeforeNextShot, rotation, idNumber);
        }
    }

    protected Projectile shootLaser(Entity entityShooting, List<Projectile> lasers, double timeBeforeNextShot, double rotation, int idNumber) {
        boolean isSpaceship = entityShooting.getClass() == Spaceship.class;
        Projectile projectile;
        if (isSpaceship) {
            projectile = createSpaceshipProjectile(spaceship, rotation, idNumber);
        }
        else {
            projectile = createEvilEntityProjectile(entityShooting, rotation, idNumber);
        }

        lasers.add(projectile);
        root.getChildren().add(projectile);
        entityShooting.addToStartShootingTime(timeBeforeNextShot);
        return projectile;
    }

    protected Projectile createSpaceshipProjectile(Spaceship spaceship, double rotation, int idNumber) {
        Projectile projectile;
        if (spaceship.hasBombPowerUp()) {
            projectile = new Bomb(spaceship.getX() + spaceship.getFitWidth()/2,
                    spaceship.getY(), false, rotation, idNumber++);
        }
        else {
            projectile = new Laser(spaceship.getX() + spaceship.getFitWidth()/2,
                    spaceship.getY(), false, rotation, idNumber++);
        }
        return projectile;
    }

    protected abstract Projectile createEvilEntityProjectile(Entity entityShooting, double rotation, int idNumber);

    protected <T extends Node> void clearNodesFromSceneAndLevel(T node) {
        root.getChildren().remove(node);
        node = null;
    }

    protected <T extends Node> void clearNodesFromSceneAndLevel(List<T> nodes) {
        root.getChildren().removeAll(nodes);
        nodes.clear();
    }

    protected <T extends Node> void clear2dNodesFromSceneAndLevel(List<List<T>> nodes) {
        for (List<T> row : nodes) {
            root.getChildren().removeAll(row);
        }
        nodes.clear();
    }

    protected void initiateLevelVictory() {
        myGame.setMenuActive();
        clearLevel();
        if (getLevelNumber() == Game.MAX_LEVEL) {
            StatusDisplay.createVictoryMenu(root);
        } else {
            StatusDisplay.createLevelIntermissionMenu(root);
        }
    }

    protected abstract void handleEvilEntitiesMovement();

    protected abstract void handleEvilEntityLasers(double gameTimer);

    protected abstract void handleSpaceshipProjectiles();

    protected abstract void createEvilEntities();

    protected abstract void handleFileLines(Scanner myReader);

    private void readFile(String levelFile) {
        try {
            File file = new File(levelFile);
            Scanner myReader = new Scanner(file);
            handleFileLines(myReader);
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while reading level layout txt file: " + levelFile);
            e.printStackTrace();
        }
    }
}
