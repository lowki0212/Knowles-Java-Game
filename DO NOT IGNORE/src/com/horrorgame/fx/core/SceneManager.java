package com.horrorgame.fx.core;

import com.horrorgame.audio.SoundManager;
import com.horrorgame.fx.logic.AnomalyManager;
import com.horrorgame.fx.logic.GameController;
import com.horrorgame.fx.logic.ThreatManager;
import com.horrorgame.fx.ui.GameSceneController;
import com.horrorgame.fx.ui.MenuController;
import com.horrorgame.game.RoomMediaLibrary;
import java.util.List;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Coordinates high-level scene switching and lifecycle for the JavaFX game.
 */
public class SceneManager {

    private final Stage stage;

    private RoomMediaLibrary mediaLibrary;
    private List<String> roomKeys;

    private GameController gameController;
    private MenuController menuController;
    private GameSceneController gameSceneController;

    private Scene mainMenuScene;
    private Scene difficultyScene;
    private Scene instructionsScene;
    private Scene gameScene;

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    public void init() {
        mediaLibrary = RoomMediaLibrary.load(SceneManager.class);
        roomKeys = mediaLibrary.getRoomKeys();
        if (roomKeys.isEmpty()) {
            throw new IllegalStateException("No room MP4s found under /com/horrorgame/assets/images/rooms/");
        }

        ThreatManager threatManager = new ThreatManager();
        AnomalyManager anomalyManager = new AnomalyManager(mediaLibrary, roomKeys);

        gameController = new GameController(mediaLibrary, roomKeys, threatManager, anomalyManager, this);
        gameSceneController = new GameSceneController(gameController, threatManager);
        gameController.attachView(gameSceneController);

        menuController = new MenuController(this, gameController);

        mainMenuScene = menuController.buildMainMenuScene();
        difficultyScene = menuController.buildDifficultyScene();
        instructionsScene = menuController.buildInstructionsScene();
        gameScene = gameSceneController.buildGameScene();

        stage.setTitle("DO NOT IGNORE");
        stage.setScene(mainMenuScene);
        menuController.applyFullscreenBounds(stage);
        stage.show();
    }

    public void showMainMenu() {
        gameController.shutdown();
        SoundManager.playLoop("/com/horrorgame/assets/audio/menu_music.WAV");
        stage.setScene(mainMenuScene);
        menuController.applyFullscreenBounds(stage);
    }

    public void showDifficulty() {
        stage.setScene(difficultyScene);
        menuController.applyFullscreenBounds(stage);
    }

    public void showInstructions() {
        stage.setScene(instructionsScene);
        menuController.applyFullscreenBounds(stage);
    }

    public void startGame() {
        SoundManager.stopLoop();
        SoundManager.playLoop("/com/horrorgame/assets/audio/VHSNoise.wav");
        gameController.startNewGame();
        stage.setScene(gameScene);
        menuController.applyFullscreenBounds(stage);
    }

    public void playAgainSameDifficulty() {
        // Keep previously selected difficulty; just restart the session.
        SoundManager.stopLoop();
        SoundManager.playLoop("/com/horrorgame/assets/audio/VHSNoise.wav");
        gameController.startNewGame();
        stage.setScene(gameScene);
        menuController.applyFullscreenBounds(stage);
    }

    public void shutdown() {
        if (gameController != null) {
            gameController.shutdown();
        }
        SoundManager.stopLoop();
    }
}

