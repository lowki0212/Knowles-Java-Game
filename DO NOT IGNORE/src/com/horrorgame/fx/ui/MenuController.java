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

        VBox content = new VBox(18);
        content.setAlignment(Pos.CENTER);

        Label title = new Label("Select Difficulty");
        title.setStyle("-fx-text-fill: red; -fx-font-size: 56px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");

        VBox easyCard = createDifficultyCard(
                "You may ignore",
                "Slow anomaly spawns • Low penalties • More forgiving reporting"
        );
        VBox mediumCard = createDifficultyCard(
                "Try not to ignore",
                "Balanced spawns • Moderate penalties • Stay attentive"
        );
        VBox hardCard = createDifficultyCard(
                "DO NOT IGNORE",
                "Fast spawns • Heavy penalties • Mistakes are costly"
        );

        Button easyButton = (Button) easyCard.getChildren().get(0);
        Button mediumButton = (Button) mediumCard.getChildren().get(0);
        Button hardButton = (Button) hardCard.getChildren().get(0);

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

        content.getChildren().addAll(title, easyCard, mediumCard, hardCard, backButton);

        root.getChildren().add(background);
        root.getChildren().add(content);

        StackPane.setAlignment(content, Pos.CENTER);

        Scene scene = new Scene(root, 1280, 720);
        background.fitWidthProperty().bind(scene.widthProperty());
        background.fitHeightProperty().bind(scene.heightProperty());

        return scene;
    }

    private VBox createDifficultyCard(String title, String description) {

        Button button = new Button(title);
        button.setPrefWidth(420);
    
        button.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #5a0000, #2b0000);" +
            "-fx-text-fill: #ff2b2b;" +
            "-fx-font-size: 34px;" +
            "-fx-font-family: 'Chiller';" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: rgba(255,0,0,0.4);" +
            "-fx-border-width: 1.5;" +
            "-fx-padding: 10 20 10 20;"
        );
    
        Label desc = new Label(description);
        desc.setWrapText(true);
        desc.setMaxWidth(500);
    
        desc.setStyle(
            "-fx-text-fill: rgba(230,230,230,0.85);" +
            "-fx-font-size: 16px;" +
            "-fx-font-family: 'Arial';" +
            "-fx-alignment: center;" +
            "-fx-text-alignment: center;"
        );
    
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.getChildren().addAll(button, desc);
    
        card.setMaxWidth(560);
    
        card.setStyle(
            "-fx-background-color: rgba(0,0,0,0.65);" +
            "-fx-background-radius: 18;" +
            "-fx-border-radius: 18;" +
            "-fx-border-color: rgba(255,0,0,0.25);" +
            "-fx-border-width: 1.2;" +
            "-fx-padding: 18 22 18 22;"
        );
        
        button.setOnMouseEntered(e ->
            button.setStyle(button.getStyle() +
                "-fx-effect: dropshadow(gaussian, red, 15, 0.6, 0, 0);")
        );
        
        button.setOnMouseExited(e ->
            button.setStyle(button.getStyle().replace(
                "-fx-effect: dropshadow(gaussian, red, 15, 0.6, 0, 0);", ""))
        );
        return card;
    }

    public Scene buildInstructionsScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");

        Label title = new Label("Instructions");
        title.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 64px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");

        VBox topBox = new VBox(title);
        topBox.setAlignment(Pos.CENTER);
        topBox.setStyle("-fx-padding: 30 0 20 0;");
        root.setTop(topBox);

        VBox card = new VBox(16);
        card.setMaxWidth(820);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: rgba(255,255,255,0.12);" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 22 26 22 26;"
        );

        Label howToPlay = new Label("How to play");
        howToPlay.setStyle("-fx-text-fill: #ff9999; -fx-font-size: 22px; -fx-font-family: Arial; -fx-font-weight: bold;");

        VBox bullets = new VBox(8);
        bullets.getChildren().addAll(
                createInstructionText("• Switch cameras with < and >."),
                createInstructionText("• Watch for changes. When you see one, press REPORT ANOMALY and choose the type."),
                createInstructionText("• Wrong reports increase THREAT. Too much THREAT triggers a jumpscare."),
                createInstructionText("• Anomalies begin spawning at 12:30 AM.")
        );

        Label anomalyTitle = new Label("Anomaly types");
        anomalyTitle.setStyle("-fx-text-fill: #ff9999; -fx-font-size: 22px; -fx-font-family: Arial; -fx-font-weight: bold;");

        VBox anomalyList = new VBox(6);
        anomalyList.getChildren().addAll(
                createInstructionText("• Missing Object — something is gone."),
                createInstructionText("• Object Displacement — something moved."),
                createInstructionText("• Shadowy Figure — a dark figure appears."),
                createInstructionText("• Intruder — a person-like entity appears."),
                createInstructionText("• Strange Imagery — something that shouldn't exist."),
                createInstructionText("• Demonic — unnatural / red demonic presence."),
                createInstructionText("• Extra Object — a new object appears."),
                createInstructionText("• Audio Disturbance — abnormal sound in a room.")
        );

        Label tip = new Label("Tip: Memorize each room’s normal state before 12:30 AM.");
        tip.setStyle("-fx-text-fill: #dddddd; -fx-font-size: 16px; -fx-font-family: Arial;");

        card.getChildren().addAll(howToPlay, bullets, anomalyTitle, anomalyList, tip);

        VBox centerBox = new VBox(card);
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

