package invader;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Game extends Application {
    public static final String TITLE = "Space Invaders";
    public static final String WINNING_MESSAGE = "WINNER!";
    public static final int GAME_WIDTH = 400;
    public static final int GAME_HEIGHT = 600;
    public static final int SCENE_WIDTH = 400;
    public static final int SCENE_HEIGHT = 700;
    public static final int FRAMES_PER_SECOND = 60;
    public static final double SECOND_DELAY = 1.0 / FRAMES_PER_SECOND;
    public static final Paint BACKGROUND = Color.BLACK;
    public static final int MAX_LEVEL = 4;

    // some things we need to remember during our game
    private Scene myScene;
    private Timeline myAnimation;
    private KeyHandler myKeyHandler;
    private double gameTimer = 0;
    private Level curLevel;
    private Group root;
    private boolean isMenuActive = false;
    private boolean isGameOverMenuActive = false;
    private boolean isHighScoreTextFieldActive = false;
    private boolean isQuitGameMenuActive = false;

    /**
     * Initialize what will be displayed and how it will be updated.
     */
    @Override
    public void start (Stage stage) {
        // attach scene to the stage and display it
        myScene = setupScene(SCENE_WIDTH, SCENE_HEIGHT, BACKGROUND);
        stage.setScene(myScene);
        stage.setTitle(TITLE);
        stage.show();
        // attach "game loop" to timeline to play it
        KeyFrame frame = new KeyFrame(Duration.seconds(SECOND_DELAY), e -> step());
        myAnimation = new Timeline();
        myAnimation.setCycleCount(Timeline.INDEFINITE);
        myAnimation.getKeyFrames().add(frame);
        myAnimation.play();
        //StatusDisplay.createStartMenu(root);
    }

    // Create the game's "scene": what shapes will be in the game and their starting properties
    public Scene setupScene(int width, int height, Paint background) {
        // create one top level collection to organize the things in the scene
        root = new Group();

        // create a place to see the shapes
        myScene = new Scene(root, width, height, background);

        // create a level
        StatusDisplay.createInterfaceAndAddToRoot(root, GAME_HEIGHT, SCENE_WIDTH, SCENE_HEIGHT);
        curLevel = new EnemyLevel(root,1, this);
        // respond to input
        myKeyHandler = new KeyHandler(this);
        myScene.setOnKeyPressed(e -> handleKeyInput(e.getCode()));
        return myScene;
    }

    public Group getRoot() {
        return root;
    }

    public Level getCurLevel() {
        return curLevel;
    }

    public void setCurLevel(Level curLevel) {
        this.curLevel = curLevel;
    }

    public double getGameTimer() {
        return gameTimer;
    }

    public void setGameTimer(double time) {
        gameTimer = time;
    }

    public void setMenuActive() {
        isMenuActive = true;
    }

    public void setMenuInActive() {
        isMenuActive = false;
    }

    public boolean isMenuActive() {
        return isMenuActive;
    }

    public boolean isGameOverMenuActive() {
        return isGameOverMenuActive;
    }

    public void setGameOverMenuActive() {
        isGameOverMenuActive = true;
    }

    public void setGameOverMenuInactive() {
        isGameOverMenuActive = false;
    }

    public boolean isQuitGameMenuActive() {
        return isQuitGameMenuActive;
    }

    public void setQuitGameMenuActive() {
        isQuitGameMenuActive = true;
    }

    public void setQuitGameMenuInactive() {
        isQuitGameMenuActive = false;
    }

    public boolean isHighScoreTextFieldActive() {
        return isHighScoreTextFieldActive;
    }

    public void setHighScoreTextFieldActive() {
        isHighScoreTextFieldActive = true;
    }

    public void setHighScoreTextFieldInactive() {
        isHighScoreTextFieldActive = false;
    }

    public Animation.Status getAnimationStatus() {
        return myAnimation.getStatus();
    }

    public void pauseAnimation() {
        myAnimation.pause();
    }

    public void playAnimation() {
        myAnimation.play();
    }

    // Change properties of shapes to animate them
    void step() {
        if (!isMenuActive) {
            gameTimer += Game.SECOND_DELAY;
            curLevel.handleEntitiesAndLasers(gameTimer, Game.SECOND_DELAY);
        }
    }

    // What to do each time a key is pressed
    private void handleKeyInput (KeyCode code) {
        myKeyHandler.handleInput(code);
    }

    /**
     * Start the program.x
     */
    public static void main (String[] args) {
        launch(args);
    }
}
