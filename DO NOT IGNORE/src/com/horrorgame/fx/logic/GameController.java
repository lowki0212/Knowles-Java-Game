package com.horrorgame.fx.logic;

import com.horrorgame.fx.DifficultyLevel;
import com.horrorgame.fx.RoomState;
import com.horrorgame.fx.core.SceneManager;
import com.horrorgame.fx.ui.GameView;
import com.horrorgame.game.RoomMediaLibrary;
import java.util.List;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

/**
 * Core gameplay logic for the JavaFX version.
 * Owns game state + timers and drives the {@link GameView}.
 */
public class GameController implements GameActions {

    private static final int REPORT_COOLDOWN_SECONDS = 2;
    private static final String CAMERA_TRANSITION_PATH = "/com/horrorgame/assets/transition/camera_transition_sfx.mp4";

    private final RoomMediaLibrary mediaLibrary;
    private final List<String> roomKeys;
    private final ThreatManager threatManager;
    private final AnomalyManager anomalyManager;
    private final SceneManager sceneManager;
    private final Random random = new Random();

    private GameView view;

    private DifficultyLevel difficulty = DifficultyLevel.MEDIUM;

    private int currentRoomIndex = 0;
    private int gameMinutesTotal = 0;
    private int secondsOnCurrentRoom = 0;
    private int threatLevel = 0;

    private boolean paused = false;
    private boolean deathCountdownActive = false;
    private boolean reportSubmissionInProgress = false;
    private boolean cameraTransitionPlaying = false;
    private boolean jumpscareActive = false;

    private Timeline gameTimeline;
    private Timeline anomalyTimeline;
    private Timeline deathCountdownTimeline;
    private Timeline reportCooldownTimeline;

    public GameController(
            RoomMediaLibrary mediaLibrary,
            List<String> roomKeys,
            ThreatManager threatManager,
            AnomalyManager anomalyManager,
            SceneManager sceneManager
    ) {
        this.mediaLibrary = mediaLibrary;
        this.roomKeys = roomKeys;
        this.threatManager = threatManager;
        this.anomalyManager = anomalyManager;
        this.sceneManager = sceneManager;
        if (!roomKeys.isEmpty()) {
            currentRoomIndex = random.nextInt(roomKeys.size());
        }
    }

    public void attachView(GameView view) {
        this.view = view;
        refreshRoomLabel();
        refreshThreatView();
        refreshTimeLabel();
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
    }

    public void startNewGame() {
        if (view == null) {
            return;
        }
        shutdown(); // stop any previous session cleanly
        jumpscareActive = false;
        cameraTransitionPlaying = false;
        reportSubmissionInProgress = false;
        paused = false;
        deathCountdownActive = false;

        gameMinutesTotal = 0;
        secondsOnCurrentRoom = 0;
        threatLevel = 0;

        anomalyManager.reset();
        if (!roomKeys.isEmpty()) {
            currentRoomIndex = random.nextInt(roomKeys.size());
        }

        playCurrentRoomMedia();
        refreshRoomLabel();
        refreshThreatView();
        refreshTimeLabel();

        startTimelines();
    }

