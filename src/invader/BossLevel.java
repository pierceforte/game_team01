package invader;

import invader.entity.Boss;
import invader.entity.Enemy;
import invader.entity.Entity;
import invader.entity.Spaceship;
import invader.projectile.Laser;
import javafx.geometry.NodeOrientation;
import javafx.scene.Group;
import javafx.scene.Node;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class BossLevel extends Level {

    public static final int DEFAULT_LASER_ROTATION = 0;
    public static final int BLAST_LASER_Y_SPEED = -80;
    public static final int LEFT_LASER_ROTATION = 45;
    public static final int LEFT_LASER_X_SPEED = -15;
    public static final int RIGHT_LASER_ROTATION = -45;
    public static final int RIGHT_LASER_X_SPEED = 15;

    private Boss boss;
    private int bossLives;
    private int curBossLaserIdNumber = 0;
    private double invulnerableTimer = 0;

    public BossLevel(Group root, int levelNumber, Game myGame){
        super(root, levelNumber, myGame);
    }

    @Override
    public void clearLevel() {
        clearNodesFromSceneAndLevel(spaceship);
        clearNodesFromSceneAndLevel(boss);
        clearNodesFromSceneAndLevel(evilEntityLasers);
        clearNodesFromSceneAndLevel(spaceshipLasers);
    }

    @Override
    public void addEntitiesToScene() {
        root.getChildren().add(boss);
        spaceship = new Spaceship(Spaceship.DEFAULT_X_POS, Spaceship.DEFAULT_Y_POS);
        root.getChildren().add(spaceship);
    }

    @Override
    protected void createEvilEntities() {
        boss = new Boss(Game.GAME_WIDTH/2 - Boss.DEFAULT_WIDTH /2, Game.GAME_HEIGHT/2 - Boss.DEFAULT_HEIGHT /2,
                Boss.DEFAULT_SPEED, Boss.DEFAULT_SPEED, bossLives);
    }

    @Override
    public void handleEntitiesAndLasers(double gameTimer, double elapsedTime) {
        updateNodePositionsOnStep(elapsedTime);
        handleEvilEntitiesMovement();
        handleEvilEntityLasers(gameTimer);
        attemptBossFire(gameTimer);
        attemptVulnerabilitySwitch(gameTimer);
        handleSpaceshipLasers();
        attemptLevelVictory();
    }

    @Override
    public void attemptLevelVictory() {
        if(!levelLost && boss.getLives() == 0) {
            initiateLevelVictory();
        }
    }

    @Override
    protected void updateNodePositionsOnStep(double elapsedTime) {
        boss.updatePositionOnStep(elapsedTime);
        updateLaserPositionsOnStep(elapsedTime, evilEntityLasers);
        updateLaserPositionsOnStep(elapsedTime, spaceshipLasers);
    }

    @Override
    public List<List<Enemy>> getEvilEntities() {
        return null;
    }

    @Override
    protected void handleEvilEntitiesMovement() {
        updateBossPosition();
    }

    @Override
    protected void handleEvilEntityLasers(double gameTimer) {
        handleLaserCollisionWithSpaceship(evilEntityLasers, spaceship);
    }

    @Override
    protected void handleSpaceshipLasers() {
        Boss bossCollision = (Boss) handleLaserCollisions(spaceshipLasers, boss);
        if (boss.isVulnerable() && bossCollision != null) {
            boss.lowerLives();
            if (boss.getLives() == 0) clearNodesFromSceneAndLevel(boss);
        }
    }

    @Override
    protected void handleFileLines(Scanner myReader) {
        String data = myReader.nextLine();
        bossLives = Integer.parseInt(data);
    }

    @Override
    public void addPowerUp(double gameTimer) {
        return;
    }

    @Override
    public void destroyFirstEnemy() {
        return;
    }

    private void updateBossPosition() {
        if (boss.isOutOfXBounds()) {
            boss.reverseXDirection();
            boss.setRandomSpeed();
        }
        if (boss.isOutOfYBounds()) {
            boss.reverseYDirection();
            boss.setRandomSpeed();
        }
    }

    private void attemptVulnerabilitySwitch(double gameTimer) {
        if (gameTimer >= boss.getSwitchVulnerabilityTime()) {
            boss.switchVulnerabilityStatus();
            if (boss.isVulnerable()) bossBlastFire();
        }
    }

    @Override
    protected Laser createEvilEntityLaser(Entity entityShooting, double rotation, int idNumber) {
        Laser laser = new Laser(entityShooting.getX() + entityShooting.getFitWidth()/2,
                entityShooting.getY(), true, rotation, idNumber++);
        return laser;
    }

    private void attemptBossFire(double gameTimer) {
        if (!boss.isVulnerable()) invulnerableTimer++;
        if (!boss.isVulnerable() && invulnerableTimer >= boss.getStartShootingTime()) {
            shootLaser(boss, evilEntityLasers, Boss.TIME_BETWEEN_SHOTS, DEFAULT_LASER_ROTATION, curBossLaserIdNumber);
        }
    }

    private void bossBlastFire() {
        Laser centerLaser = shootLaser(boss, evilEntityLasers, Boss.TIME_BETWEEN_SHOTS, DEFAULT_LASER_ROTATION, curBossLaserIdNumber);
        centerLaser.setYSpeed(BLAST_LASER_Y_SPEED);
        Laser leftLaser = shootLaser(boss, evilEntityLasers, Boss.TIME_BETWEEN_SHOTS, LEFT_LASER_ROTATION, curBossLaserIdNumber);
        leftLaser.setXSpeed(LEFT_LASER_X_SPEED);
        leftLaser.setYSpeed(BLAST_LASER_Y_SPEED);
        Laser rightLaser = shootLaser(boss, evilEntityLasers, Boss.TIME_BETWEEN_SHOTS, RIGHT_LASER_ROTATION, curBossLaserIdNumber);
        rightLaser.setXSpeed(RIGHT_LASER_X_SPEED);
        rightLaser.setYSpeed(BLAST_LASER_Y_SPEED);
    }
}
