package com.horrorgame.fx.ui;

import com.horrorgame.fx.logic.GameActions;
import com.horrorgame.fx.logic.ThreatManager;
import java.net.URL;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

/**
 * Builds and manages the in-game JavaFX scene (UI + media playback).
 * Game rules/timers live in {@code GameController}.
 */
public class GameSceneController implements GameView {

    private final GameActions actions;

    private MediaPlayer mediaPlayer;
    private MediaView mediaView;

    private StackPane gameRoot;
    private StackPane reportOverlay;
    private StackPane pauseOverlay;
    private StackPane gameOverOverlay;

    private Label timeLabelGame;
    private Label roomLabelGame;
    private Label threatLabelGame;
    private ProgressBar threatBarGame;

    private Button pauseButtonGame;
    private Button reportButtonGame;
    private Label reportCooldownLabel;

    private Button prevButton;
    private Button nextButton;

    private Label reportStatusLabel;
    private Button[] reportTypeButtons;

    private static final String[] REPORT_ANOMALY_TYPES = {
            "Missing Object",
            "Object Displacement",
            "Shadowy Figure",
            "Intruder",
            "Strange Imagery",
            "Demonic",
            "Extra Object",
            "Audio Disturbance"
    };

    public GameSceneController(GameActions actions, ThreatManager threatManager) {
        this.actions = actions;
    }

    public Scene buildGameScene() {
        mediaView = new MediaView();
        mediaView.setPreserveRatio(false);

        StackPane root = new StackPane();
        gameRoot = root;
        root.setStyle("-fx-background-color: black;");
        root.getChildren().add(mediaView);

        BorderPane overlay = new BorderPane();
        overlay.setPickOnBounds(false);

        timeLabelGame = new Label("12:00 AM");
        timeLabelGame.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 32px; -fx-font-family: Arial; -fx-font-weight: bold;");

        roomLabelGame = new Label();
        roomLabelGame.setStyle(
                "-fx-text-fill: #e6e6e6;" +
                "-fx-font-size: 16px;" +
                "-fx-font-family: Arial;" +
                "-fx-background-color: rgba(255,255,255,0.08);" +
                "-fx-padding: 6 12 6 12;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(255,255,255,0.18);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 12;"
        );

        threatLabelGame = new Label();
        threatLabelGame.setStyle("-fx-text-fill: #ffcc66; -fx-font-size: 18px; -fx-font-family: Arial; -fx-font-weight: bold;");

        threatBarGame = new ProgressBar(0);
        threatBarGame.setPrefWidth(140);
        threatBarGame.setPrefHeight(10);
        threatBarGame.setMinHeight(10);
        threatBarGame.setMaxHeight(10);
        threatBarGame.setStyle(
                "-fx-accent: #ff5555;" +
                "-fx-control-inner-background: rgba(255,255,255,0.12);" +
                "-fx-background-insets: 0;" +
                "-fx-background-radius: 6;"
        );
        // Keep internally but hide visually (per UX request)
        threatBarGame.setManaged(false);
        threatBarGame.setVisible(false);

        pauseButtonGame = new Button("PAUSE");
        styleControlButton(pauseButtonGame);
        pauseButtonGame.setOnAction(e -> actions.onPauseRequested());

        HBox topContainer = new HBox(24, timeLabelGame, roomLabelGame, threatLabelGame);
        topContainer.setPadding(new javafx.geometry.Insets(12, 24, 12, 24));
        topContainer.setStyle(
                "-fx-background-color: rgba(0,0,0,0.72);" +
                "-fx-background-radius: 0 0 16 0;"
        );

        HBox topRight = new HBox(pauseButtonGame);
        topRight.setAlignment(Pos.TOP_RIGHT);
        HBox.setMargin(pauseButtonGame, new javafx.geometry.Insets(16, 24, 0, 24));

        BorderPane topOverlay = new BorderPane();
        topOverlay.setLeft(topContainer);
        topOverlay.setRight(topRight);
        topOverlay.setPadding(new javafx.geometry.Insets(8, 0, 0, 0));
        overlay.setTop(topOverlay);

        prevButton = new Button("<");
        nextButton = new Button(">");
        reportButtonGame = new Button("REPORT ANOMALY");
        reportCooldownLabel = new Label("");
        reportCooldownLabel.setStyle("-fx-text-fill: #ffaaaa; -fx-font-size: 14px; -fx-font-family: Arial;");

        styleNavButton(prevButton);
        styleNavButton(nextButton);
        styleControlButton(reportButtonGame);

        reportButtonGame.setStyle(
                "-fx-background-color: linear-gradient(#aa0000,#400000);" +
                "-fx-text-fill: #ffdede;" +
                "-fx-font-size: 22px;" +
                "-fx-font-family: 'Chiller';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 18;" +
                "-fx-effect: dropshadow(gaussian, rgba(255,0,0,0.6), 20, 0.4, 0, 0);"
        );
        reportButtonGame.setPrefWidth(320);

        prevButton.setOnAction(e -> actions.onPrevRoom());
        nextButton.setOnAction(e -> actions.onNextRoom());
        reportButtonGame.setOnAction(e -> actions.onReportRequested());

        VBox leftBox = new VBox(prevButton);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        leftBox.setStyle("-fx-padding: 0 0 0 30;");

        VBox rightBox = new VBox(nextButton);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setStyle("-fx-padding: 0 30 0 0;");

        overlay.setLeft(leftBox);
        overlay.setRight(rightBox);

        VBox reportButtonContainer = new VBox(4);
        reportButtonContainer.setAlignment(Pos.CENTER);
        reportButtonContainer.getChildren().addAll(reportCooldownLabel, reportButtonGame);
        HBox bottomBar = new HBox(reportButtonContainer);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-padding: 0 0 40 0;");
        overlay.setBottom(bottomBar);

        StackPane vignette = new StackPane();
        vignette.setMouseTransparent(true);
        vignette.setStyle(
                "-fx-background-color: radial-gradient(center 50% 50%, radius 80%, " +
                "rgba(0,0,0,0.0), rgba(0,0,0,0.75));"
        );

        root.getChildren().add(vignette);
        root.getChildren().add(overlay);

        Scene scene = new Scene(root, 1280, 720);
        mediaView.fitWidthProperty().bind(scene.widthProperty());
        mediaView.fitHeightProperty().bind(scene.heightProperty());
        return scene;
    }

