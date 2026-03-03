package com.horrorgame.fx;

import com.horrorgame.fx.core.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX entry point.
 * Responsibilities:
 * - Application startup
 * - Primary stage launch
 * - Delegate all scene/game work to {@link SceneManager}
 * - Cleanup on termination
 */
public class FxMain extends Application {

    private SceneManager sceneManager;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        sceneManager = new SceneManager(primaryStage);
        sceneManager.init();
    }

    @Override
    public void stop() {
        if (sceneManager != null) {
            sceneManager.shutdown();
        }
    }
}