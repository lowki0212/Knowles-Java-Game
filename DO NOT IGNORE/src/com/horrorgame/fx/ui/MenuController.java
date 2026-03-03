package com.horrorgame.fx.ui;

import com.horrorgame.audio.SoundManager;
import com.horrorgame.fx.DifficultyLevel;
import com.horrorgame.fx.core.SceneManager;
import com.horrorgame.fx.logic.GameController;
import java.net.URL;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Builds and manages menu/difficulty/instructions scenes (JavaFX UI only).
 */
public class MenuController {

    private final SceneManager sceneManager;
    private final GameController gameController;

    private DifficultyLevel currentDifficulty = DifficultyLevel.MEDIUM;

    public MenuController(SceneManager sceneManager, GameController gameController) {
        this.sceneManager = sceneManager;
        this.gameController = gameController;
    }

    public void applyFullscreenBounds(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setResizable(false);
    }

    public Scene buildMainMenuScene() {
        StackPane root = new StackPane();

        ImageView background = new ImageView();
        URL bgUrl = getClass().getResource("/com/horrorgame/assets/images/homescreen.gif");
        if (bgUrl != null) {
            background.setImage(new Image(bgUrl.toExternalForm()));
            background.setPreserveRatio(false);
            background.setFitWidth(1280);
            background.setFitHeight(720);
        }

        VBox content = new VBox(40);
        content.setAlignment(Pos.CENTER);

        Label title = new Label("DO NOT IGNORE");
        title.setStyle("-fx-text-fill: red; -fx-font-size: 72px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");

        Button startButton = new Button("Start");
        Button exitButton = new Button("Exit");
        styleMenuButton(startButton);
        styleMenuButton(exitButton);

        startButton.setOnAction(e -> {
            SoundManager.playSound("/com/horrorgame/assets/audio/hover_click.WAV");
            sceneManager.showDifficulty();
        });
        exitButton.setOnAction(e -> Platform.exit());

        content.getChildren().addAll(title, startButton, exitButton);

        root.getChildren().add(background);
        root.getChildren().add(content);

        StackPane.setAlignment(content, Pos.CENTER);

        Scene scene = new Scene(root, 1280, 720);
        background.fitWidthProperty().bind(scene.widthProperty());
        background.fitHeightProperty().bind(scene.heightProperty());

        SoundManager.playLoop("/com/horrorgame/assets/audio/menu_music.WAV");
        return scene;
    }

    public Scene buildDifficultyScene() {
        StackPane root = new StackPane();

        ImageView background = new ImageView();
        URL bgUrl = getClass().getResource("/com/horrorgame/assets/images/difficultyscreen.gif");
        if (bgUrl != null) {
            background.setImage(new Image(bgUrl.toExternalForm()));
            background.setPreserveRatio(false);
            background.setFitWidth(1280);
            background.setFitHeight(720);
        }

        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);

        Label title = new Label("Select Difficulty");
        title.setStyle("-fx-text-fill: red; -fx-font-size: 56px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");

        Button easyButton = new Button("Easy");
        Button mediumButton = new Button("Medium");
        Button hardButton = new Button("Hard");
        styleMenuButton(easyButton);
        styleMenuButton(mediumButton);
        styleMenuButton(hardButton);

        easyButton.setOnAction(e -> {
            SoundManager.playSound("/com/horrorgame/assets/audio/hover_click.WAV");
            currentDifficulty = DifficultyLevel.EASY;
            gameController.setDifficulty(currentDifficulty);
            sceneManager.showInstructions();
        });
        mediumButton.setOnAction(e -> {
            SoundManager.playSound("/com/horrorgame/assets/audio/hover_click.WAV");
            currentDifficulty = DifficultyLevel.MEDIUM;
            gameController.setDifficulty(currentDifficulty);
            sceneManager.showInstructions();
        });
        hardButton.setOnAction(e -> {
            SoundManager.playSound("/com/horrorgame/assets/audio/hover_click.WAV");
            currentDifficulty = DifficultyLevel.HARD;
            gameController.setDifficulty(currentDifficulty);
            sceneManager.showInstructions();
        });

        Button backButton = new Button("Back");
        styleBackButton(backButton);
        backButton.setOnAction(e -> {
            SoundManager.playSound("/com/horrorgame/assets/audio/hover_click.WAV");
            sceneManager.showMainMenu();
        });

        content.getChildren().addAll(title, easyButton, mediumButton, hardButton, backButton);

        root.getChildren().add(background);
        root.getChildren().add(content);

        StackPane.setAlignment(content, Pos.CENTER);

        Scene scene = new Scene(root, 1280, 720);
        background.fitWidthProperty().bind(scene.widthProperty());
        background.fitHeightProperty().bind(scene.heightProperty());

        return scene;
    }

    public Scene buildInstructionsScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");

        Label title = new Label("Instructions");
        title.setStyle("-fx-text-fill: red; -fx-font-size: 56px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");

        VBox topBox = new VBox(title);
        topBox.setAlignment(Pos.CENTER);
        topBox.setStyle("-fx-padding: 30 0 10 0;");
        root.setTop(topBox);

        VBox content = new VBox(8);
        content.setAlignment(Pos.TOP_LEFT);
        content.setStyle("-fx-padding: 10 40 10 40;");
        content.setMaxWidth(800);

        content.getChildren().addAll(
                createInstructionText("Missing Object - objects that were there before are missing."),
                createInstructionText("Object Displacement - objects that were displaced from their position."),
                createInstructionText("Shadowy Figure - shadowy entity that is in the background."),
                createInstructionText("Intruder - humanoid entity that can cause harm if not reported."),
                createInstructionText("Strange Imagery - any imagery that should not be there."),
                createInstructionText("Demonic - demonic entity, often with something red."),
                createInstructionText("Extra Object - new objects that were not there before."),
                createInstructionText("Audio Disturbance - any audio that is not normal.")
        );

        Label noteTitle = new Label("NOTE:");
        noteTitle.setStyle("-fx-text-fill: #ff7777; -fx-font-size: 20px; -fx-font-family: Arial; -fx-font-weight: bold;");
        Label noteBody = new Label("MEMORIZE EVERY ROOM. Anomalies start appearing at 12:30 AM.");
        noteBody.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-family: Arial;");

        VBox centerBox = new VBox(10, content, noteTitle, noteBody);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setStyle("-fx-padding: 0 40 0 40;");
        root.setCenter(centerBox);

        Button backButton = new Button("Back");
        styleBackButton(backButton);
        backButton.setOnAction(e -> {
            SoundManager.playSound("/com/horrorgame/assets/audio/hover_click.WAV");
            sceneManager.showDifficulty();
        });

        Button startShiftButton = new Button("Start");
        styleMenuButton(startShiftButton);
        startShiftButton.setOnAction(e -> sceneManager.startGame());

        HBox bottomButtons = new HBox(20, backButton, startShiftButton);
        bottomButtons.setAlignment(Pos.CENTER);
        bottomButtons.setStyle("-fx-padding: 20 0 40 0;");
        root.setBottom(bottomButtons);

        return new Scene(root, 1280, 720);
    }

    private Label createInstructionText(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-family: Arial;");
        return label;
    }

    private void styleMenuButton(Button button) {
        button.setStyle("-fx-background-color: #400000; -fx-text-fill: red; -fx-font-size: 32px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");
        button.setPrefWidth(260);
    }

    private void styleBackButton(Button button) {
        button.setStyle("-fx-background-color: #300000; -fx-text-fill: red; -fx-font-size: 22px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");
        button.setPrefWidth(160);
    }
}

