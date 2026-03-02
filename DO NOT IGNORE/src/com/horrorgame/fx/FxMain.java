package com.horrorgame.fx;

import com.horrorgame.audio.SoundManager;
import com.horrorgame.game.RoomMediaLibrary;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FxMain extends Application {

    private enum DifficultyLevel {
        EASY,
        MEDIUM,
        HARD
    }

    private final Random random = new Random();
    private RoomMediaLibrary mediaLibrary;
    private List<String> roomKeys;
    private int currentRoomIndex = 0;

    private MediaPlayer mediaPlayer;
    private MediaView mediaView;

    private Stage primaryStage;
    private Scene mainMenuScene;
    private Scene difficultyScene;
    private Scene instructionsScene;
    private Scene gameScene;

    private Timeline gameTimeline;
    private Timeline anomalyTimeline;
    private Timeline deathCountdownTimeline;
    private int gameMinutesTotal = 0;
    private boolean paused = false;
    private Label timeLabelGame;
    private Label roomLabelGame;
    private int secondsOnCurrentRoom = 0;
    private int threatLevel = 0;
    private Label threatLabelGame;
    private Button pauseButtonGame;

    private StackPane gameRoot;
    private StackPane reportOverlay;
    private Label reportStatusLabel;
    private Button[] reportTypeButtons;
    private boolean reportSubmissionInProgress = false;
    private StackPane pauseOverlay;
    private Button reportButtonGame;
    private Label reportCooldownLabel;
    private Timeline reportCooldownTimeline;
    private static final int REPORT_COOLDOWN_SECONDS = 5;
    private boolean deathCountdownActive = false;

    private static class RoomState {
        boolean hasAnomaly;
        RoomMediaLibrary.AnomalyType anomalyType;
        int anomalySecondsAlive;
        boolean penaltyApplied;
    }

    private final Map<String, RoomState> roomStates = new HashMap<>();

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

    private DifficultyLevel currentDifficulty = DifficultyLevel.MEDIUM;
    private static final String CAMERA_TRANSITION_PATH = "/com/horrorgame/assets/transition/camera_transition_sfx.mp4";
    private boolean cameraTransitionPlaying = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        mediaLibrary = RoomMediaLibrary.load(FxMain.class);
        roomKeys = mediaLibrary.getRoomKeys();
        if (roomKeys.isEmpty()) {
            throw new IllegalStateException("No room MP4s found under /com/horrorgame/assets/images/rooms/");
        }

        for (String key : roomKeys) {
            roomStates.put(key, new RoomState());
        }

        currentRoomIndex = random.nextInt(roomKeys.size());

        mainMenuScene = buildMainMenuScene();
        difficultyScene = buildDifficultyScene();
        instructionsScene = buildInstructionsScene();
        gameScene = buildGameScene();

        primaryStage.setTitle("DO NOT IGNORE");
        primaryStage.setScene(mainMenuScene);
        applyFullscreenBounds();
        primaryStage.show();
    }

    private void applyFullscreenBounds() {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setResizable(false);
    }

    private Scene buildMainMenuScene() {
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
            primaryStage.setScene(difficultyScene);
            applyFullscreenBounds();
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

    private Scene buildDifficultyScene() {
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
            primaryStage.setScene(instructionsScene);
        });
        mediumButton.setOnAction(e -> {
            SoundManager.playSound("/com/horrorgame/assets/audio/hover_click.WAV");
            currentDifficulty = DifficultyLevel.MEDIUM;
            primaryStage.setScene(instructionsScene);
        });
        hardButton.setOnAction(e -> {
            SoundManager.playSound("/com/horrorgame/assets/audio/hover_click.WAV");
            currentDifficulty = DifficultyLevel.HARD;
            primaryStage.setScene(instructionsScene);
        });

        Button backButton = new Button("Back");
        styleBackButton(backButton);
        backButton.setOnAction(e -> {
            SoundManager.playSound("/com/horrorgame/assets/audio/hover_click.WAV");
            primaryStage.setScene(mainMenuScene);
            applyFullscreenBounds();
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

    private Scene buildInstructionsScene() {
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
            primaryStage.setScene(difficultyScene);
            applyFullscreenBounds();
        });

        Button startShiftButton = new Button("Start");
        styleMenuButton(startShiftButton);
        startShiftButton.setOnAction(e -> {
            SoundManager.stopLoop();
            SoundManager.playLoop("/com/horrorgame/assets/audio/VHSNoise.wav");
            stopMedia();
            hideReportOverlay();
            resetGameState();
            currentRoomIndex = random.nextInt(roomKeys.size());
            playCurrentRoomNormal();
            startGameLoop();
            primaryStage.setScene(gameScene);
            applyFullscreenBounds();
        });

        HBox bottomButtons = new HBox(20, backButton, startShiftButton);
        bottomButtons.setAlignment(Pos.CENTER);
        bottomButtons.setStyle("-fx-padding: 20 0 40 0;");
        root.setBottom(bottomButtons);

        return new Scene(root, 1280, 720);
    }

    private Scene buildGameScene() {
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
        roomLabelGame.setStyle("-fx-text-fill: #dddddd; -fx-font-size: 18px; -fx-font-family: Arial;");

        threatLabelGame = new Label();
        threatLabelGame.setStyle("-fx-text-fill: #ffcc66; -fx-font-size: 18px; -fx-font-family: Arial; -fx-font-weight: bold;");

        pauseButtonGame = new Button("PAUSE");
        styleControlButton(pauseButtonGame);
        pauseButtonGame.setOnAction(e -> {
            showPauseOverlay();
        });

        HBox topBar = new HBox(30, timeLabelGame, roomLabelGame, threatLabelGame);
        HBox topRight = new HBox(pauseButtonGame);
        topRight.setAlignment(Pos.TOP_RIGHT);
        HBox topContainer = new HBox();
        topContainer.setPadding(new javafx.geometry.Insets(20, 20, 0, 20));
        topContainer.setSpacing(30);
        topContainer.getChildren().addAll(timeLabelGame, roomLabelGame, threatLabelGame);
        HBox.setMargin(pauseButtonGame, new javafx.geometry.Insets(0, 0, 0, 40));

        BorderPane topOverlay = new BorderPane();
        topOverlay.setLeft(topContainer);
        topOverlay.setRight(pauseButtonGame);
        overlay.setTop(topOverlay);

        Button prevButton = new Button("<");
        Button nextButton = new Button(">");
        reportButtonGame = new Button("REPORT ANOMALY");
        reportCooldownLabel = new Label("");
        reportCooldownLabel.setStyle("-fx-text-fill: #ff8888; -fx-font-size: 14px; -fx-font-family: Arial;");

        styleNavButton(prevButton);
        styleNavButton(nextButton);
        styleControlButton(reportButtonGame);

        prevButton.setOnAction(e -> playCameraTransitionAndSwitchRoom(-1));
        nextButton.setOnAction(e -> playCameraTransitionAndSwitchRoom(1));

        reportButtonGame.setOnAction(e -> showReportOverlay());

        VBox leftBox = new VBox(prevButton);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        leftBox.setStyle("-fx-padding: 0 0 0 40;");

        VBox rightBox = new VBox(nextButton);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setStyle("-fx-padding: 0 40 0 0;");

        overlay.setLeft(leftBox);
        overlay.setRight(rightBox);

        VBox reportButtonContainer = new VBox(4);
        reportButtonContainer.setAlignment(Pos.CENTER);
        reportButtonContainer.getChildren().addAll(reportCooldownLabel, reportButtonGame);
        HBox bottomBar = new HBox(reportButtonContainer);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-padding: 0 0 40 0;");
        overlay.setBottom(bottomBar);

        root.getChildren().add(overlay);

        Scene scene = new Scene(root, 1280, 720);
        mediaView.fitWidthProperty().bind(scene.widthProperty());
        mediaView.fitHeightProperty().bind(scene.heightProperty());

        playCurrentRoomNormal();
        updateRoomLabel();

        return scene;
    }

    private void styleMenuButton(Button button) {
        button.setStyle("-fx-background-color: #400000; -fx-text-fill: red; -fx-font-size: 32px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");
        button.setPrefWidth(260);
    }

    private void styleBackButton(Button button) {
        button.setStyle("-fx-background-color: #300000; -fx-text-fill: red; -fx-font-size: 22px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");
        button.setPrefWidth(160);
    }

    private Label createInstructionText(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-family: Arial;");
        return label;
    }

    private void styleNavButton(Button button) {
        button.setStyle("-fx-background-color: #200000; -fx-text-fill: #ff4444; -fx-font-size: 36px; -fx-font-family: Arial; -fx-font-weight: bold;");
        button.setPrefSize(80, 80);
    }

    private void styleControlButton(Button button) {
        button.setStyle("-fx-background-color: #300000; -fx-text-fill: #ffcccc; -fx-font-size: 18px; -fx-font-family: Arial; -fx-font-weight: bold;");
        button.setPrefWidth(220);
    }

    private void playCameraTransitionAndSwitchRoom(int delta) {
        if (cameraTransitionPlaying || roomKeys.isEmpty()) {
            return;
        }
        cameraTransitionPlaying = true;

        int targetIndex = (currentRoomIndex + delta + roomKeys.size()) % roomKeys.size();

        URL resource = getClass().getResource(CAMERA_TRANSITION_PATH);
        if (resource == null) {
            // Fallback: no transition video, just switch immediately.
            currentRoomIndex = targetIndex;
            playCurrentRoomNormal();
            updateRoomLabel();
            secondsOnCurrentRoom = 0;
            cameraTransitionPlaying = false;
            return;
        }

        String uri = resource.toExternalForm();
        stopMedia();
        Media media = new Media(uri);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(1);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.setOnEndOfMedia(() -> Platform.runLater(() -> {
            currentRoomIndex = targetIndex;
            playCurrentRoomNormal();
            updateRoomLabel();
            secondsOnCurrentRoom = 0;
            cameraTransitionPlaying = false;
        }));
        mediaPlayer.play();
    }

    private void playCurrentRoomNormal() {
        if (roomKeys.isEmpty()) {
            return;
        }
        String roomKey = roomKeys.get(currentRoomIndex);
        RoomState state = roomStates.get(roomKey);
        String mediaPath;
        if (state != null && state.hasAnomaly && state.anomalyType != null) {
            mediaPath = mediaLibrary.getRandomAnomaly(roomKey, state.anomalyType);
        } else {
            mediaPath = mediaLibrary.getRandomNormal(roomKey);
        }
        playMedia(mediaPath);
    }

    private void resetGameState() {
        for (String key : roomKeys) {
            RoomState state = roomStates.get(key);
            if (state != null) {
                state.hasAnomaly = false;
                state.anomalyType = null;
                state.anomalySecondsAlive = 0;
                state.penaltyApplied = false;
            }
        }
    }

    private void startGameLoop() {
        gameMinutesTotal = 0;
        paused = false;
        secondsOnCurrentRoom = 0;
        threatLevel = 0;
        deathCountdownActive = false;
        updateTimeLabel();
        updateThreatLabel();
        if (gameTimeline != null) {
            gameTimeline.stop();
        }
        if (anomalyTimeline != null) {
            anomalyTimeline.stop();
        }
        if (deathCountdownTimeline != null) {
            deathCountdownTimeline.stop();
        }
        gameTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (paused) {
                return;
            }
            gameMinutesTotal++;
            updateTimeLabel();
            secondsOnCurrentRoom++;
            if (secondsOnCurrentRoom >= 20) {
                secondsOnCurrentRoom = 0;
                if (!roomKeys.isEmpty()) {
                    currentRoomIndex = (currentRoomIndex + 1) % roomKeys.size();
                    playCurrentRoomNormal();
                    updateRoomLabel();
                    // Simple threat mechanic: each automatic room change slightly increases threat.
                    increaseThreat(3);
                }
            }
            if (gameMinutesTotal >= 360) {
                gameTimeline.stop();
                if (anomalyTimeline != null) {
                    anomalyTimeline.stop();
                }
                if (deathCountdownTimeline != null) {
                    deathCountdownTimeline.stop();
                }
                Platform.runLater(() -> {
                    showGameOverOverlay(true);
                });
            }
        }));
        gameTimeline.setCycleCount(Timeline.INDEFINITE);
        gameTimeline.play();

        anomalyTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> tickAnomalies()));
        anomalyTimeline.setCycleCount(Timeline.INDEFINITE);
        anomalyTimeline.play();
    }

    private void updateTimeLabel() {
        int h = gameMinutesTotal / 60;
        int m = gameMinutesTotal % 60;
        int dispH = (12 + h) % 12;
        if (dispH == 0) {
            dispH = 12;
        }
        String suffix = "AM";
        if (timeLabelGame != null) {
            timeLabelGame.setText(String.format("%d:%02d %s", dispH, m, suffix));
        }
    }

    private void increaseThreat(int amount) {
        threatLevel = Math.max(0, Math.min(100, threatLevel + amount));
        updateThreatLabel();
        if (threatLevel >= 100) {
            startDeathCountdown();
        } else {
            stopDeathCountdown();
        }
    }

    private void updateThreatLabel() {
        if (threatLabelGame == null) {
            return;
        }
        String text;
        if (threatLevel < 25) {
            text = "THREAT: LOW";
            threatLabelGame.setStyle("-fx-text-fill: #88ff88; -fx-font-size: 18px; -fx-font-family: Arial; -fx-font-weight: bold;");
        } else if (threatLevel < 50) {
            text = "THREAT: UNSTABLE";
            threatLabelGame.setStyle("-fx-text-fill: #ffff88; -fx-font-size: 18px; -fx-font-family: Arial; -fx-font-weight: bold;");
        } else if (threatLevel < 75) {
            text = "THREAT: HIGH";
            threatLabelGame.setStyle("-fx-text-fill: #ffaa44; -fx-font-size: 18px; -fx-font-family: Arial; -fx-font-weight: bold;");
        } else {
            text = "THREAT: CRITICAL";
            threatLabelGame.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 18px; -fx-font-family: Arial; -fx-font-weight: bold;");
        }
        threatLabelGame.setText(text + " (" + threatLevel + "%)");
    }

    private void tickAnomalies() {
        if (paused) {
            return;
        }
        if (gameMinutesTotal < 30) {
            return;
        }
        if (roomKeys.isEmpty()) {
            return;
        }
        // Age existing anomalies and apply miss penalties.
        int delaySeconds = getDelayBeforePenaltySeconds();
        for (String key : roomKeys) {
            RoomState state = roomStates.get(key);
            if (state != null && state.hasAnomaly) {
                if (!state.penaltyApplied) {
                    state.anomalySecondsAlive++;
                    if (state.anomalySecondsAlive >= delaySeconds) {
                        increaseThreat(getMissAnomalyPenalty());
                        state.penaltyApplied = true;
                        state.hasAnomaly = false;
                        state.anomalyType = null;
                        state.anomalySecondsAlive = 0;
                        if (key.equals(roomKeys.get(currentRoomIndex))) {
                            playCurrentRoomNormal();
                        }
                    }
                }
            }
        }

        double spawnChance = getAnomalySpawnChance();
        if (random.nextDouble() > spawnChance) {
            return;
        }
        // Choose a room that is NOT the currently viewed one and that has no anomaly.
        int roomIndex = -1;
        for (int attempts = 0; attempts < roomKeys.size(); attempts++) {
            int candidate = random.nextInt(roomKeys.size());
            if (candidate == currentRoomIndex) {
                continue;
            }
            String candidateKey = roomKeys.get(candidate);
            RoomState candidateState = roomStates.get(candidateKey);
            if (candidateState == null || !candidateState.hasAnomaly) {
                roomIndex = candidate;
                break;
            }
        }
        if (roomIndex < 0) {
            return;
        }
        String roomKey = roomKeys.get(roomIndex);
        RoomState state = roomStates.get(roomKey);
        if (state == null) {
            state = new RoomState();
            roomStates.put(roomKey, state);
        }
        List<RoomMediaLibrary.AnomalyType> available = mediaLibrary.getAvailableAnomalyTypes(roomKey);
        if (available.isEmpty()) {
            return;
        }
        RoomMediaLibrary.AnomalyType type = available.get(random.nextInt(available.size()));
        state.hasAnomaly = true;
        state.anomalyType = type;
        state.anomalySecondsAlive = 0;
        state.penaltyApplied = false;
    }

    private double getAnomalySpawnChance() {
        switch (currentDifficulty) {
            case EASY:
                return 0.3;
            case HARD:
                return 0.8;
            case MEDIUM:
            default:
                return 0.5;
        }
    }

    private int getMissAnomalyPenalty() {
        switch (currentDifficulty) {
            case EASY:
                return 5;
            case HARD:
                return 15;
            case MEDIUM:
            default:
                return 10;
        }
    }

    private int getCorrectReportDelta() {
        switch (currentDifficulty) {
            case EASY:
                return -5;
            case HARD:
                return 0;
            case MEDIUM:
            default:
                return -3;
        }
    }

    private int getDelayBeforePenaltySeconds() {
        switch (currentDifficulty) {
            case EASY:
                return 15;
            case HARD:
                return 5;
            case MEDIUM:
            default:
                return 10;
        }
    }

    private void showReportOverlay() {
        if (gameRoot == null || reportButtonGame == null) {
            return;
        }
        if (reportCooldownTimeline != null && reportCooldownTimeline.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            return;
        }
        if (reportSubmissionInProgress) {
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
                b.setDisable(false);
                b.setMouseTransparent(false);
                b.setFocusTraversable(true);
                b.setOnAction(e -> handleReport(selectedType));
                reportTypeButtons[i] = b;
                box.getChildren().add(b);
            }

            Button cancel = new Button("CANCEL");
            cancel.setStyle("-fx-background-color: #444444; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-family: Arial;");
            cancel.setPrefWidth(400);
            cancel.setOnAction(e -> hideReportOverlay(false));
            box.getChildren().add(cancel);

            reportOverlay.getChildren().add(box);
        }
        reportStatusLabel.setText("");
        setReportTypeButtonsEnabled(true);
        if (!gameRoot.getChildren().contains(reportOverlay)) {
            gameRoot.getChildren().add(reportOverlay);
        }
        reportOverlay.setVisible(true);
    }

    private void setReportTypeButtonsEnabled(boolean enabled) {
        if (reportTypeButtons != null) {
            for (Button b : reportTypeButtons) {
                b.setDisable(!enabled);
            }
        }
    }

    private void hideReportOverlay() {
        hideReportOverlay(true);
    }

    private void hideReportOverlay(boolean startCooldown) {
        reportSubmissionInProgress = false;
        if (gameRoot != null && reportOverlay != null) {
            gameRoot.getChildren().remove(reportOverlay);
        }
        if (startCooldown) {
            startReportCooldown();
        }
    }

    private void startReportCooldown() {
        if (reportButtonGame == null || reportCooldownLabel == null) {
            return;
        }
        if (reportCooldownTimeline != null) {
            reportCooldownTimeline.stop();
        }
        reportButtonGame.setDisable(true);
        final int[] secondsLeft = {REPORT_COOLDOWN_SECONDS};
        reportCooldownLabel.setText("Cooldown: " + secondsLeft[0] + "s");
        reportCooldownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsLeft[0]--;
            if (secondsLeft[0] <= 0) {
                reportCooldownTimeline.stop();
                reportButtonGame.setDisable(false);
                reportCooldownLabel.setText("");
            } else {
                reportCooldownLabel.setText("Cooldown: " + secondsLeft[0] + "s");
            }
        }));
        reportCooldownTimeline.setCycleCount(REPORT_COOLDOWN_SECONDS);
        reportCooldownTimeline.play();
    }

    private void showPauseOverlay() {
        if (gameRoot == null) {
            return;
        }
        paused = true;
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
        if (gameTimeline != null) {
            gameTimeline.pause();
        }
        if (anomalyTimeline != null) {
            anomalyTimeline.pause();
        }
        if (deathCountdownTimeline != null) {
            deathCountdownTimeline.pause();
        }
        if (pauseOverlay == null) {
            pauseOverlay = new StackPane();
            pauseOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.75);");

            VBox box = new VBox(20);
            box.setAlignment(Pos.CENTER);

            Button resume = new Button("RESUME");
            resume.setStyle("-fx-background-color: #400000; -fx-text-fill: red; -fx-font-size: 24px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");
            resume.setPrefWidth(260);
            resume.setOnAction(e -> hidePauseOverlay());

            Button mainMenu = new Button("MAIN MENU");
            mainMenu.setStyle("-fx-background-color: #300000; -fx-text-fill: red; -fx-font-size: 24px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");
            mainMenu.setPrefWidth(260);
            mainMenu.setOnAction(e -> {
                hidePauseOverlay();
                stopMedia();
                if (gameTimeline != null) {
                    gameTimeline.stop();
                }
                if (anomalyTimeline != null) {
                    anomalyTimeline.stop();
                }
                if (deathCountdownTimeline != null) {
                    deathCountdownTimeline.stop();
                }
                SoundManager.playLoop("/com/horrorgame/assets/audio/menu_music.WAV");
                primaryStage.setScene(mainMenuScene);
                applyFullscreenBounds();
            });

            box.getChildren().addAll(resume, mainMenu);
            pauseOverlay.getChildren().add(box);
        }
        if (!gameRoot.getChildren().contains(pauseOverlay)) {
            gameRoot.getChildren().add(pauseOverlay);
        }
        pauseOverlay.setVisible(true);
    }

    private void hidePauseOverlay() {
        paused = false;
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
        if (gameTimeline != null) {
            gameTimeline.play();
        }
        if (anomalyTimeline != null) {
            anomalyTimeline.play();
        }
        if (deathCountdownTimeline != null && deathCountdownActive) {
            deathCountdownTimeline.play();
        }
        if (gameRoot != null && pauseOverlay != null) {
            gameRoot.getChildren().remove(pauseOverlay);
        }
    }

    private void startDeathCountdown() {
        if (deathCountdownActive) {
            return;
        }
        deathCountdownActive = true;
        if (deathCountdownTimeline != null) {
            deathCountdownTimeline.stop();
        }
        deathCountdownTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            if (threatLevel >= 100) {
                triggerDeathJumpscare();
            }
        }));
        deathCountdownTimeline.setCycleCount(1);
        deathCountdownTimeline.play();
    }

    private void stopDeathCountdown() {
        deathCountdownActive = false;
        if (deathCountdownTimeline != null) {
            deathCountdownTimeline.stop();
        }
    }

    private void triggerDeathJumpscare() {
        stopDeathCountdown();
        if (roomKeys.isEmpty()) {
            showGameOverOverlay(false);
            return;
        }
        if (gameTimeline != null) {
            gameTimeline.stop();
        }
        if (anomalyTimeline != null) {
            anomalyTimeline.stop();
        }
        paused = true;
        currentRoomIndex = (currentRoomIndex + 1) % roomKeys.size();
        String roomKey = roomKeys.get(currentRoomIndex);
        String jumpscarePath = mediaLibrary.getRandomJumpscare(roomKey);
        if (jumpscarePath == null) {
            showGameOverOverlay(false);
            return;
        }
        URL resource = getClass().getResource(jumpscarePath);
        if (resource == null) {
            showGameOverOverlay(false);
            return;
        }
        String uri = resource.toExternalForm();
        stopMedia();
        Media media = new Media(uri);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(1);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.setOnEndOfMedia(() -> Platform.runLater(() -> showGameOverOverlay(false)));
        mediaPlayer.play();
    }

    private void showGameOverOverlay(boolean win) {
        if (gameRoot == null) {
            return;
        }
        stopMedia();
        if (gameTimeline != null) {
            gameTimeline.stop();
        }
        if (anomalyTimeline != null) {
            anomalyTimeline.stop();
        }
        if (deathCountdownTimeline != null) {
            deathCountdownTimeline.stop();
        }

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.85);");

        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);

        String message = win
                ? "Wake up, it's time for another day.\nYOU WIN!!!"
                : "YOU DID NOT SURVIVE UNTIL 6:00 AM\nGAME OVER!";

        Label label = new Label(message);
        label.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 28px; -fx-font-family: Arial; -fx-font-weight: bold;");

        Button mainMenu = new Button("MAIN MENU");
        mainMenu.setStyle("-fx-background-color: #300000; -fx-text-fill: red; -fx-font-size: 22px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");
        mainMenu.setPrefWidth(260);
        mainMenu.setOnAction(e -> {
            gameRoot.getChildren().remove(overlay);
            SoundManager.playLoop("/com/horrorgame/assets/audio/menu_music.WAV");
            primaryStage.setScene(mainMenuScene);
            applyFullscreenBounds();
        });

        Button playAgain = new Button("PLAY AGAIN");
        playAgain.setStyle("-fx-background-color: #300000; -fx-text-fill: red; -fx-font-size: 22px; -fx-font-family: 'Chiller'; -fx-font-weight: bold;");
        playAgain.setPrefWidth(260);
        playAgain.setOnAction(e -> {
            gameRoot.getChildren().remove(overlay);
            SoundManager.stopLoop();
            primaryStage.setScene(difficultyScene);
            applyFullscreenBounds();
        });

        box.getChildren().addAll(label, mainMenu, playAgain);
        overlay.getChildren().add(box);
        gameRoot.getChildren().add(overlay);
    }

    private void handleReport(String selectedLabel) {
        if (reportSubmissionInProgress) {
            return;
        }
        reportSubmissionInProgress = true;
        setReportTypeButtonsEnabled(false);

        if (roomKeys.isEmpty()) {
            reportStatusLabel.setText("Reporting...");
            reportStatusLabel.setStyle("-fx-text-fill: #ffaa44; -fx-font-size: 20px; -fx-font-family: Arial;");
            Timeline delay = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
                increaseThreat(getMissAnomalyPenalty());
                reportStatusLabel.setText("NO ANOMALY FOUND");
                reportStatusLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 20px; -fx-font-family: Arial;");
                Timeline doneDelay = new Timeline(new KeyFrame(Duration.millis(1200), ev -> hideReportOverlay()));
                doneDelay.setCycleCount(1);
                doneDelay.play();
            }));
            delay.setCycleCount(1);
            delay.play();
            return;
        }
        final int roomIndexAtReport = currentRoomIndex;
        String roomKey = roomKeys.get(roomIndexAtReport);
        RoomState state = roomStates.get(roomKey);
        RoomMediaLibrary.AnomalyType selectedType = mapLabelToAnomalyType(selectedLabel);

        // Show a unified "Reporting..." phase before revealing result.
        reportStatusLabel.setText("Reporting...");
        reportStatusLabel.setStyle("-fx-text-fill: #ffaa44; -fx-font-size: 20px; -fx-font-family: Arial;");

        if (state == null || !state.hasAnomaly || selectedType == null || state.anomalyType != selectedType) {
            Timeline delay = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
                increaseThreat(getMissAnomalyPenalty());
                reportStatusLabel.setText("NO ANOMALY FOUND");
                reportStatusLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 20px; -fx-font-family: Arial;");
                Timeline doneDelay = new Timeline(new KeyFrame(Duration.millis(1200), ev -> hideReportOverlay()));
                doneDelay.setCycleCount(1);
                doneDelay.play();
            }));
            delay.setCycleCount(1);
            delay.play();
            return;
        }

        final String roomKeyFinal = roomKey;
        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            RoomState s = roomStates.get(roomKeyFinal);
            if (s != null) {
                s.hasAnomaly = false;
                s.anomalyType = null;
                s.anomalySecondsAlive = 0;
                s.penaltyApplied = false;
            }
            increaseThreat(getCorrectReportDelta());
            if (currentRoomIndex == roomIndexAtReport) {
                playCurrentRoomNormal();
            }
            reportStatusLabel.setText("ANOMALY REMOVED");
            reportStatusLabel.setStyle("-fx-text-fill: #88ff88; -fx-font-size: 20px; -fx-font-family: Arial;");
            Timeline doneDelay = new Timeline(new KeyFrame(Duration.millis(1200), ev -> hideReportOverlay()));
            doneDelay.setCycleCount(1);
            doneDelay.play();
        }));
        delay.setCycleCount(1);
        delay.play();
    }

    private RoomMediaLibrary.AnomalyType mapLabelToAnomalyType(String label) {
        if (label == null) {
            return null;
        }
        switch (label) {
            case "Missing Object":
                return RoomMediaLibrary.AnomalyType.MISSING_OBJECT;
            case "Object Displacement":
                return RoomMediaLibrary.AnomalyType.OBJECT_DISPLACEMENT;
            case "Shadowy Figure":
                return RoomMediaLibrary.AnomalyType.SHADOWY_FIGURE;
            case "Intruder":
                return RoomMediaLibrary.AnomalyType.INTRUDER;
            case "Strange Imagery":
                return RoomMediaLibrary.AnomalyType.STRANGE_IMAGERY;
            case "Demonic":
                return RoomMediaLibrary.AnomalyType.DEMONIC;
            case "Extra Object":
                return RoomMediaLibrary.AnomalyType.EXTRA_OBJECT;
            case "Audio Disturbance":
                return RoomMediaLibrary.AnomalyType.AUDIO_DISTURBANCE;
            default:
                return null;
        }
    }

    private void playMedia(String resourcePath) {
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

    private void stopMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    private void updateRoomLabel(Label roomLabel) {
        if (roomKeys.isEmpty()) {
            roomLabel.setText("");
            return;
        }
        String key = roomKeys.get(currentRoomIndex);
        roomLabel.setText("Room: " + key);
    }

    private void updateRoomLabel() {
        if (roomLabelGame == null) {
            return;
        }
        if (roomKeys.isEmpty()) {
            roomLabelGame.setText("");
            return;
        }
        String key = roomKeys.get(currentRoomIndex);
        roomLabelGame.setText("Room: " + key);
    }
}