    @Override
    public void setTimeText(String text) {
        if (timeLabelGame != null) {
            timeLabelGame.setText(text);
        }
    }

    @Override
    public void setRoomText(String text) {
        if (roomLabelGame != null) {
            roomLabelGame.setText(text);
        }
    }

    @Override
    public void setThreat(ThreatManager.ThreatViewModel vm, double progress) {
        if (threatLabelGame != null && vm != null) {
            threatLabelGame.setText(vm.labelText());
            threatLabelGame.setStyle(vm.css());
        }
        if (threatBarGame != null) {
            threatBarGame.setProgress(progress);
        }
    }

    @Override
    public void playLoopingMedia(String resourcePath) {
        if (resourcePath == null) {
            stopMedia();
            return;
        }
        URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            stopMedia();
            return;
        }
        String uri = resource.toExternalForm();
        stopMedia();
        Media media = new Media(uri);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();
    }

    @Override
    public void playOnceMedia(String resourcePath, Runnable onFinished) {
        if (resourcePath == null) {
            if (onFinished != null) {
                Platform.runLater(onFinished);
            }
            return;
        }
        URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            if (onFinished != null) {
                Platform.runLater(onFinished);
            }
            return;
        }
        String uri = resource.toExternalForm();
        stopMedia();
        Media media = new Media(uri);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(1);
        mediaView.setMediaPlayer(mediaPlayer);
        if (onFinished != null) {
            mediaPlayer.setOnEndOfMedia(() -> Platform.runLater(onFinished));
        }
        mediaPlayer.play();
    }

    @Override
    public void stopMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    @Override
    public void pauseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void resumeMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    @Override
    public void setReportButtonDisabled(boolean disabled) {
        if (reportButtonGame != null) {
            reportButtonGame.setDisable(disabled);
        }
    }

    @Override
    public void setReportCooldownText(String text) {
        if (reportCooldownLabel != null) {
            reportCooldownLabel.setText(text == null ? "" : text);
        }
    }

    @Override
    public void setNavButtonsDisabled(boolean disabled) {
        if (prevButton != null) {
            prevButton.setDisable(disabled);
        }
        if (nextButton != null) {
            nextButton.setDisable(disabled);
        }
    }

    @Override
    public void showReportOverlay() {
        if (gameRoot == null) {
            return;
        }
        if (reportOverlay == null) {
            reportOverlay = new StackPane();
            reportOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.75);");
            reportOverlay.prefWidthProperty().bind(gameRoot.widthProperty());
            reportOverlay.prefHeightProperty().bind(gameRoot.heightProperty());
            reportOverlay.setMinSize(0, 0);

            VBox box = new VBox(15);
            box.setAlignment(Pos.CENTER);
            box.setStyle("-fx-padding: 40;");

            Label title = new Label("CLASSIFY ANOMALY");
            title.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 32px; -fx-font-family: Arial; -fx-font-weight: bold;");
            box.getChildren().add(title);

            reportStatusLabel = new Label("");
            reportStatusLabel.setStyle("-fx-text-fill: #ffaa44; -fx-font-size: 20px; -fx-font-family: Arial;");
            box.getChildren().add(reportStatusLabel);

            reportTypeButtons = new Button[REPORT_ANOMALY_TYPES.length];
            for (int i = 0; i < REPORT_ANOMALY_TYPES.length; i++) {
                final String selectedType = REPORT_ANOMALY_TYPES[i];
                Button b = new Button(selectedType);
                b.setStyle("-fx-background-color: #550000; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-family: Arial;");
                b.setPrefWidth(400);
                b.setOnAction(e -> actions.onReportTypeSelected(selectedType));
                reportTypeButtons[i] = b;
                box.getChildren().add(b);
            }

            Button cancel = new Button("CANCEL");
            cancel.setStyle("-fx-background-color: #444444; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-family: Arial;");
            cancel.setPrefWidth(400);
            cancel.setOnAction(e -> actions.onReportCancelled());
            box.getChildren().add(cancel);

            reportOverlay.getChildren().add(box);
        }
        setReportStatus("", "-fx-text-fill: #ffaa44; -fx-font-size: 20px; -fx-font-family: Arial;");
        setReportTypeButtonsEnabled(true);
        if (!gameRoot.getChildren().contains(reportOverlay)) {
            gameRoot.getChildren().add(reportOverlay);
        }
        reportOverlay.setVisible(true);
    }

    @Override
    public void hideReportOverlay() {
        if (gameRoot != null && reportOverlay != null) {
            gameRoot.getChildren().remove(reportOverlay);
        }
    }

    @Override
    public void setReportStatus(String text, String css) {
        if (reportStatusLabel != null) {
            reportStatusLabel.setText(text == null ? "" : text);
            if (css != null) {
                reportStatusLabel.setStyle(css);
            }
        }
    }

    @Override
    public void setReportTypeButtonsEnabled(boolean enabled) {
        if (reportTypeButtons != null) {
            for (Button b : reportTypeButtons) {
                b.setDisable(!enabled);
            }
        }
    }

    @Override
    public void showPauseOverlay() {
        if (gameRoot == null) {
            return;
        }
        if (pauseOverlay == null) {
            pauseOverlay = new StackPane();
            pauseOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.75);");

            VBox box = new VBox(20);
            box.setAlignment(Pos.CENTER);

            Button resume = new Button("RESUME");
            resume.setStyle("-fx-background-color: #400000; -fx-text-fill: red; -fx-font-size: 24px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");
            resume.setPrefWidth(260);
            resume.setOnAction(e -> actions.onResumeFromPause());

            Button mainMenu = new Button("MAIN MENU");
            mainMenu.setStyle("-fx-background-color: #300000; -fx-text-fill: red; -fx-font-size: 24px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");
            mainMenu.setPrefWidth(260);
            mainMenu.setOnAction(e -> actions.onReturnToMainMenuFromPause());

            box.getChildren().addAll(resume, mainMenu);
            pauseOverlay.getChildren().add(box);
        }
        if (!gameRoot.getChildren().contains(pauseOverlay)) {
            gameRoot.getChildren().add(pauseOverlay);
        }
        pauseOverlay.setVisible(true);
    }

    @Override
    public void hidePauseOverlay() {
        if (gameRoot != null && pauseOverlay != null) {
            gameRoot.getChildren().remove(pauseOverlay);
        }
    }

    @Override
    public void showGameOverOverlay(boolean win) {
        if (gameRoot == null) {
            return;
        }
        if (gameOverOverlay != null) {
            gameRoot.getChildren().remove(gameOverOverlay);
        }

        gameOverOverlay = new StackPane();
        gameOverOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.86);");
        gameOverOverlay.setPickOnBounds(true);

        VBox box = new VBox(18);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(24, 32, 24, 32));

        String message = win
                ? "Wake up, it's time for another day.\nYOU WIN!!!"
                : "YOU DID NOT SURVIVE UNTIL 6:00 AM\nGAME OVER!";

        Label label = new Label(message);
        label.setStyle("-fx-text-fill: #ff6666; -fx-font-size: 30px; -fx-font-family: Arial; -fx-font-weight: bold;");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button mainMenu = new Button("MAIN MENU");
        mainMenu.setStyle(
                "-fx-background-color: linear-gradient(#7a0000,#2a0000);" +
                "-fx-text-fill: #ffdede;" +
                "-fx-font-size: 20px;" +
                "-fx-font-family: 'Chiller';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 14;"
        );
        mainMenu.setPrefWidth(280);
        mainMenu.setOnAction(e -> actions.onReturnToMainMenuFromGameOver());

        Button playAgain = new Button("PLAY AGAIN");
        playAgain.setStyle(
                "-fx-background-color: rgba(255,255,255,0.12);" +
                "-fx-text-fill: #ffdede;" +
                "-fx-font-size: 20px;" +
                "-fx-font-family: 'Chiller';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: rgba(255,255,255,0.18);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 14;"
        );
        playAgain.setPrefWidth(280);
        playAgain.setOnAction(e -> actions.onPlayAgainFromGameOver());

        HBox buttons = new HBox(18, playAgain, mainMenu);
        buttons.setAlignment(Pos.CENTER);

        box.getChildren().addAll(label, buttons);
        gameOverOverlay.getChildren().add(box);
        gameOverOverlay.setOpacity(0.0);
        box.setScaleX(0.98);
        box.setScaleY(0.98);
        gameRoot.getChildren().add(gameOverOverlay);

        FadeTransition fade = new FadeTransition(Duration.millis(320), gameOverOverlay);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(320), box);
        scale.setFromX(0.98);
        scale.setFromY(0.98);
        scale.setToX(1.0);
        scale.setToY(1.0);

        new ParallelTransition(fade, scale).play();
    }

    @Override
    public void hideGameOverOverlay() {
        if (gameRoot != null && gameOverOverlay != null) {
            gameRoot.getChildren().remove(gameOverOverlay);
        }
    }

    private void styleNavButton(Button button) {
        button.setStyle(
                "-fx-background-color: rgba(30,0,0,0.85);" +
                "-fx-text-fill: #ff4444;" +
                "-fx-font-size: 32px;" +
                "-fx-font-family: Arial;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 40;" +
                "-fx-border-color: #ff7777;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 40;"
        );
        button.setPrefSize(80, 80);
    }

    private void styleControlButton(Button button) {
        button.setStyle(
                "-fx-background-color: #300000;" +
                "-fx-text-fill: #ffcccc;" +
                "-fx-font-size: 18px;" +
                "-fx-font-family: Arial;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 14;"
        );
        button.setPrefWidth(220);
    }
}