    private void startTimelines() {
        gameTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tickGameSecond()));
        gameTimeline.setCycleCount(Timeline.INDEFINITE);
        gameTimeline.play();

        anomalyTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> tickAnomalies()));
        anomalyTimeline.setCycleCount(Timeline.INDEFINITE);
        anomalyTimeline.play();
    }

    private void tickGameSecond() {
        if (paused || jumpscareActive) {
            return;
        }
        gameMinutesTotal++;
        refreshTimeLabel();

        secondsOnCurrentRoom++;
        if (secondsOnCurrentRoom >= 20) {
            secondsOnCurrentRoom = 0;
            if (!roomKeys.isEmpty()) {
                currentRoomIndex = (currentRoomIndex + 1) % roomKeys.size();
                playCurrentRoomMedia();
                refreshRoomLabel();
                adjustThreat(3);
            }
        }

        if (gameMinutesTotal >= 360) {
            stopAllTimelines();
            Platform.runLater(() -> showGameOver(true));
        }
    }

    private void tickAnomalies() {
        if (paused || jumpscareActive) {
            return;
        }
        if (gameMinutesTotal < 30) {
            return;
        }
        if (roomKeys.isEmpty()) {
            return;
        }

        String currentKey = roomKeys.get(currentRoomIndex);
        RoomState currentState = anomalyManager.getState(currentKey);
        boolean currentHadAnomaly = currentState != null && currentState.hasAnomaly;

        int misses = anomalyManager.tickExistingAnomalies(getDelayBeforePenaltySeconds(), currentRoomIndex);
        if (misses > 0) {
            adjustThreat(getMissAnomalyPenalty() * misses);
        }

        boolean currentHasAnomalyNow = currentState != null && currentState.hasAnomaly;
        if (currentHadAnomaly && !currentHasAnomalyNow) {
            playCurrentRoomMedia();
        }

        double spawnChance = getAnomalySpawnChance();
        if (random.nextDouble() > spawnChance) {
            return;
        }
        anomalyManager.spawnAnomalyExcludingCurrent(currentRoomIndex);
    }

    private void refreshTimeLabel() {
        if (view == null) {
            return;
        }
        int h = gameMinutesTotal / 60;
        int m = gameMinutesTotal % 60;
        int dispH = (12 + h) % 12;
        if (dispH == 0) {
            dispH = 12;
        }
        view.setTimeText(String.format("%d:%02d AM", dispH, m));
    }

    private void refreshRoomLabel() {
        if (view == null) {
            return;
        }
        if (roomKeys.isEmpty()) {
            view.setRoomText("");
            return;
        }
        view.setRoomText("Room: " + prettifyRoomKey(roomKeys.get(currentRoomIndex)));
    }

    private static String prettifyRoomKey(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        String[] parts = key.split("_+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isBlank()) {
                continue;
            }
            String lower = p.toLowerCase();
            sb.append(Character.toUpperCase(lower.charAt(0)));
            if (lower.length() > 1) {
                sb.append(lower.substring(1));
            }
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    private void refreshThreatView() {
        if (view == null) {
            return;
        }
        ThreatManager.ThreatViewModel vm = threatManager.toViewModel(threatLevel);
        double progress = Math.max(0.0, Math.min(1.0, threatLevel / 100.0));
        view.setThreat(vm, progress);
    }

    private void playCurrentRoomMedia() {
        if (view == null || roomKeys.isEmpty()) {
            return;
        }
        String roomKey = roomKeys.get(currentRoomIndex);
        RoomState state = anomalyManager.getState(roomKey);
        String mediaPath;
        if (state != null && state.hasAnomaly && state.anomalyType != null) {
            mediaPath = mediaLibrary.getRandomAnomaly(roomKey, state.anomalyType);
        } else {
            mediaPath = mediaLibrary.getRandomNormal(roomKey);
        }
        view.playLoopingMedia(mediaPath);
    }

    private void adjustThreat(int delta) {
        threatLevel = threatManager.clamp(threatLevel + delta);
        refreshThreatView();
        if (threatLevel >= 100) {
            startDeathCountdown();
        } else {
            stopDeathCountdown();
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
        if (jumpscareActive || view == null || roomKeys.isEmpty()) {
            return;
        }
        jumpscareActive = true;
        stopDeathCountdown();
        stopAllTimelines();
        paused = true;

        currentRoomIndex = (currentRoomIndex + 1) % roomKeys.size();
        String roomKey = roomKeys.get(currentRoomIndex);
        String jumpscarePath = mediaLibrary.getRandomJumpscare(roomKey);
        if (jumpscarePath == null) {
            showGameOver(false);
            return;
        }

        view.playOnceMedia(jumpscarePath, () -> showGameOver(false));
    }

    private void showGameOver(boolean win) {
        if (view == null) {
            return;
        }
        jumpscareActive = false;
        view.stopMedia();
        stopAllTimelines();
        view.showGameOverOverlay(win);
    }

    private void stopAllTimelines() {
        if (gameTimeline != null) {
            gameTimeline.stop();
        }
        if (anomalyTimeline != null) {
            anomalyTimeline.stop();
        }
        if (deathCountdownTimeline != null) {
            deathCountdownTimeline.stop();
        }
    }

    private void startReportCooldown() {
        if (view == null) {
            return;
        }
        if (reportCooldownTimeline != null) {
            reportCooldownTimeline.stop();
        }
        view.setReportButtonDisabled(true);
        final int[] secondsLeft = {REPORT_COOLDOWN_SECONDS};
        view.setReportCooldownText("Cooldown: " + secondsLeft[0] + "s");
        reportCooldownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsLeft[0]--;
            if (secondsLeft[0] <= 0) {
                reportCooldownTimeline.stop();
                view.setReportButtonDisabled(false);
                view.setReportCooldownText("");
            } else {
                view.setReportCooldownText("Cooldown: " + secondsLeft[0] + "s");
            }
        }));
        reportCooldownTimeline.setCycleCount(REPORT_COOLDOWN_SECONDS);
        reportCooldownTimeline.play();
    }

    private boolean isReportCooldownRunning() {
        return reportCooldownTimeline != null
                && reportCooldownTimeline.getStatus() == javafx.animation.Animation.Status.RUNNING;
    }

    private void closeReportOverlay(boolean startCooldown) {
        reportSubmissionInProgress = false;
        if (view != null) {
            view.hideReportOverlay();
        }
        if (startCooldown) {
            startReportCooldown();
        }
    }

    @Override
    public void onPrevRoom() {
        if (jumpscareActive || cameraTransitionPlaying || paused || roomKeys.isEmpty() || view == null) {
            return;
        }
        cameraTransitionPlaying = true;
        int targetIndex = (currentRoomIndex - 1 + roomKeys.size()) % roomKeys.size();
        view.playOnceMedia(CAMERA_TRANSITION_PATH, () -> {
            currentRoomIndex = targetIndex;
            secondsOnCurrentRoom = 0;
            playCurrentRoomMedia();
            refreshRoomLabel();
            cameraTransitionPlaying = false;
        });
    }

    @Override
    public void onNextRoom() {
        if (jumpscareActive || cameraTransitionPlaying || paused || roomKeys.isEmpty() || view == null) {
            return;
        }
        cameraTransitionPlaying = true;
        int targetIndex = (currentRoomIndex + 1) % roomKeys.size();
        view.playOnceMedia(CAMERA_TRANSITION_PATH, () -> {
            currentRoomIndex = targetIndex;
            secondsOnCurrentRoom = 0;
            playCurrentRoomMedia();
            refreshRoomLabel();
            cameraTransitionPlaying = false;
        });
    }

    @Override
    public void onPauseRequested() {
        if (jumpscareActive || view == null) {
            return;
        }
        paused = true;
        view.pauseMedia();
        if (gameTimeline != null) {
            gameTimeline.pause();
        }
        if (anomalyTimeline != null) {
            anomalyTimeline.pause();
        }
        if (deathCountdownTimeline != null) {
            deathCountdownTimeline.pause();
        }
        view.showPauseOverlay();
    }

    @Override
    public void onReportRequested() {
        if (jumpscareActive || paused || view == null) {
            return;
        }
        if (isReportCooldownRunning() || reportSubmissionInProgress) {
            return;
        }
        view.showReportOverlay();
    }

    @Override
    public void onReportTypeSelected(String label) {
        if (view == null || reportSubmissionInProgress) {
            return;
        }
        reportSubmissionInProgress = true;
        view.setReportTypeButtonsEnabled(false);

        final int roomIndexAtReport = currentRoomIndex;
        RoomMediaLibrary.AnomalyType selectedType = mapLabelToAnomalyType(label);

        view.setReportStatus("Reporting...", "-fx-text-fill: #ffaa44; -fx-font-size: 20px; -fx-font-family: Arial;");

        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            if (roomKeys.isEmpty()) {
                adjustThreat(getMissAnomalyPenalty());
                view.setReportStatus("NO ANOMALY FOUND", "-fx-text-fill: #ff5555; -fx-font-size: 20px; -fx-font-family: Arial;");
                Timeline doneDelay = new Timeline(new KeyFrame(Duration.millis(1200), ev -> closeReportOverlay(true)));
                doneDelay.setCycleCount(1);
                doneDelay.play();
                return;
            }

            String roomKey = roomKeys.get(roomIndexAtReport);
            RoomState state = anomalyManager.getState(roomKey);
            if (state == null || !state.hasAnomaly || selectedType == null || state.anomalyType != selectedType) {
                adjustThreat(getMissAnomalyPenalty());
                view.setReportStatus("NO ANOMALY FOUND", "-fx-text-fill: #ff5555; -fx-font-size: 20px; -fx-font-family: Arial;");
                Timeline doneDelay = new Timeline(new KeyFrame(Duration.millis(1200), ev -> closeReportOverlay(true)));
                doneDelay.setCycleCount(1);
                doneDelay.play();
                return;
            }

            state.hasAnomaly = false;
            state.anomalyType = null;
            state.anomalySecondsAlive = 0;
            state.penaltyApplied = false;

            adjustThreat(getCorrectReportDelta());
            if (currentRoomIndex == roomIndexAtReport) {
                playCurrentRoomMedia();
            }
            view.setReportStatus("ANOMALY REMOVED", "-fx-text-fill: #88ff88; -fx-font-size: 20px; -fx-font-family: Arial;");
            Timeline doneDelay = new Timeline(new KeyFrame(Duration.millis(1200), ev -> closeReportOverlay(true)));
            doneDelay.setCycleCount(1);
            doneDelay.play();
        }));
        delay.setCycleCount(1);
        delay.play();
    }

    @Override
    public void onReportCancelled() {
        if (view == null) {
            return;
        }
        closeReportOverlay(false);
    }

    @Override
    public void onResumeFromPause() {
        if (view == null) {
            return;
        }
        paused = false;
        view.hidePauseOverlay();
        view.resumeMedia();
        if (gameTimeline != null) {
            gameTimeline.play();
        }
        if (anomalyTimeline != null) {
            anomalyTimeline.play();
        }
        if (deathCountdownTimeline != null && deathCountdownActive) {
            deathCountdownTimeline.play();
        }
    }

    @Override
    public void onReturnToMainMenuFromPause() {
        if (view != null) {
            view.hidePauseOverlay();
        }
        paused = false;
        sceneManager.showMainMenu();
    }

    @Override
    public void onReturnToMainMenuFromGameOver() {
        if (view != null) {
            view.hideGameOverOverlay();
        }
        sceneManager.showMainMenu();
    }

    @Override
    public void onPlayAgainFromGameOver() {
        if (view != null) {
            view.hideGameOverOverlay();
        }
        sceneManager.playAgainSameDifficulty();
    }

    public void shutdown() {
        reportSubmissionInProgress = false;
        paused = false;
        cameraTransitionPlaying = false;
        jumpscareActive = false;
        deathCountdownActive = false;

        if (reportCooldownTimeline != null) {
            reportCooldownTimeline.stop();
        }
        stopAllTimelines();
        if (view != null) {
            view.hideReportOverlay();
            view.hidePauseOverlay();
            view.hideGameOverOverlay();
            view.stopMedia();
            view.setReportCooldownText("");
            view.setReportButtonDisabled(false);
        }
    }

    private double getAnomalySpawnChance() {
        return switch (difficulty) {
            case EASY -> 0.3;
            case HARD -> 0.8;
            case MEDIUM -> 0.5;
        };
    }

    private int getMissAnomalyPenalty() {
        return switch (difficulty) {
            case EASY -> 5;
            case HARD -> 15;
            case MEDIUM -> 10;
        };
    }

    private int getCorrectReportDelta() {
        return switch (difficulty) {
            case EASY -> -5;
            case HARD -> 0;
            case MEDIUM -> -3;
        };
    }

    private int getDelayBeforePenaltySeconds() {
        return switch (difficulty) {
            case EASY -> 15;
            case HARD -> 5;
            case MEDIUM -> 10;
        };
    }

    private RoomMediaLibrary.AnomalyType mapLabelToAnomalyType(String label) {
        if (label == null) {
            return null;
        }
        return switch (label) {
            case "Missing Object" -> RoomMediaLibrary.AnomalyType.MISSING_OBJECT;
            case "Object Displacement" -> RoomMediaLibrary.AnomalyType.OBJECT_DISPLACEMENT;
            case "Shadowy Figure" -> RoomMediaLibrary.AnomalyType.SHADOWY_FIGURE;
            case "Intruder" -> RoomMediaLibrary.AnomalyType.INTRUDER;
            case "Strange Imagery" -> RoomMediaLibrary.AnomalyType.STRANGE_IMAGERY;
            case "Demonic" -> RoomMediaLibrary.AnomalyType.DEMONIC;
            case "Extra Object" -> RoomMediaLibrary.AnomalyType.EXTRA_OBJECT;
            case "Audio Disturbance" -> RoomMediaLibrary.AnomalyType.AUDIO_DISTURBANCE;
            default -> null;
        };
    }
}

