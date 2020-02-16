package invader.level;

import invader.Game;
import invader.StatusDisplay;
import invader.entity.Boss;
import invader.entity.Enemy;
import invader.entity.Spaceship;
import invader.powerup.BurstFirePowerUp;
import invader.powerup.MissilePowerUp;
import invader.powerup.PowerUp;
import invader.powerup.SpaceshipSpeedPowerUp;
import invader.projectile.Projectile;
import javafx.scene.Group;

import java.util.List;
import java.util.Scanner;

public class BossLevel extends Level {
    public static final int DEFAULT_SPACESHIP_LIVES = 5;

    private Boss boss;
    private int bossLives;
    private int curBossProjectileIdNumber = 0;
    private double invulnerableTimer = 0;

    public BossLevel(Group root, int levelNumber, Game myGame){
        super(root, levelNumber, myGame);
        spaceship.setLives(DEFAULT_SPACESHIP_LIVES);
        StatusDisplay.updateLifeCountDisplay(DEFAULT_SPACESHIP_LIVES);
    }

    @Override
    public void clearLevel() {
        clearNodesFromSceneAndLevel(spaceship);
        clearNodesFromSceneAndLevel(boss);
        clearNodesFromSceneAndLevel(evilEntityProjectiles);
        clearNodesFromSceneAndLevel(spaceshipProjectiles);
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
        attemptBossFire();
        attemptVulnerabilitySwitch(gameTimer);
        handleSpaceshipProjectiles();
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
        updateProjectilePositionsOnStep(elapsedTime, evilEntityProjectiles);
        updateProjectilePositionsOnStep(elapsedTime, spaceshipProjectiles);
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
        handleProjectileCollisionWithSpaceship(evilEntityProjectiles, spaceship);
        handleProjectileBounds(evilEntityProjectiles);
    }

    @Override
    protected void handleSpaceshipProjectiles() {
        handleProjectileCollisions(spaceshipProjectiles, boss);
        if (boss.getLives() == 0) clearNodesFromSceneAndLevel(boss);
    }

    @Override
    protected void handleFileLines(Scanner myReader) {
        String data = myReader.nextLine();
        bossLives = Integer.parseInt(data);
    }

    @Override
    public void addRandomPowerUp(double gameTimer) {
       return;
    }

    @Override
    public void addSpeedPowerUp(double gameTimer) {
        return;
    }

    @Override
    public void addMissilePowerUp(double gameTimer) {
        return;
    }

    @Override
    public void addBurstFirePowerUp(double gameTimer) {
        return;
    }

    @Override
    public void destroyFirstEnemy() {
        boss.setLives(0);
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
            if (boss.isVulnerable()) {
                boss.setHasFireballBlast(true);
                blastFire(boss, evilEntityProjectiles);
                boss.setHasFireballBlast(false);
            }
        }
    }

    private void attemptBossFire() {
        if (!boss.isVulnerable()) invulnerableTimer++;
        if (!boss.isVulnerable() && invulnerableTimer >= boss.getStartShootingTime()) {
            shootProjectile(boss, evilEntityProjectiles, Projectile.DEFAULT_PROJECTILE_ROTATION);
        }
    }
}